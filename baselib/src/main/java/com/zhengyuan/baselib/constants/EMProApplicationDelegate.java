package com.zhengyuan.baselib.constants;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.telephony.TelephonyManager;

import com.zhengyuan.baselib.entities.UserInfo;
import com.zhengyuan.baselib.utils.SharedPrefHelper;

import java.util.HashMap;

/**
 * Created by zy on 2017/11/10.
 * 给插件用的代理
 */

public class EMProApplicationDelegate {

    // TODO 未处理同步
    public static SharedPrefHelper sharedPrefHelper;

    // 名字, userId
    public static HashMap<String, String> allUserInfo = new HashMap<>();

    public static UserInfo userInfo = new UserInfo();
    public static boolean isEnableFingerPrint = false;
    public static boolean isUseFingerPrint = false;
    public static Context applicationContext;

    public static FingerprintManagerCompat fingerprintManager;
    public static TelephonyManager telephonyManager;
    public static String deviceId;

    public static void getDeviceId() {

        fingerprintManager = FingerprintManagerCompat.from(applicationContext);
        isEnableFingerPrint = fingerprintManager.isHardwareDetected();

        // 获取deviceId
        telephonyManager = (TelephonyManager)
                applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();
    }
}
