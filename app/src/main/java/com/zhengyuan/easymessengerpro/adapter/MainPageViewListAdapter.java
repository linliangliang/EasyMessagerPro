package com.zhengyuan.easymessengerpro.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qihoo360.replugin.RePlugin;
import com.zhengyuan.baselib.utils.CrashHandler;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.RePluginHelper;
import com.zhengyuan.easymessengerpro.entity.MainPageItemEntity;

import java.util.ArrayList;

/**
 * Created by zy on 2017/11/16.
 */

public class MainPageViewListAdapter extends BaseAdapter {


    private ArrayList<MainPageItemEntity> listData;
    private Context context;
    private LayoutInflater inflater = null;

    public MainPageViewListAdapter(ArrayList<MainPageItemEntity> list, Context context) {
        this.context = context;
        this.listData = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {

        return listData.size();
    }

    @Override
    public Object getItem(int position) {

        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.gridview_item, null);
            holder.content = convertView
                    .findViewById(R.id.imageview_item);
            holder.remark = convertView
                    .findViewById(R.id.textview_item);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String realName = listData.get(position).realName;

        Resources resources = RePlugin.fetchResources(realName);
        if (resources == null) {
            CrashHandler.getInstance().writeLog("position: " + position + " " + realName
                + " " + listData.get(position).toString() + " " +
                RePluginHelper.INSTANCE.installedPluginEntities + " " +
                RePluginHelper.INSTANCE.installedPurePluginsMap.get(realName));
            return convertView;
        }

        int id = resources.getIdentifier(
                "ic_launcher",
                "mipmap",
                RePluginHelper.INSTANCE.getPackageName(realName)
        );

//        holder.content.setImageDrawable(resources.getDrawable(id));

        holder.content.setImageDrawable(RePluginHelper.INSTANCE.getPluginDrawable(
                realName, "mipmap", "ic_launcher"
        ));
//        holder.remark.setText(RePluginHelper.INSTANCE.getPluginStringById(
//                realName, "app_name"
//        ));
        holder.remark.setText(listData.get(position).name);

        return convertView;
    }

    public class ViewHolder {
        ImageView content;
        TextView remark;
    }
}
