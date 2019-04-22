package com.zhengyuan.easymessengerpro.activity.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.entities.UserInfo;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.ViewUtil;
import com.zhengyuan.baselib.xmpp.db.MessageDAO;
import com.zhengyuan.easymessengerpro.EMProApplication;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.activity.LoginActivity;
import com.zhengyuan.easymessengerpro.activity.MainActivity;
import com.zhengyuan.easymessengerpro.activity.user.ChangeAvatarsActivity;
import com.zhengyuan.easymessengerpro.activity.user.ChangePasswordActivity;
import com.zhengyuan.easymessengerpro.adapter.LeftFragmentAdapter;
import com.zhengyuan.easymessengerpro.util.GlideCircleTransform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.qihoo360.loader2.PMF.getApplicationContext;
import static com.zhengyuan.baselib.constants.Constants.DOWNLOAD_PATH;

/**
 * Created by 林亮 on 2018/11/22
 */
public class LeftFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "LeftFragment";

    private ListView lv;
    private TextView mLogout_bt;
    private ImageView user_avatar;//用户头像
    private ArrayList<String[]> listData = new ArrayList<String[]>();

    public static UserInfo userInfo = EMProApplicationDelegate.userInfo;//全局的用户信息实体类
    View root;
    public static LeftFragmentAdapter leftFragmentAdapter;

    private enum ITEM_TAG {
        NAME, ID, DEPARTMENT, POSITION, MOBILE, CHANGE_AVATAR, CHANGE_PWD, USER_FINGERPRINT
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.leftlayout, null);
        lv = (ListView) root.findViewById(R.id.lv);
        mLogout_bt = root.findViewById(R.id.logout_bt);
        mLogout_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = ViewUtil.createNormalDialog(
                        getActivity(),
                        null,
                        "是否注销用户",
                        "确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.isLogout = true;
                                EMProApplication.logout();
                                EMProApplicationDelegate.userInfo.isAutoLogin = false;
                                EMProApplicationDelegate.sharedPrefHelper.saveBool(Constants.SHARED_PREF_IS_AUTO_LOGIN, false);

                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        },
                        "取消",
                        null
                );
                alertDialog.show();
            }
        });
        init(root);
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_avatar:
                Intent intent = new Intent(getApplicationContext(), ChangeAvatarsActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void initData() {
        //填充数据
        listData.clear();
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

    private void init(View root) {

        initData();
        initUserAvatar();//头像初始化
        //绑定监听器
        leftFragmentAdapter = new LeftFragmentAdapter(listData, getActivity());
        lv = (ListView) root.findViewById(R.id.lv);
        lv.setAdapter(leftFragmentAdapter);
        //绑定要跳转的activity
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> paren, View view, int position, long id) {
                Intent intent;
                LeftFragment.ITEM_TAG tag = LeftFragment.ITEM_TAG.values()[position];
                switch (tag) {
                    case CHANGE_AVATAR:
                        intent = new Intent(getActivity(), ChangeAvatarsActivity.class);
                        startActivity(intent);
                        break;
                    case CHANGE_PWD:
                        intent = new Intent(getActivity(), ChangePasswordActivity.class);
                        startActivity(intent);
                        break;
                    case USER_FINGERPRINT:
                        if (EMProApplicationDelegate.isEnableFingerPrint) {
                            EMProApplicationDelegate.isUseFingerPrint = !EMProApplicationDelegate.isUseFingerPrint;
                            EMProApplicationDelegate.sharedPrefHelper.saveBool(
                                    Constants.SHARED_PREF_IS_FINGER_PRINT, EMProApplicationDelegate.isUseFingerPrint
                            );
                            initData();
                            leftFragmentAdapter.notifyDataSetChanged();
                        } else {
                            Utils.showToast("权限未打开或当前设备不支持指纹");
                        }
                        break;
                }
            }
        });
        user_avatar.setOnClickListener(this);
    }

    private void initUserAvatar() {
        String userId = EMProApplicationDelegate.userInfo.getUserId().toUpperCase();
        MessageDAO messageDAO = new MessageDAO();
        List<String> theavatars = messageDAO.qureyTheAvatarsByUserName(userId);

        if (user_avatar == null) {
            user_avatar = root.findViewById(R.id.user_avatar);
        }

        if (theavatars.size() == 0 || theavatars.get(0).equals("null")) {
            //user_avatar.setImageResource(R.drawable.user_avater_default);
            Glide
                    .with(getContext())
                    .load(R.drawable.user_avater_default)
                    .transform(new GlideCircleTransform(getContext()))
                    .into(user_avatar);

        } else {
            String taresult = theavatars.get(0);
            Glide
                    .with(getContext())
                    .load(new File(DOWNLOAD_PATH + taresult))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transform(new GlideCircleTransform(getContext()))
                    .into(user_avatar);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //website = (setWebsite) context;//把activity向下转型成我们定义的接口，注意这里要强转
    }
}