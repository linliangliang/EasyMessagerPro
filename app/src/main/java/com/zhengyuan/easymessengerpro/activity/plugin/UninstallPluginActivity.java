package com.zhengyuan.easymessengerpro.activity.plugin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.easymessengerpro.RePluginHelper;
import com.zhengyuan.easymessengerpro.activity.BaseSimpleListActivity;

/**
 * Created by zy on 2017/11/4.
 */

public class UninstallPluginActivity extends BaseSimpleListActivity {

    private final String TAG = "UninstallPlugin";

    @Override
    protected String getTitleName() {
        return "卸载插件";
    }

    @Override
    protected void getData() {

        RePluginHelper.INSTANCE.initInstalledPluginInfos();

        listData.clear();

        for (PluginInfo pluginInfo : RePluginHelper.INSTANCE.installedPurePluginInfos) {
            Log.d(TAG, pluginInfo.toString());

            String realName = pluginInfo.getName();
            String showName = RePluginHelper.INSTANCE.realName2ShowNameMaps.get(realName);

            if (RePlugin.getPluginInfo(realName).getType() == PluginInfo.TYPE_BUILTIN)
                showName = showName + " (内置)";
            else
                showName = showName + " (外置)";
            listData.add(new String[]{showName, "版本:" + pluginInfo.getVersion()});
        }
    }

    @Override
    protected AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                final String name = RePluginHelper.INSTANCE.installedPurePluginInfos.get(position).getName();
                if (RePlugin.getPluginInfo(name).getType() == PluginInfo.TYPE_BUILTIN) {
                    Utils.showToast("内置插件不可卸载");
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(UninstallPluginActivity.this);
                builder.setTitle("是否卸载 " + RePluginHelper.INSTANCE.realName2ShowNameMaps.get(name) + "?")
                        .setPositiveButton("确定卸载", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                boolean isRunning = RePlugin.isPluginRunning(name);
                                Log.d(TAG, "isRunning: " + name + " " + isRunning);

                                if (isRunning) {

                                    if (RePlugin.uninstall(name)) {
                                        Utils.showToast("卸载成功");
                                    } else
                                        Utils.showToast("卸载成功, 重启App即可生效");

//                                    listData.remove(position);
                                    getData();
                                    simpleAdapter.notifyDataSetChanged();
                                } else {

                                    if (RePlugin.uninstall(name)) {
                                        Utils.showToast("卸载成功");

//                                        listData.remove(position);
                                        getData();
                                        simpleAdapter.notifyDataSetChanged();
                                    } else
                                        Utils.showToast("卸载失败");
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(true)
                        .create().show();
            }
        };
    }
}
