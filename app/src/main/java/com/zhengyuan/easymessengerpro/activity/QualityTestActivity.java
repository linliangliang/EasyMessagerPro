package com.zhengyuan.easymessengerpro.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.ChatUtils;
import com.zhengyuan.easymessengerpro.R;

public class QualityTestActivity extends Activity {
    private TextView workplan_quality_workorderId_TextView;
    private TextView workplan_quality_workcenterID_TextView;
    private TextView workplan_quality_require_TextView;
    private TextView workplan_quality_done_TextView;
    private TextView workplan_quality_donedate_TextView;
    private TextView workplan_quality_people_TextView;
    private Spinner workplan_quality_operationSeqID_Spinner;
    private EditText workplan_quality_confirmdone_EditText;
    private Button workplan_quality_submit_Button;


    //	public static ChatManager qualityTestChatManager;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workpaln_qualitytest_layout);
//	qualityTestChatManager=XmppManager.getConnection().getChatManager();
        iniView();
        addDatas();
        workplan_quality_submit_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String confirm = workplan_quality_confirmdone_EditText.getText().toString().trim();
                if (confirm == null || confirm.equals("")) {
                    Toast.makeText(QualityTestActivity.this, "确认数量不能为空", Toast.LENGTH_LONG).show();
                } else {
                    int doneSum = Integer.parseInt(dones[pos]);
                    int confirmSum = Integer.parseInt(confirm);
                    if (confirmSum > doneSum) {
                        Toast.makeText(QualityTestActivity.this, "确认数量不能大于完成数量", Toast.LENGTH_LONG).show();
                    } else if (confirmSum < 0) {
                        Toast.makeText(QualityTestActivity.this, "确认数量不能小于0", Toast.LENGTH_LONG).show();
                    } else {
                        Element element = new Element("mybody");
                        element.addProperty("type", "requestQualityTestReport");
                        element.addProperty("confirmSum", confirm);
                        element.addProperty("scanning", scanning);
                        element.addProperty("operationSeqID", operationSeqIDs[pos]);
                        element.addProperty("donedate", donedate[pos]);
                        element.addProperty("donepeople", peoples[pos]);
//				Chat newchat0 = qualityTestChatManager.createChat("iqreceiver@"+XmppManager.getConnection().getServiceName(),null);//xxzx-gyj8860
//				try {
//					newchat0.sendMessage(element.toString());
//				} catch (XMPPException e) {
//					e.printStackTrace();
//				}
                        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString());
                        QualityTestActivity.this.finish();
                    }
                }
            }
        });
    }

    private void addDatas() {
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

        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, operationSeqIDs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workplan_quality_operationSeqID_Spinner.setAdapter(adapter);
        workplan_quality_operationSeqID_Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
                                       long arg3) {
                Log.i("qualityTest--->spinner", "" + position);
                pos = position;
                workplan_quality_workorderId_TextView.setText("工作单号:" + workOderId[position]);
                workplan_quality_workcenterID_TextView.setText("工作中心编号:" + workcenterIDs[position]);
                workplan_quality_require_TextView.setText("需求数量:" + requires[position]);
                workplan_quality_done_TextView.setText("完成数量:" + dones[position]);
                workplan_quality_donedate_TextView.setText("完成日期:" + donedate[position]);
                workplan_quality_people_TextView.setText("完成人编号:" + peoples[position]);
                workplan_quality_confirmdone_EditText.setText(dones[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private void iniView() {
        workplan_quality_workorderId_TextView = findViewById(R.id.workplan_quality_workorderId_TextView);
        workplan_quality_workcenterID_TextView = findViewById(R.id.workplan_quality_workcenterID_TextView);
        workplan_quality_require_TextView = findViewById(R.id.workplan_quality_require_TextView);
        workplan_quality_done_TextView = findViewById(R.id.workplan_quality_done_TextView);
        workplan_quality_donedate_TextView = findViewById(R.id.workplan_quality_donedate_TextView);
        workplan_quality_people_TextView = findViewById(R.id.workplan_quality_people_TextView);
        workplan_quality_operationSeqID_Spinner = findViewById(R.id.workplan_quality_operationSeqID_Spinner);
        workplan_quality_confirmdone_EditText = findViewById(R.id.workplan_quality_confirmdone_EditText);
        workplan_quality_submit_Button = findViewById(R.id.workplan_quality_submit_Button);

    }
}
