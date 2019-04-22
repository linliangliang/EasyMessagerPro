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


public class BatchQualityTestAdapter extends BaseAdapter {
    private List<Map<String, Object>> list;
    private Context context;
    private LayoutInflater inflater = null;

    public BatchQualityTestAdapter(List<Map<String, Object>> list, Context context) {
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
        ViewHolder holder = new ViewHolder();
        convertView = inflater.inflate(R.layout.batch_qualitytest_item, null);
        holder.seqid = convertView.findViewById(R.id.workplan_batchqualitytest_seqid_TextView);
        holder.qualityTest_sure_EditText = convertView.findViewById(R.id.workplan_batchqualitytest_sure_EditText);
        holder.donesum = convertView.findViewById(R.id.workplan_batchqualitytest_done_TextView);
        holder.requiresum = convertView.findViewById(R.id.workplan_batchqualitytest_require_TextView);
        holder.item_sendcheckBox = convertView.findViewById(R.id.workplan_batchqualitytest_item_sendcheckBox);

        Map<String, Object> map = list.get(position);
        System.out.println("seqid=" + map.get("seqid"));
        holder.seqid.setText("工序号:" + map.get("seqid"));
        holder.qualityTest_sure_EditText.setText((CharSequence) map.get("sure"));
        holder.requiresum.setText("需求数量:" + map.get("requiresum"));
        holder.donesum.setText("完成数量:" + map.get("donesum"));
        System.out.println("sendcheck=" + map.get("sendcheck"));
        holder.item_sendcheckBox.setChecked((Boolean) map.get("sendcheck"));
        holder.item_sendcheckBox.setOnCheckedChangeListener(new MyOnCheckedChangeListener(map));
//			holder.item_sendcheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//				
//				@Override
//				public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
//					list.get(position).put("sendcheck", isChecked);
//				}
//			});
        /**
         * 监听文本框的变化
         * */
        holder.qualityTest_sure_EditText.addTextChangedListener(new MyTextWatcher(holder.qualityTest_sure_EditText, map));
        return convertView;
    }

    public class MyOnCheckedChangeListener implements OnCheckedChangeListener {
        //			private int  position;
        private Map<String, Object> map;

        public MyOnCheckedChangeListener(Map<String, Object> map) {
//		this.position=position;
            System.out.println("check");
            this.map = map;
        }

        @Override
        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            map.put("sendcheck", arg1);

        }

    }

    //监听editText的变化
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
            //对比完确定数量不能超过完成数量
            String content = editText.getText().toString().trim();
            if (content != null && !content.equals("")) {//文本框中有数据
                int doneSum = Integer.parseInt((String) map.get("donesum"));
                int sureSum = Integer.parseInt(content);
                if (sureSum > doneSum) {
                    Toast.makeText(context, "确定数量不能超过完成数量", Toast.LENGTH_SHORT).show();
                    editText.setText((CharSequence) map.get("donesum"));
                }
                map.put("sure", "" + sureSum);
            } else {//文本框里没有数据
                map.put("sure", "0");
            }
        }

    }

    public class ViewHolder {
        TextView seqid;
        EditText qualityTest_sure_EditText;
        TextView requiresum;
        TextView donesum;
        CheckBox item_sendcheckBox;
    }

}
