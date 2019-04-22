package com.zhengyuan.easymessengerpro.activity.plugin;

import android.view.View;
import android.widget.AdapterView;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.easymessengerpro.activity.BaseSimpleListActivity;

import java.util.List;

/**
 * Created by zy on 2017/11/4.
 */

public class UpdatePluginActivity extends BaseSimpleListActivity {

    private final String TAG = "UpdatePlugin";
    private List<PluginInfo> pluginInfos;

    @Override
    protected String getTitleName() {
        return "更新插件";
    }

    @Override
    protected void getData() {

        listData.add(new String[]{"安装插件", ""});
//        listData.add(new String[]{"更新插件", ""});
//        listData.add(new String[]{"卸载插件", ""});
    }

    @Override
    protected AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                PluginInfo pluginInfo = RePlugin.install(
                        Utils.getPluginDir() + "EMTest.jar"
                );
                if (pluginInfo != null) {
                    Utils.showToast("更新成功");

                    getData();
                    simpleAdapter.notifyDataSetChanged();
                } else {
                    Utils.showToast("更新失败");
                }
            }
        };
    }
}
