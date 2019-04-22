package com.zhengyuan.easymessengerpro.activity.voip;

import android.content.Intent;
import android.os.Bundle;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.entity.GroupMemberEntity;
import com.zhengyuan.reslib.base.BaseActivity;
import com.zhengyuan.reslib.base.EventBusMessageEntity;

import java.util.ArrayList;

/**
 * 多人视频聊天界面
 * Created by gpsts on 17-6-19.
 */

public class MultiUserVideoCallActivity extends BaseActivity{

    private ArrayList<GroupMemberEntity> listData;

    @Override
    protected void onCreate(Bundle instance) {

        super.onCreate(instance);
        setContentView(R.layout.activity_multi_user_video_call);

        getData();
    }

    private void getData() {

        Intent intent = getIntent();
        listData = intent.getParcelableArrayListExtra(Constants.INTENT_TAG_MULTI_USER);
        if (listData == null)
            listData = new ArrayList<>();
    }

    @Override
    protected String getFiltTag() {
        return MultiUserVideoCallActivity.class.getName();
    }

    @Override
    protected void onHandlerEvent(EventBusMessageEntity event) {

    }
}
