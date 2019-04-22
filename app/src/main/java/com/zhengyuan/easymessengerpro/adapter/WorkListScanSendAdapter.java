package com.zhengyuan.easymessengerpro.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.entity.WorkListScanSendModel;

import java.util.List;



public class WorkListScanSendAdapter extends Adapter<WorkListScanSendAdapter.MyHolder>{

	private Context mContext;
    private List<WorkListScanSendModel> mDatas;

    public WorkListScanSendAdapter(Context context, List<WorkListScanSendModel> datas) {
        super();
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return mDatas.size();
    }

    @Override
    // 填充onCreateViewHolder方法返回的holder中的控件
    public void onBindViewHolder(MyHolder holder, int position) {
        // TODO Auto-generated method stub
    	holder.textView1.setText(mDatas.get(position).materiel);
    	holder.textView2.setText(mDatas.get(position).requirement);
    	holder.textView3.setText(mDatas.get(position).overplus);
    	holder.textView4.setText(mDatas.get(position).storeIssue);
        holder.textView5.setText(mDatas.get(position).getStatusString());
    }

    @Override
    // 重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    public MyHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
        // 填充布局
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_work_list_send, null);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    // 定义内部类继承ViewHolder
    class MyHolder extends ViewHolder {

        private TextView textView1;
        private TextView textView2;
        private TextView textView3;
        private TextView textView4;
        private TextView textView5;

        public MyHolder(View view) {
            super(view);
            textView1 = view.findViewById(R.id.worklist_scan_send1);
            textView2 = view.findViewById(R.id.worklist_scan_send2);
            textView3 = view.findViewById(R.id.worklist_scan_send3);
            textView4 = view.findViewById(R.id.worklist_scan_send4);
            textView5 = view.findViewById(R.id.worklist_scan_send5);
        }

    }
}
