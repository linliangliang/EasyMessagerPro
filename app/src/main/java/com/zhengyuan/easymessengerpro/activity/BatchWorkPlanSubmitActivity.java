package com.zhengyuan.easymessengerpro.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.StaticVariable;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.ChatUtils;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.adapter.BatchWorkPlanSubmitAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在adapter控制完成数量不能大于需求数量
 */
public class BatchWorkPlanSubmitActivity extends Activity {
    private ListView batch_workplan_submit_listview;
    private BatchWorkPlanSubmitAdapter adapter;
    private TextView batch_workplan_submit_orderid_TextView;
    private CheckBox batch_workplan_submit_allcheck_CheckBox;
    private Button batch_workplan_submit_sendButton;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    //	public static ChatManager batchWorkPlanSubmitChatManager;
    String[] workOderId;
    String[] operationSeqIDs;
    String[] workCenterNames;
    String[] tools;
    String[] requireTexts;
    String[] workcenterIDs;
    String[] processs;
    String[] requires;
    String[] descriptions;
    String[] allRequireNums;
    //通过条码扫描跳转能得到scanning的值
    String scanning;

    @Override
    protected void onStart() {
        StaticVariable.inBatchWorkPlanSubmitActivity = true;
//		StaticVariable.handler=this.handler;
        super.onStart();
    }

    @Override
    protected void onRestart() {
        StaticVariable.inBatchWorkPlanSubmitActivity = true;
        super.onRestart();
    }

    @Override
    protected void onPause() {
        StaticVariable.inBatchWorkPlanSubmitActivity = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        StaticVariable.inBatchWorkPlanSubmitActivity = false;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        StaticVariable.inBatchWorkPlanSubmitActivity = true;
        super.onResume();
    }

    @Override
    protected void onStop() {
        StaticVariable.inBatchWorkPlanSubmitActivity = false;
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_batch_workplan_submit);
        Constants.contexts.add(this);
        StaticVariable.inBatchWorkPlanSubmitActivity = true;
//	batchWorkPlanSubmitChatManager=XmppManager.getConnection().getChatManager();
        initView();
        addListData();

        adapter = new BatchWorkPlanSubmitAdapter(list, this);
        batch_workplan_submit_listview.setAdapter(adapter);
        batch_workplan_submit_allcheck_CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).put("sendcheck", isChecked);
                }
                adapter.notifyDataSetChanged();
            }
        });
        batch_workplan_submit_sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                int checkSum = 0;
                Element element = new Element("mybody");
                element.addProperty("type", "requestBatchWorkPlanSubmit");
                for (int i = 0; i < list.size(); i++) {
                    if ((Boolean) list.get(i).get("sendcheck") && !list.get(i).get("done").equals("0")) {//子项被标记要发送
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        String donetime = sdf.format(new Date().getTime());
                        element.addProperty("scanning" + checkSum, scanning);//作业单号
                        element.addProperty("workorderid" + checkSum, workOderId[i]);//子项工作单号
                        element.addProperty("allrequirenum" + checkSum, allRequireNums[i]);//作业数量
                        element.addProperty("operationseqid" + checkSum, operationSeqIDs[i]);//工作中心序号
                        element.addProperty("workcenterid" + checkSum, workcenterIDs[i]);//工作中心编号
                        element.addProperty("donesum" + checkSum, "" + list.get(i).get("done"));//完成数量
                        element.addProperty("donetime" + checkSum, donetime);//时间
                        checkSum++;
                    }
                }

                if (checkSum == 0) {
                    Toast.makeText(BatchWorkPlanSubmitActivity.this, "请至少选择一项工序发送", Toast.LENGTH_LONG).show();
                } else {
                    element.addProperty("workSum", "" + checkSum);
//				Chat chat=batchWorkPlanSubmitChatManager.createChat("iqreceiver@"+XmppManager.getConnection().getServiceName(),null);
//				try {
//					chat.sendMessage(element.toString());
//				} catch (XMPPException e) {
//					e.printStackTrace();
//				}
                    ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString());
                    BatchWorkPlanSubmitActivity.this.finish();
                }

            }
        });
    }

    private void addListData() {
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        scanning = bundle.getString("scanning");
        String data = bundle.getString("allDetailDatas");
        Log.i("allDetailDatas", data);
        String[] detailDataLines = data.split("@end");
        workOderId = new String[detailDataLines.length];
        operationSeqIDs = new String[detailDataLines.length];
        workCenterNames = new String[detailDataLines.length];
        tools = new String[detailDataLines.length];
        requireTexts = new String[detailDataLines.length];
        workcenterIDs = new String[detailDataLines.length];
        processs = new String[detailDataLines.length];
        requires = new String[detailDataLines.length];
        descriptions = new String[detailDataLines.length];
        allRequireNums = new String[detailDataLines.length];
        for (int i = 0; i < detailDataLines.length; i++) {
            String[] detailDataLine = detailDataLines[i].split("@@@");
            Log.i("workplandetaildatas-detaildataline", "" + detailDataLine.length);
            workOderId[i] = detailDataLine[0];
            operationSeqIDs[i] = detailDataLine[1];
            workcenterIDs[i] = detailDataLine[2];
            workCenterNames[i] = detailDataLine[3];
            descriptions[i] = detailDataLine[4];
            requireTexts[i] = detailDataLine[5];
            processs[i] = detailDataLine[6];
            requires[i] = detailDataLine[7];
            tools[i] = detailDataLine[8];
            allRequireNums[i] = detailDataLine[9];
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("seqid", operationSeqIDs[i]);
            map.put("done", requireTexts[i]);
            map.put("requiresum", requireTexts[i]);
            map.put("sendcheck", false);
            list.add(map);
        }
        batch_workplan_submit_orderid_TextView.setText("工作单号：" + workOderId[0]);
    }

    private void initView() {
        batch_workplan_submit_orderid_TextView = findViewById(R.id.batch_workplan_submit_orderid_TextView);
        batch_workplan_submit_sendButton = findViewById(R.id.batch_workplan_submit_sendButton);
        batch_workplan_submit_listview = findViewById(R.id.batch_workplan_submit_listview);
        batch_workplan_submit_allcheck_CheckBox = findViewById(R.id.batch_workplan_submit_allcheck_CheckBox);
    }
}
