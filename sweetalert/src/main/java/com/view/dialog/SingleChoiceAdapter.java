package com.view.dialog;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import cn.pedant.SweetAlert.R;

public class SingleChoiceAdapter extends ArrayAdapter<String>{

	private  int resourceId;
	private int selectedIndex=-1;
	public SingleChoiceAdapter(Context context, int textViewResourceId, List<String> objects) {
		super(context, textViewResourceId, objects);
		resourceId=textViewResourceId;
	}
	public void setSelectedIndex(int index)
	{
		selectedIndex=index;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String itemText=getItem(position);
		View view;
		ViewHolder viewHolder;
		if (convertView==null) {
			view=LayoutInflater.from(getContext()).inflate(resourceId,null);
			viewHolder=new ViewHolder();
			viewHolder.itemText=(TextView) view.findViewById(R.id.list_item_text);
			viewHolder.itemRadio=(RadioButton) view.findViewById(R.id.list_item_radio);
			view.setTag(viewHolder);
		}else {
			view=convertView;
			viewHolder=(ViewHolder) view.getTag();
		}
		viewHolder.itemText.setText(itemText);
		if(selectedIndex==position)
		{
			viewHolder.itemRadio.setChecked(true);
		}else {
			viewHolder.itemRadio.setChecked(false);
		}
		return view;
	}
	
	class ViewHolder
	{
		TextView itemText;
		RadioButton itemRadio;
	}

}
