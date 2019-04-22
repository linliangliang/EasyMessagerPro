package com.zhengyuan.easymessengerpro.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.adapter.BaseSimpleListAdapter;

import java.util.ArrayList;

/**
 * Created by zy on 2017/11/4.
 */

public abstract class BaseSimpleListActivity extends Activity {

    private ImageButton backBtn, downBtn;
    private TextView textView;

    protected ListView listView;
    protected BaseSimpleListAdapter simpleAdapter;

    public void onCreate(Bundle onSavedState) {
        super.onCreate(onSavedState);

        setContentView(R.layout.activity_base_simple_list);

        listView = findViewById(R.id.listview);

        initView();
    }

    protected void initView() {

        getData();
        simpleAdapter = new BaseSimpleListAdapter(listData, this);

        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(getOnItemClickListener());

        backBtn = findViewById(R.id.title_back_btn);
        downBtn = findViewById(R.id.main_menu_bn);
        textView = findViewById(R.id.title_tv);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        downBtn.setVisibility(View.GONE);

        textView.setText(getTitleName());
    }

    protected abstract String getTitleName();

    protected ArrayList<String[] > listData = new ArrayList<>();
    protected abstract void getData();

    protected abstract AdapterView.OnItemClickListener getOnItemClickListener();
}
