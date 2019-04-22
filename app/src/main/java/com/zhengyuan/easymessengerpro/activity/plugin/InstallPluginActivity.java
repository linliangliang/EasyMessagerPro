package com.zhengyuan.easymessengerpro.activity.plugin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.http.OkHttpUtil;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.easymessengerpro.RePluginHelper;
import com.zhengyuan.easymessengerpro.activity.BaseSimpleListActivity;
import com.zhengyuan.easymessengerpro.entity.PluginEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2017/11/4.
 */

public class InstallPluginActivity extends BaseSimpleListActivity {

    private final String TAG = "InstallPlugin";
    private List<PluginEntity> pluginInfos = new ArrayList<>();

    @Override
    protected String getTitleName() {
        return "安装插件";
    }

    @Override
    protected void getData() {

        listData.clear();

        // 通过json获取内直插件的名字和资源id
        PluginEntity pluginEntity;
        List<PluginEntity> pluginEntities = RePluginHelper.INSTANCE.serverPluginEntities;
        for (int i = 0; i < pluginEntities.size(); i++) {

            pluginEntity = pluginEntities.get(i);
//                Log.d(TAG, "PluginEntity: " + pluginEntity.realName + " : " + pluginEntity.version);

            if (RePluginHelper.INSTANCE.installedPurePluginsMap.keySet().contains(pluginEntity.realName)) {

//                    Log.d(TAG, "installedPurePluginsMap: " +
//                            RePluginHelper.INSTANCE.installedPurePluginsMap.get(pluginEntity.realName).getVersion());
                if (RePluginHelper.INSTANCE.installedPurePluginsMap.get(pluginEntity.realName).getVersion()
                        < pluginEntity.version) {
                    listData.add(new String[]{pluginEntity.showName + "(新版本)",
                            pluginEntity.version + ""});
                    pluginInfos.add(pluginEntity);
                }
            } else {
                listData.add(new String[]{pluginEntity.showName + "(未安装)",
                        pluginEntity.version + ""});
                pluginInfos.add(pluginEntity);
            }
        }
    }

    @Override
    protected AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                pos = position;
                name = pluginInfos.get(position).realName + "-" +
                        pluginInfos.get(position).version + ".jar";
                realName = pluginInfos.get(position).realName;
                url = Constants.DownLoadBaseUrl +
                        Constants.PLUGIN_BASE_URL + name;
                new Thread(getSizeRunnable).start();
            }
        };
    }

    private void downloadAPK() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressDialog.setTitle("下载\"" + pluginInfos.get(pos).showName +
                " - " + pluginInfos.get(pos).version + "\"中");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final String path = Utils.getPluginDir() + pluginInfos.get(pos).realName + ".jar";
        OkHttpUtil.INSTANCE.download(url, path,
                new OkHttpUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess() {
                        //在主进程刷新，插进列表，更新已安装插件缓存，实现刷新即可显示插件
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final PluginInfo pluginInfo = RePlugin.install(path);
                                if (pluginInfo != null) {
                                    // 内置/外置插件升级时, preload调不调用效果一样,
                                    // 第二次启动只能读到升级的插件, 第三次启动恢复正常
                                    // 解决: 在关闭应用时, 将:GuardService进程关掉即可
                                    // 代码见NotificationService
                                    RePlugin.preload(pluginInfo);

                                    listData.remove(pos);//刷新列表数据
                                    pluginInfos.remove(pos);
                                    simpleAdapter.notifyDataSetChanged();
                                    //实现直接刷新，不需要退出程序
                                    boolean flag = RePluginHelper.INSTANCE.addToInstalledPluginEntities(realName);//根据插件名，将插件添加到要显示的插件的list中
                                    //数据已经更新。在主界面点击按钮刷新
                                    progressDialog.dismiss();//下载成功
                                    Utils.showToast("安装成功!");
                                } else {
                                    progressDialog.dismiss();
                                    Utils.showToast("安装失败");
                                }
                            }
                        });
                       /* runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Utils.showToast("下载成功");
                                final PluginInfo pluginInfo = RePlugin.install(path);
                                if (pluginInfo != null) {

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 内置/外置插件升级时, preload调不调用效果一样,
                                            // 第二次启动只能读到升级的插件, 第三次启动恢复正常
                                            // 解决: 在关闭应用时, 将:GuardService进程关掉即可
                                            // 代码见NotificationService
                                            RePlugin.preload(pluginInfo);
                                        }
                                    }).start();

                                    listData.remove(pos);
                                    pluginInfos.remove(pos);
//                                    getData();
                                    simpleAdapter.notifyDataSetChanged();
                                } else {
                                    Utils.showToast("安装失败");
                                }
                            }
                        });*/
                    }

                    @Override
                    public void onDownloading(final int progress) {

//                        Log.d(TAG, "download progress: " + progress);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setProgress(progress);
                            }
                        });
                    }

                    @Override
                    public void onDownloadFailed() {
                        progressDialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showToast("下载失败");
                            }
                        });
                    }
                });
    }

    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(InstallPluginActivity.this);
        builder.setTitle("是否安装插件?");
        builder.setMessage(
                "名字: " + pluginInfos.get(pos).showName +
                        "\n版本: " + pluginInfos.get(pos).version +
                        "\n大小: " + size +
                        "\n更新日志:\n" + pluginInfos.get(pos).updateInfo);
        builder.setPositiveButton("确定安装", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadAPK();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.setCancelable(true);
        builder.create().show();
    }

    private String url, name, size;
    private int pos;
    private String realName;

    Runnable getSizeRunnable = new Runnable() {
        @Override
        public void run() {

            size = Utils.getNetWorkFileSize(url);
            InstallPluginActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showDialog();
                }
            });
        }
    };
}
