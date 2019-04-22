package com.zhengyuan.easymessengerpro.activity.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.RePluginHelper;
import com.zhengyuan.easymessengerpro.adapter.RecyclerViewAdapter;
import com.zhengyuan.easymessengerpro.entity.MainPageItemEntity;
import com.zhengyuan.easymessengerpro.entity.PluginEntity;
import com.zhengyuan.easymessengerpro.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 林亮
 * @description:
 * @date :2019/1/31 15:09
 */

public class RightSubPlugin extends Fragment {

    private final static String TAG = "RightSubPlugin";
    private ArrayList<MainPageItemEntity> listData = new ArrayList<>();
    /**
     * Fregmant根布局
     */
    public static View root;
    private RecyclerView mRecyclerView = null;
    private RecyclerViewAdapter recyclerViewAdapter = null;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtil.i(TAG, "RightFragment 的子Fregment :获取ViewPager对象---------------------------");
        root = inflater.inflate(R.layout.fragment_plugin, null);
        //initData();//在这里初始化可能存在插件信息还没有请求回来的情况，需要在插件信息请求回来之后再调用一次这个函数。
        init();
        return root;
    }

    private void init() {
        LogUtil.i(TAG, "RightFragment 的子Fregment :获取RecyclerView对象--------------------------");
        mRecyclerView = root.findViewById(R.id.RecyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); //Grid视图
        /*LogUtil.i(TAG, "RightFragment 的子Fregment :RecyclerView 添加适配器--------------------------");
        for (int i = 0; i < listData.size(); i++) {
            LogUtil.i(TAG, "listData.get(1).toString():" + listData.get(i).toString() + "--------------------------");
        }*/
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), listData, pluginEntities);
        mRecyclerView.setAdapter(recyclerViewAdapter);

    }

    // 插件信息
    public static List<PluginEntity> pluginEntities = new ArrayList<>();

    public void initData() {
        LogUtil.i(TAG, "RightFragment 的子Fregment :获取插件列表信息---------------------------");
        pluginEntities.clear();
        pluginEntities.addAll(RePluginHelper.INSTANCE.installedPluginEntities);
        listData.clear();
        // 通过json获取内直插件的名字和资源id
        LogUtil.i(TAG, "RightFragment 的子Fregment :获取插件列表信息---------------------------pluginEntities.size()" + pluginEntities.size());
        LogUtil.i(TAG, "RightFragment 的子Fregment :获取插件列表信息---------------------------RePluginHelper.INSTANCE.installedPluginEntities" + RePluginHelper.INSTANCE.installedPluginEntities);

        for (int i = 0; i < pluginEntities.size(); i++) {
            if (pluginEntities.get(i).isShowInMainView) {
                listData.add(new MainPageItemEntity(pluginEntities.get(i).showName,
                        pluginEntities.get(i).realName));
                LogUtil.i(TAG, "RightFragment 的子Fregment :获取插件列表信息---------------------------pluginEntities.get(i).realName:" + pluginEntities.get(i).realName);
            } else {
                pluginEntities.remove(i);
                i--;
            }
        }
    }


    public void refreshRightSubPlugin() {
        initData();
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }
}
