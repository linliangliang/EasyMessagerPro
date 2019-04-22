package com.zhengyuan.easymessengerpro.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.activity.plugin.InstallPluginActivity;
import com.zhengyuan.easymessengerpro.activity.plugin.UninstallPluginActivity;

/**
 * Created by 林亮 on 2018/11/22
 */

/**
 * 弹窗视图
 */
public class ManagePluginsPopWindow extends PopupWindow implements View.OnClickListener {
    private Context context;
    private View ll_install, ll_uninstall;

    public ManagePluginsPopWindow(Context context) {
        super(context);
        this.context = context;
        initalize();
    }

    private void initalize() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.popwindow_confirm_dialog, null);
        ll_install = view.findViewById(R.id.ll_install);
        ll_install.setOnClickListener(this);
        //安装
        ll_uninstall = view.findViewById(R.id.ll_uninstall);
        // 卸载
        ll_uninstall.setOnClickListener(this);
        setContentView(view);
        initWindow();
    }

    private void initWindow() {
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        this.setWidth((int) (d.widthPixels * 0.35));
        this.setHeight(ActionBar.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        backgroundAlpha((Activity) context, 0.8f);//0.0-1.0
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha((Activity) context, 1f);
            }
        });
    }

    /**
     * @param context
     * @param bgAlpha 设置添加屏幕的背景透明度
     */
    public void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

    /**
     * @param view 设置显示popwindow的位置，并显示。可以使用，还没全面测试
     */
    public void showAtBottom(View view) {
        //弹窗位置设置
        //相对某个控件的位置，有偏移;xoff表示x轴的偏移，正值表示向右，负值表示向左；yoff表示相对y轴的偏移，正值是向下，负值是向上；
        showAsDropDown(view, Math.abs((view.getWidth() - getWidth()) / 2), 10);
        //相对于父控件的位置（例如正中央Gravity.CENTER，下方Gravity.BOTTOM等），可以设置偏移或无偏移
        //showAtLocation(view, Gravity.BOTTOM | Gravity.RIGHT, 10, 50);
    }

    /**
     * @param view 重写onClick函数，点击显示安装/卸载的activity,点击后隐藏popwindow
     */
    @Override
    public void onClick(View view) {
        if (this != null && this.isShowing()) {//点击完成后，隐藏popwindow
            this.dismiss();
        }
        Intent intent = null;
        switch (view.getId()) {
            case R.id.ll_install:
                intent = new Intent(context, InstallPluginActivity.class);
                context.startActivity(intent);
                break;
            case R.id.ll_uninstall:
                intent = new Intent(context, UninstallPluginActivity.class);
                context.startActivity(intent);
                break;
            default:
                break;
        }

    }
}
