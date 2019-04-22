package com.zhengyuan.easymessengerpro.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.zhengyuan.baselib.http.OkHttpUtil;
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.easymessengerpro.RePluginHelper;
import com.zhengyuan.easymessengerpro.entity.updataVersionEntity;
import com.zhengyuan.easymessengerpro.network.DataObtainer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogRecord;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * @author 林亮
 * @description:
 * @date :2019/2/25 13:36
 */

public class UpdateAppUtil {

    public static updataVersionEntity info = new updataVersionEntity();

    /**
     * 获取当前apk的版本号 currentVersionCode
     *
     * @param ctx
     * @return
     */
    public static int getAPPLocalVersion(Context ctx) {
        int currentVersionCode = 0;
        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            String appVersionName = info.versionName; // 版本名
            currentVersionCode = info.versionCode; // 版本号
            LogUtil.i("appVersionName", "" + appVersionName);
            LogUtil.i("currentVersionCode", "" + currentVersionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return currentVersionCode;
    }


}

