package com.zhengyuan.easymessengerpro.activity;

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
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.ChatUtils;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.adapter.BatchQualityTestAdapter;
import com.zhengyuan.easymessengerpro.network.MainPageChatter;
import com.zhengyuan.reslib.base.BaseActivity;
import com.zhengyuan.reslib.base.EventBusMessageEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchQualityTestActivity extends BaseActivity {
    private ListView batch_workplan_submit_listview;
    private BatchQualityTestAdapter adapter;
    private TextView batch_workplan_submit_orderid_TextView;
    private CheckBox batch_workplan_submit_allcheck_CheckBox;
    private Button batch_workplan_submit_sendButton;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    //	public static ChatManager batchQualityTestActivitycChatManager;
    String scanning;
    String[] workOderId;
    String[] workcenterIDs;
    String[] requires;
    String[] dones;
    String[] donedate;
    String[] peoples;
    String[] operationSeqIDs;
    int pos;

    @Override
    protected void onStart() {
        StaticVariable.inBatchQualityTestActivity = true;
        super.onStart();
    }

    @Override
    protected void onRestart() {
        StaticVariable.inBatchQualityTestActivity = true;
        super.onRestart();
    }

    @Override
    protected void onPause() {
        StaticVariable.inBatchQualityTestActivity = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        StaticVariable.inBatchQualityTestActivity = false;
        super.onDestroy();
        Constants.contexts.remove(Constants.contexts.size() - 1);
    }

    @Override
    protected void onResume() {
        StaticVariable.inBatchQualityTestActivity = true;
        super.onResume();
    }

    @Override
    protected void onStop() {
        StaticVariable.inBatchQualityTestActivity = false;
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_batch_workplan_submit);// 暂时与班组人员提交界面一致
        StaticVariable.inBatchQualityTestActivity = true;
        Constants.contexts.add(this);
//		batchQualityTestActivitycChatManager=XmppManager.getConnection().getChatManager();
        initView();
        addListData();
        adapter = new BatchQualityTestAdapter(list, BatchQualityTestActivity.this);
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
            public void onClick(View arg0) {//批量发送
                int sureSum = 0;
                Element element = new Element("mybody");
                element.addProperty("type", "requestBatchQualityTestReport");
                for (int i = 0; i < list.size(); i++) {
                    if ((Boolean) list.get(i).get("sendcheck")) {//质检员可以提交确定数量为0的数据
                        element.addProperty("confirmSum" + sureSum, "" + list.get(i).get("sure"));//只让质检员修改确定数量
                        element.addProperty("scanning" + sureSum, scanning);
                        element.addProperty("operationSeqID" + sureSum, operationSeqIDs[i]);
                        element.addProperty("donedate" + sureSum, donedate[i]);
                        element.addProperty("donepeople" + sureSum, peoples[i]);
                        sureSum++;
                    }
                }
                element.addProperty("sureSum", "" + sureSum);
                if (sureSum == 0) {
                    Toast.makeText(BatchQualityTestActivity.this, "请至少选择一项工序发送", Toast.LENGTH_LONG).show();
                } else {
                    ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString());
//					Chat chat=batchQualityTestActivitycChatManager.createChat("iqreceiver@"+XmppManager.getConnection().getServiceName(),null);
//					try {
//						chat.sendMessage(element.toString());
//					} catch (XMPPException e) {
//						e.printStackTrace();
//					}
                    BatchQualityTestActivity.this.finish();
                }
            }
        });
        getData();
    }

    @Override
    protected String getFiltTag() {
        return BatchQualityTestActivity.class.getName();
    }

    @Override
    protected void onHandlerEvent(EventBusMessageEntity entity) {

    }

    private void getData() {

        Intent intent = getIntent();
        String scanResult = intent.getStringExtra("scanResult");

        Utils.createCircleProgressDialog(this, "正在获取班组工作单...");
        MainPageChatter.INSTANCE.getWorkListNumber(scanResult,
                new NetworkCallbacks.SimpleDataCallback() {
                    @Override
                    public void onFinish(boolean isSuccess, String msg, Object data) {


                    }
                });

    }

    private void addListData() {
//		Map<String, Object> map=new HashMap<String, Object>();
//		map.put("seqid", "001");
//		map.put("donesum", "10");
//		map.put("requiresum", "15");
//		map.put("sure", "10");
//		map.put("sendcheck", false);
//		list.add(map);


        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        scanning = bundle.getString("scanning");
        String data = bundle.getString("allDetailDatas");
        Log.i("allDetailDatas", data);
        String[] detailDataLines = data.split(";");
        workOderId = new String[detailDataLines.length];
        workcenterIDs = new String[detailDataLines.length];
        requires = new String[detailDataLines.length];
        dones = new String[detailDataLines.length];
        donedate = new String[detailDataLines.length];
        peoples = new String[detailDataLines.length];
        operationSeqIDs = new String[detailDataLines.length];
        for (int i = 0; i < detailDataLines.length; i++) {
            /**
             * WE_WorkOrderID
             * WE_LotQty
             * WE_OperationSeqID
             * WE_WorkCenterID
             * WE_OperationEmpID
             * WE_CompleteQty
             * WE_CompleteDate
             * */
            String[] detailDataLine = detailDataLines[i].split("\\+");
            Log.i("workplandetaildatas-detaildataline", "" + detailDataLine.length);
            workOderId[i] = detailDataLine[0];
            requires[i] = detailDataLine[1];
            operationSeqIDs[i] = detailDataLine[2];
            workcenterIDs[i] = detailDataLine[3];
            peoples[i] = detailDataLine[4];
            dones[i] = detailDataLine[5];
            donedate[i] = detailDataLine[6];

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("seqid", operationSeqIDs[i]);
            map.put("donesum", dones[i]);
            map.put("requiresum", requires[i]);
            map.put("sure", dones[i]);//确定完成数量默认是员工完成数量
            map.put("sendcheck", false);
            list.add(map);
        }
        batch_workplan_submit_orderid_TextView.setText("工作单号:" + workOderId[0]);
    }

    private void initView() {
        batch_workplan_submit_orderid_TextView = findViewById(R.id.batch_workplan_submit_orderid_TextView);
        batch_workplan_submit_sendButton = findViewById(R.id.batch_workplan_submit_sendButton);
        batch_workplan_submit_listview = findViewById(R.id.batch_workplan_submit_listview);
        batch_workplan_submit_allcheck_CheckBox = findViewById(R.id.batch_workplan_submit_allcheck_CheckBox);
    }
}
