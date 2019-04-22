package com.view.dialog;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import cn.pedant.SweetAlert.R;

public class MultiChoiceAdapter extends ArrayAdapter<MultiChoiceItem>{

private int resourceId;
	
	public MultiChoiceAdapter(Context context,int textViewResourceId,List<MultiChoiceItem> objects)
	{
		super(context, textViewResourceId,objects);
		resourceId=textViewResourceId;
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		MultiChoiceItem item=getItem(position);
		View view;
		ViewHolder viewHolder;
		if (convertView==null) {
			view=LayoutInflater.from(getContext()).inflate(resourceId,null);
			viewHolder=new ViewHolder();
			viewHolder.tv=(TextView) view.findViewById(R.id.multi_choice_text);
			viewHolder.cb=(CheckBox) view.findViewById(R.id.multi_choice_check);
			view.setTag(viewHolder);
		}else {
			view=convertView;
			viewHolder=(ViewHolder) view.getTag();
		}
		viewHolder.tv.setText(item.getText());
		viewHolder.cb.setChecked(item.getChecked());
		
		return view;
	}
	class ViewHolder{
		TextView tv;
		CheckBox cb;
	}
}
