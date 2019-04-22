package com.zhengyuan.easymessengerpro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.entity.PurchaseListScanModel;

import java.util.ArrayList;

public class PurchaseListScanAdapter extends BaseAdapter{
	
	private ArrayList<PurchaseListScanModel> list;
	private Context context;
	private LayoutInflater inflater=null;
	
	public PurchaseListScanAdapter(ArrayList<PurchaseListScanModel> list,Context context){
		this.context = context;
		this.list = list;
		inflater = LayoutInflater.from(context);
	}
		@Override
		public int getCount() {
			
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			
			if(convertView == null) {
				holder = new ViewHolder();
				
				convertView = inflater.inflate(R.layout.item_purchase_list, null);
				holder.workListNumber = convertView
						.findViewById(R.id.purchase_list_number_tv);
				holder.workListStatus = convertView
						.findViewById(R.id.purchase_status_tv);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
				
			PurchaseListScanModel model = list.get(position);
			holder.workListNumber.setText(model.purchaseListNumber);
			holder.workListStatus.setText(model.getStatusString());
			
			return convertView;
		}
		
		public class ViewHolder {
			TextView workListNumber;
			TextView workListStatus;
		}

}
