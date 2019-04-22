package com.zhengyuan.easymessengerpro.activity.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.ViewUtil;
import com.zhengyuan.baselib.xmpp.db.MessageDAO;
import com.zhengyuan.easymessengerpro.EMProApplication;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.activity.LoginActivity;
import com.zhengyuan.easymessengerpro.activity.MainActivity;
import com.zhengyuan.easymessengerpro.activity.ManagePluginsPopWindow;
import com.zhengyuan.easymessengerpro.adapter.MainViewPagerAdapter;
import com.zhengyuan.easymessengerpro.adapter.RightFragmentPagerAdapter;
import com.zhengyuan.easymessengerpro.util.GlideCircleTransform;
import com.zhengyuan.easymessengerpro.util.LogUtil;
import com.zhengyuan.easymessengerpro.widget.LittleDotsView;
import com.zhengyuan.easymessengerpro.widget.MainPageView;
import com.zhengyuan.easymessengerpro.xmpp.XmppManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 林亮
 */

public class RightFragment extends Fragment implements View.OnClickListener  {
    private final String TAG = "RightFragment";
    private ImageView userImage;//头像

    private TextView userIdTV;//用户id
    private ImageButton imageButton;//刷新连接的按钮
    private ImageButton pluginManageBtn;//插件安装卸载的按钮
    private ImageButton unrigisterBtn;//注销按钮
    /**
     * 底部点
     */
    private LittleDotsView littleDotsView;
    /**
     * Fregmant根布局
     */
    public static View root;
    String loginId = "";

    /**
     * 导航栏按钮的布局，用于添加响应事件
     */
    private LinearLayout mHome = null;
    private LinearLayout mPlugin = null;
    private LinearLayout mOther = null;
    /**
     * 导航栏按钮的图标，
     */
    private ImageView mHomeImageView = null;
    private ImageView mPluginImageView = null;
    private ImageView mOtherImageView = null;
    /**
     * 导航栏按钮的文字，
     */
    private TextView mHomeTextView = null;
    private TextView mPluginTextView = null;
    private TextView mOtherTextView = null;
    /**
     * ViewPager选中时候导航栏的图标
     */
    private int[] mNavigationIcon = new int[]{R.mipmap.home, R.mipmap.plugin, R.mipmap.other};
    /**
     * ViewPager选中没有时候导航栏的图标
     */
    private int[] mNavigationGrayIcon = new int[]{R.mipmap.home_gray, R.mipmap.plugin_gray, R.mipmap.other_gray};
    /**
     * ViewPager 的fragment List
     */
    private ViewPager mRightViewPager = null;
    private List<Fragment> fragmentList = null;
    private Fragment fragmentHome = null;
    private Fragment fragmentPlugin = null;
    private Fragment fragmentOther = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.rightlayout, null);
        initUserIcon();
        init();
        return root;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.LL_home:
                changeViewPager(0);
                break;
            case R.id.LL_plugin:
                changeViewPager(1);
                break;
            case R.id.LL_other:
                changeViewPager(2);
                break;
        }
    }

    private void init() {
        LogUtil.i(TAG, "RightFragment init:获取ViewPager对象---------------------------");
        mRightViewPager = root.findViewById(R.id.VP_rightViewPager);

        fragmentList = new ArrayList<Fragment>();
        fragmentHome = new RightSubHome();
        fragmentPlugin = new RightSubPlugin();
        fragmentOther = new RightSubOther();
        fragmentList.add(fragmentHome);
        fragmentList.add(fragmentPlugin);
        fragmentList.add(fragmentOther);

        RightFragmentPagerAdapter rightFragmentPagerAdapter = new RightFragmentPagerAdapter(getChildFragmentManager(), fragmentList);
        mRightViewPager.setAdapter(rightFragmentPagerAdapter);
        mRightViewPager.setCurrentItem(1);

        mHome = root.findViewById(R.id.LL_home);
        mHome.setOnClickListener(this);
        mPlugin = root.findViewById(R.id.LL_plugin);
        mPlugin.setOnClickListener(this);
        mOther = root.findViewById(R.id.LL_other);
        mOther.setOnClickListener(this);

        mHomeImageView = root.findViewById(R.id.IV_home);
        mPluginImageView = root.findViewById(R.id.IV_plugin);
        mOtherImageView = root.findViewById(R.id.IV_other);

        mHomeTextView = root.findViewById(R.id.TV_home);
        mPluginTextView = root.findViewById(R.id.TV_plugin);
        mOtherTextView = root.findViewById(R.id.TV_other);

    }

    public void initView() {
        pluginManageBtn = root.findViewById(R.id.plugin_manager);
        userIdTV = root.findViewById(R.id.username);
        imageButton = root.findViewById(R.id.reconnect);
        unrigisterBtn = root.findViewById(R.id.logout);
        refreshToolbarUserName();


        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //可以改为划出侧边栏，
            }
        });


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (XmppManager.getInstance().isAuthenticated()) {
                    Utils.showToast("已连接");
                } else {
                    XmppManager.getInstance().startReconnectionThread();
                }
            }
        });

        unrigisterBtn.setOnClickListener(new View.OnClickListener() {
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
                               /*
                               //这句话没什么屌用，还带来问题，干掉
                               Utils.createCircleProgressDialog(
                                        EMProApplicationDelegate.applicationContext,
                                        "注销中...");*/
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

        pluginManageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new ManagePluginsPopWindow(getActivity()).showAtBottom(arg0);//展示卸载安装插件的powindow
            }
        });
        LogUtil.i("TAG", "RightFragment 的子Fregment :获取插件列表信息    refreshLayout();");
        refreshLayout();//界面显示插件
    }

    /**
     * 刷新用户登录名信息
     */
    public void refreshToolbarUserName() {
        loginId = "";
        if (EMProApplicationDelegate.userInfo.nickName != null) {
            loginId += EMProApplicationDelegate.userInfo.nickName;
        }
        if (EMProApplicationDelegate.userInfo.getUserId() != null) {
            loginId += EMProApplicationDelegate.userInfo.getUserId();
        }
        userIdTV.setText(loginId);
    }

    /**
     * 初始化用户头像
     */
    public void initUserIcon() {
        String userId = EMProApplicationDelegate.userInfo.getUserId().toUpperCase();
        MessageDAO messageDAO = new MessageDAO();
        List<String> theavatars = messageDAO.qureyTheAvatarsByUserName(userId);

        if (userImage == null) {
            userImage = root.findViewById(R.id.user_image);
        }

        if (theavatars.size() == 0 || theavatars.get(0).equals("null")) {
            userImage.setImageResource(R.drawable.user_avater_default);
        } else {
            String taresult = theavatars.get(0);
            Glide
                    .with(getContext())
                    .load(Constants.DOWNLOAD_PATH + taresult)
                    .transform(new GlideCircleTransform(getContext()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.user_avater_default)
                    .into(userImage);
        }
    }

    public void refreshLayout() {
        /*ViewPager viewPager = root.findViewById(R.id.viewpager_id);

        // 使用list来进行分页保存
        List<View> viewList = new ArrayList<>();

        // 分页添加
        MainPageView page = new MainPageView(getActivity());
        int num = page.getPageNumber();
        viewList.clear();
        for (int i = 0; i < num; i++) {
            viewList.add(page);
        }
        // 适配器使用
        viewPager.setAdapter(new MainViewPagerAdapter(viewList));
        // 页面小白点
        littleDotsView = null;
        littleDotsView = root.findViewById(R.id.dot_num);
        LogUtil.i(TAG + ".initView()", "initView");
        littleDotsView.initDots(viewList.size());

        // 监听页面滑动的变化
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // 当前选中页
            public void onPageSelected(int position) {

                littleDotsView.selectDot(position);
            }

            // 页面滑动后
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            // 页面滑动状态
            public void onPageScrollStateChanged(int arg0) {

            }
        });*/
        ((RightSubPlugin) fragmentPlugin).refreshRightSubPlugin();
    }

    /**
     * 点击事件，修改viewPage中的Fragment
     *
     * @param position
     */
    private void changeViewPager(int position) {
        mRightViewPager.setCurrentItem(position);
        switch (position) {
            case 0:
                mHomeImageView.setImageResource(mNavigationIcon[0]);
                mHomeTextView.setTextColor(getResources().getColor(R.color.text_bg));
                mPluginImageView.setImageResource(mNavigationGrayIcon[1]);
                mPluginTextView.setTextColor(getResources().getColor(R.color.text_gray_bg));
                mOtherImageView.setImageResource(mNavigationGrayIcon[2]);
                mOtherTextView.setTextColor(getResources().getColor(R.color.text_gray_bg));
                break;
            case 1:
                mHomeImageView.setImageResource(mNavigationGrayIcon[0]);
                mHomeTextView.setTextColor(getResources().getColor(R.color.text_gray_bg));
                mPluginImageView.setImageResource(mNavigationIcon[1]);
                mPluginTextView.setTextColor(getResources().getColor(R.color.text_bg));
                mOtherImageView.setImageResource(mNavigationGrayIcon[2]);
                mOtherTextView.setTextColor(getResources().getColor(R.color.text_gray_bg));
                break;
            case 2:
                mHomeImageView.setImageResource(mNavigationGrayIcon[0]);
                mHomeTextView.setTextColor(getResources().getColor(R.color.text_gray_bg));
                mPluginImageView.setImageResource(mNavigationGrayIcon[1]);
                mPluginTextView.setTextColor(getResources().getColor(R.color.text_gray_bg));
                mOtherImageView.setImageResource(mNavigationIcon[2]);
                mOtherTextView.setTextColor(getResources().getColor(R.color.text_bg));
                break;
            default:
                mHomeImageView.setImageResource(mNavigationGrayIcon[0]);
                mHomeTextView.setTextColor(getResources().getColor(R.color.text_gray_bg));
                mPluginImageView.setImageResource(mNavigationIcon[1]);
                mPluginTextView.setTextColor(getResources().getColor(R.color.text_bg));
                mOtherImageView.setImageResource(mNavigationGrayIcon[2]);
                mOtherTextView.setTextColor(getResources().getColor(R.color.text_gray_bg));
                break;

        }
    }

}
