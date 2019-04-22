package com.zhengyuan.easymessengerpro.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zhengyuan.easymessengerpro.R;

import java.util.List;
import java.util.Map;

/**
 * 
 * 班组批量填写作业单adapter
 * */
public class BatchWorkPlanSubmitAdapter extends BaseAdapter {
	private List<Map<String, Object>> list;
	private Context context;
	private LayoutInflater inflater = null;

	public BatchWorkPlanSubmitAdapter(List<Map<String, Object>> list,
			Context context) {
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
		holder = new ViewHolder();
		convertView = inflater.inflate(
				R.layout.batchworkplansubmit_listview_item, null);
		holder.seqid = convertView.findViewById(R.id.seqid);
		holder.workplan_detail_done_EditText = convertView
				.findViewById(R.id.workplan_detail_done_EditText);
		holder.requiresum = convertView
				.findViewById(R.id.requiresum);
		holder.item_sendcheckBox = convertView
				.findViewById(R.id.item_sendcheckBox);

		Map<String, Object> map = list.get(position);
		System.out.println("seqid=" + map.get("seqid"));
		holder.seqid.setText("工序号:" + map.get("seqid"));
		holder.workplan_detail_done_EditText.setText((CharSequence) map
				.get("done"));
		holder.requiresum.setText("需求数量:" + map.get("requiresum"));
		System.out.println("sendcheck=" + map.get("sendcheck"));
		holder.item_sendcheckBox.setChecked((Boolean) map.get("sendcheck"));
		holder.item_sendcheckBox
				.setOnCheckedChangeListener(new MyOnCheckedChangeListener(map));
		holder.workplan_detail_done_EditText
				.addTextChangedListener(new MyTextWatcher(
						holder.workplan_detail_done_EditText, map));
		return convertView;
	}

	public class MyOnCheckedChangeListener implements OnCheckedChangeListener {
		// private int position;
		private Map<String, Object> map;

		public MyOnCheckedChangeListener(Map<String, Object> map) {
			// this.position=position;
			System.out.println("check");
			this.map = map;
		}

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			map.put("sendcheck", arg1);

		}

	}

	// 监听editText的变化
	public class MyTextWatcher implements TextWatcher {
		private EditText editText;
		private Map<String, Object> map;

		public MyTextWatcher(EditText editText, Map<String, Object> map) {
			this.editText = editText;
			this.map = map;

		}

		@Override
		public void afterTextChanged(Editable arg0) {

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			//对比完成数量不能超过需求数量
			String content=editText.getText().toString().trim();
			if (content!=null&&!content.equals("")) {
				int requireSum=Integer.parseInt((String) map.get("requiresum"));
				int doneSum=Integer.parseInt(content);
				if (doneSum>requireSum) {
					Toast.makeText(context, "完成数量不能超过需求数量", Toast.LENGTH_SHORT).show();
					editText.setText( (CharSequence) map.get("requiresum"));
				}
				map.put("done", ""+doneSum);
			}else {
				map.put("done", "0");
			}
			
		}

	}

	public class ViewHolder {
		TextView seqid;
		EditText workplan_detail_done_EditText;
		TextView requiresum;
		CheckBox item_sendcheckBox;
	}

}
