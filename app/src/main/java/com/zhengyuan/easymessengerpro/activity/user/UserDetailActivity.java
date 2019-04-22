package com.zhengyuan.easymessengerpro.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.entities.UserInfo;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.easymessengerpro.activity.BaseSimpleListActivity;

/**
 * Created by zy on 2017/11/4.
 * 用户详情界面
 */

public class UserDetailActivity extends BaseSimpleListActivity {

    public void onCreate(Bundle onSavedState) {
        super.onCreate(onSavedState);
    }

    @Override
    protected String getTitleName() {
        return "账户管理";
    }

    @Override
    protected void getData() {

        listData.clear();//listData-继承自父类
        UserInfo userInfo = EMProApplicationDelegate.userInfo;

        listData.add(new String[]{"姓名: " + userInfo.nickName, ""});
        listData.add(new String[]{"工号: " + userInfo.getUserId(), ""});
        listData.add(new String[]{"部门: " + userInfo.department, ""});
        listData.add(new String[]{"职位: " + userInfo.position, ""});
        listData.add(new String[]{"手机: " + userInfo.mobile, ""});
        listData.add(new String[]{"修改头像", "点击修改"});
        listData.add(new String[]{"更改密码", "点击修改"});
        listData.add(new String[]{"使用指纹登录: " +
                (EMProApplicationDelegate.isUseFingerPrint ? "是" : "否"),
                "点击切换"});
    }

    private enum ITEM_TAG {
        NAME, ID, DEPARTMENT, POSITION, MOBILE, CHANGE_AVATAR, CHANGE_PWD, USER_FINGERPRINT
    }

    @Override
    protected AdapterView.OnItemClickListener getOnItemClickListener() {

        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent;
                ITEM_TAG tag = ITEM_TAG.values()[position];

                switch (tag) {

                    case NAME:

                        break;
                    case CHANGE_AVATAR:
                        intent = new Intent(UserDetailActivity.this, ChangeAvatarsActivity.class);
                        startActivity(intent);
                        break;
                    case CHANGE_PWD:
                        intent = new Intent(UserDetailActivity.this, ChangePasswordActivity.class);
                        startActivity(intent);
                        break;
                    case USER_FINGERPRINT:
                        if (EMProApplicationDelegate.isEnableFingerPrint) {

                            EMProApplicationDelegate.isUseFingerPrint = !EMProApplicationDelegate.isUseFingerPrint;
                            EMProApplicationDelegate.sharedPrefHelper.saveBool(
                                    Constants.SHARED_PREF_IS_FINGER_PRINT, EMProApplicationDelegate.isUseFingerPrint
                            );
                            getData();
                            simpleAdapter.notifyDataSetChanged();
                        } else {
                            Utils.showToast("权限未打开或当前设备不支持指纹");
                        }
                        break;
                }
            }
        };
    }
}
