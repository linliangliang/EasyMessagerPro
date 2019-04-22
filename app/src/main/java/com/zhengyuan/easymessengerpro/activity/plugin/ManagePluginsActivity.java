package com.zhengyuan.easymessengerpro.activity.plugin;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.zhengyuan.easymessengerpro.activity.BaseSimpleListActivity;

/**
 * Created by zy on 2017/11/4.
 * 插件管理界面
 * --2019-1-24 该activity被弃用，使用popwindow
 */

public class ManagePluginsActivity extends BaseSimpleListActivity {

    private final String TAG = "ManagePluginsActivity";

    @Override
    protected String getTitleName() {
        return "管理插件";
    }

    @Override
    protected void getData() {

        listData.add(new String[]{"装载插件", ""});
//        listData.add(new String[]{"测试安装和更新插件", ""});
        listData.add(new String[]{"卸载插件", ""});

    }

    private enum ITEM_TAG {
        INSTALL_PLUGIN,
        //        UPDATE_PLUGIN,
        UNINSTALL_PLUGIN
    }

    @Override
    protected AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent;
                ITEM_TAG tag = ITEM_TAG.values()[position];
                switch (tag) {

                    case INSTALL_PLUGIN:
                        intent = new Intent(ManagePluginsActivity.this, InstallPluginActivity.class);
                        startActivity(intent);
                        break;
//                    case UPDATE_PLUGIN:
//                        intent = new Intent(ManagePluginsActivity.this, UpdatePluginActivity.class);
//                        startActivity(intent);
//                        break;
                    case UNINSTALL_PLUGIN:
                        intent = new Intent(ManagePluginsActivity.this, UninstallPluginActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        };
    }
}
