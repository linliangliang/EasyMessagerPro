package com.zhengyuan.easymessengerpro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhengyuan.easymessengerpro.R;

import java.util.ArrayList;

/**
 * Created by zy on 2017/11/4.
 * 简单列表界面的adapter
 */

public class BaseSimpleListAdapter extends BaseAdapter {

    private ArrayList<String[]> listData;
    private Context context;
    private LayoutInflater inflater = null;

    public BaseSimpleListAdapter(ArrayList<String[]> list, Context context) {
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

            convertView = inflater.inflate(R.layout.item_base_simple_list, null);
            holder.content = convertView
                    .findViewById(R.id.content);
            holder.remark = convertView
                    .findViewById(R.id.remark);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.content.setText(listData.get(position)[0]);
        holder.remark.setText(listData.get(position)[1]);

        return convertView;
    }

    public class ViewHolder {
        TextView content;
        TextView remark;
    }
}
