package com.zhengyuan.easymessengerpro.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qihoo360.replugin.RePlugin;
import com.zhengyuan.baselib.utils.CrashHandler;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.RePluginHelper;
import com.zhengyuan.easymessengerpro.activity.fragment.RightSubPlugin;
import com.zhengyuan.easymessengerpro.entity.MainPageItemEntity;
import com.zhengyuan.easymessengerpro.entity.PluginEntity;
import com.zhengyuan.easymessengerpro.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 林亮
 * @description:
 * @date :2019/2/1 16:59
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.NormalViewHolder> {
    private final static String TAG = "RecyclerViewAdapter";
    private ArrayList<MainPageItemEntity> listData;

    private LayoutInflater mLayoutInflater;

    private Context mContext;

    // 插件
    private List<PluginEntity> pluginEntities = new ArrayList<>();

    //通过构造方法将图片以及文字，上下文传递过去
    public RecyclerViewAdapter(Context context, ArrayList<MainPageItemEntity> list, List<PluginEntity> pluginEntities) {
        LogUtil.i(TAG, "RecyclerViewAdapter:构造函数--------------------------");
        mContext = context;
        listData = list;
        mLayoutInflater = LayoutInflater.from(context);
        this.pluginEntities = pluginEntities;
    }

    //我们创建一个ViewHolder并返回，ViewHolder必须有一个带有View的构造函数，这个View就是我们Item的根布局，在这里我们使用自定义Item的布局；
    @Override
    public NormalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogUtil.i(TAG, "RRecyclerViewAdapter:onCreateViewHolder函数--------------------------");
        return new NormalViewHolder(mLayoutInflater.inflate(R.layout.recycleview_item, parent, false));
    }

    //将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(NormalViewHolder holder, final int position) {
        LogUtil.i(TAG, "RRecyclerViewAdapter:onBindViewHolder函数--------------------------");
        final String realName = listData.get(position).realName;
        Resources resources = RePlugin.fetchResources(realName);
        if (resources == null) {
            CrashHandler.getInstance().writeLog("position: " + position + " " + realName
                    + " " + listData.get(position).toString() + " " +
                    RePluginHelper.INSTANCE.installedPluginEntities + " " +
                    RePluginHelper.INSTANCE.installedPurePluginsMap.get(realName));
        } else {
            int id = resources.getIdentifier(
                    "ic_launcher",
                    "mipmap",
                    RePluginHelper.INSTANCE.getPackageName(realName)
            );

            holder.mImageView.setImageDrawable(RePluginHelper.INSTANCE.getPluginDrawable(
                    realName, "mipmap", "ic_launcher"
            ));
            holder.mTextView.setText(listData.get(position).name);


            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = RePlugin.createIntent(realName,
                            RePluginHelper.INSTANCE.getPackageName(realName) +
                                    RightSubPlugin.pluginEntities.get(position).host2PluginActivities[0].name);

                    Log.d(TAG, "realName " + realName + " packageName " +
                            RePluginHelper.INSTANCE.getPackageName(realName) +
                            RightSubPlugin.pluginEntities.get(position).host2PluginActivities[0].name);
                    RePlugin.startActivity(mContext, intent);
                }
            });
        }
    }


    //获取数据的数量
    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }


    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class NormalViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        LinearLayout mCardView;
        ImageView mImageView;

        public NormalViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.tv_text);
            mCardView = itemView.findViewById(R.id.cv_item);
            mImageView = (ImageView) itemView.findViewById(R.id.iv_pic);
        }
    }

}

