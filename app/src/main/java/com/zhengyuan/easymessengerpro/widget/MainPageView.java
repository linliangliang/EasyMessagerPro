package com.zhengyuan.easymessengerpro.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qihoo360.replugin.RePlugin;
import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.ChatUtils;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.RePluginHelper;
import com.zhengyuan.easymessengerpro.adapter.MainPageViewListAdapter;
import com.zhengyuan.easymessengerpro.entity.MainPageItemEntity;
import com.zhengyuan.easymessengerpro.entity.PluginEntity;
import com.zhengyuan.easymessengerpro.util.LogUtil;
import com.zhengyuan.easymessengerpro.xmpp.XmppManager;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于分页中单页的gridview的布局
 */
public class MainPageView extends LinearLayout {

    private final String TAG = "MainPageView";
    private Context context;
    private GridView gridView;
    private MainPageViewListAdapter mainPageViewListAdapter;
    private ArrayList<MainPageItemEntity> listData = new ArrayList<>();

    public MainPageView(Context context) {

        super(context);
        this.context = context;
        inflate(context, R.layout.widget_main_page_view, this);

        initData();
        initView();
    }

    // 控制页面数
    public int getPageNumber() {

        return 1; //listData.size() / 15 + 1;
    }

    private void initView() {

        gridView = findViewById(R.id.grid_view);

        mainPageViewListAdapter = new MainPageViewListAdapter(listData, getContext());
        gridView.setAdapter(mainPageViewListAdapter);
        gridView.setOnItemClickListener(onItemClickListener);
        gridView.setOnItemLongClickListener(onItemLongClickListener);
    }

    // 插件
    private List<PluginEntity> pluginEntities = new ArrayList<>();

    private void initData() {

        pluginEntities.clear();
        pluginEntities.addAll(RePluginHelper.INSTANCE.installedPluginEntities);

//        List<PluginInfo> pluginInfos = RePluginHelper.INSTANCE.installedPurePluginInfos;

        listData.clear();
        // 通过json获取内直插件的名字和资源id
        for (int i = 0; i < pluginEntities.size(); i++) {
            if (pluginEntities.get(i).isShowInMainView) {
                listData.add(new MainPageItemEntity(pluginEntities.get(i).showName,
                        pluginEntities.get(i).realName));
            } else {
                pluginEntities.remove(i);
                i--;
            }
        }
    }

    /**
     * item监听事件处理
     */
    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> arg0, View view, int position,
                                long arg3) {

            String realName = pluginEntities.get(position).realName;


            //TODO 对二维码特殊处理,后边要把扫描界面统一或者把选择框放到插件中
            if (realName.equals("EMQRCode")) {
                chooseSanningMode(getContext());
            } else if (realName.equals("EMProgramBurn")
                    && (XmppManager.ScanFunction == null
                    || XmppManager.ScanFunction.equals("")
                    || XmppManager.ScanFunction.equals("null"))) {

                Toast.makeText(getContext(), "您没有权限", Toast.LENGTH_SHORT).show();
            } else {
                // TODO 调试新的插件代码
//                if (position == 0) {
//
//                    Intent intent = new Intent(getContext(), MaterialInfoUpdateActivity.class);
//                    getContext().startActivity(intent);
//                    return;
//                }
                Intent intent = RePlugin.createIntent(realName,
                        RePluginHelper.INSTANCE.getPackageName(realName) +
                                pluginEntities.get(position).host2PluginActivities[0].name);

                Log.d(TAG, "realName " + realName + " packageName " +
                        RePluginHelper.INSTANCE.getPackageName(realName) +
                        pluginEntities.get(position).host2PluginActivities[0].name);
                RePlugin.startActivity(getContext(), intent);
            }
        }
    };


    /**
     * GradView Item长按弹框删除
     */
    private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
            LogUtil.d(TAG, "position: " + position);
            final String name = pluginEntities.get(position).realName;
            LogUtil.d(TAG, "realName=" + name);

            //长按弹窗，弹窗功能前人已经封装了，但是我忘了在哪个包中，这里自己写
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("是否卸载 " + RePluginHelper.INSTANCE.realName2ShowNameMaps.get(name) + "?");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //确认删除
                    boolean isRunning = RePlugin.isPluginRunning(name);
                    Log.d(TAG, "isRunning: " + name + " " + isRunning);


                    if (isRunning) {

                        if (RePlugin.uninstall(name)) {
                            Utils.showToast("卸载成功");
                        } else
                            Utils.showToast("卸载成功, 重启App即可生效");

                        //刷新界面数据
                        /*pluginEntities.remove(position);
                        ((MainActivity) context).refreshLayout();*/
                    } else {

                        if (RePlugin.uninstall(name)) {
                            Utils.showToast("卸载成功");
                            /*pluginEntities.remove(position);
                            ((MainActivity) context).refreshLayout();*/
                        } else
                            Utils.showToast("卸载失败");
                    }
                }
            });
            builder.setNegativeButton("取消", null);
            builder.setCancelable(true);
            builder.show();
            //更新pluginEntities内容，剔除被删除的插件
            return true;
        }
    };


    /**
     * 弹出选择扫码格式对话框
     */
    protected void chooseSanningMode(final Context context) {
        Builder build = new AlertDialog.Builder(context);
        build.setTitle("请选择");
        String[] items = new String[]{"条码扫描", "二维码扫描", "手动输入"};
        build.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("EMQRCode",
                                "zhengyuan.easymessenger.zxing.activity.CaptureActivity"));
                        RePlugin.startActivityForResult(
                                (Activity) context, intent,
                                Constants.EM_QRCODE_REQUEST_CODE, null);
                        break;
                    case 1:
                        Intent intent1 = new Intent();
                        intent1.setComponent(new ComponentName("EMQRCode",
                                "com.zbar.lib.CaptureActivity"));
                        RePlugin.startActivityForResult(
                                (Activity) context, intent1,
                                Constants.EM_QRCODE_REQUEST_CODE, null);

                        break;
                    case 2:
                        scanning2();
                        break;
                    default:
                        break;
                }
            }
        });
        build.create().show();
    }

    /**
     * 手动输入
     */
    protected void scanning2() {

        Builder builder = new AlertDialog.Builder(Constants.contexts.get(Constants.contexts.size() - 1));
        final EditText editText = new EditText(Constants.contexts.get(Constants.contexts.size() - 1));
        builder.setTitle("请输入条码对应的序列号");
        builder.setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                String scanReslult1 = editText.getText().toString().trim();
                Element element = new Element("mybody");
                element.addProperty("type", "requestInsertScanningNormal");
                element.addProperty("scanning", scanReslult1);
                ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString());
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    //弹出扫码
    protected void scanning() {
        Builder builder = new AlertDialog.Builder(Constants.contexts.get(Constants.contexts.size() - 1));
        final EditText editText = new EditText(Constants.contexts.get(Constants.contexts.size() - 1));
        builder.setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                String scanning = editText.getText().toString().trim();
                Element element = new Element("mybody");
                element.addProperty("type", "requestQueryWorkPlanByScanning");
                if (scanning != null && !scanning.equals("")) {
                    element.addProperty("scanning", scanning);
                }
                ChatManager chatmanager = XmppManager.getConnection().getChatManager();
                Chat newchat0 = chatmanager.createChat("iqreceiver@" + XmppManager.getConnection().getServiceName(), null);//xxzx-gyj8860
                Utils.createCircleProgressDialog(Constants.contexts.get(Constants.contexts.size() - 1),
                        "正在获取班组工作单，请等待...");
                try {
                    newchat0.sendMessage(element.toString());
                } catch (XMPPException e) {
                    //
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }
}