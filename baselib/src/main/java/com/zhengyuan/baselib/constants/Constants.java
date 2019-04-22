/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhengyuan.baselib.constants;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;

/**
 * 程序中用到的一些静态常量
 *
 * @author linliang
 */
public class Constants {

    public static final String XMPP_IP = "59.175.173.136";
    public static final int XMPP_HOST = 5222;

    /*测试服务器的ip以及端口号*/
    /*public static final String XMPP_IP = "http://172.16.200.251";
    public static final int XMPP_HOST = 5222;
*/
    public static final String XMPP_RESOURCE_NAME = "ZYDQPhone";

    public static String APP_DIRECTORY = "EasyMessenger";
    public static String LOG_DIRECTORY = "log";
    public static String PLUGIN_DIRECTORY = "plugins";
    public static String APK_DIRECTORY = "emapk";
    public static String CACHE_DIRECTORY = "cache";
    public static String DOWNLOAD_DIRRECTORY = "myxmpp";
    public static String DOWNLOAD_PATH = Environment.getExternalStorageDirectory()
            .toString() + "/myxmpp/downloadfile/";

    public static final String INTENT_TAG_MULTI_USER = "MULTI_USER";

    public static String CHAT_TO_USER = "iqreceiver";
    public static String SERVER_NAME = "xxzx";

    public static final String DOWNLOAD_IMAGE_URL = "http://" + XMPP_IP + ":9080/images/";
    public static final String UPLOAD_IMAGE_URL = "http://" + XMPP_IP + ":9080/" +
            "EasyMessagerHttp//UploadImageServlet";
    public static final String UploadBaseuUrl = "http://" + XMPP_IP + ":9080/upload/upload/execute.do";
    public static final String DownLoadBaseUrl = "http://" + XMPP_IP + ":9080";

    public static final String PLUGIN_BASE_URL = "/plugins/";
    public static final String PLUGIN_LIST_NAME = "PluginList.json";
    public static final String PLUGIN_LIST_URL = DownLoadBaseUrl + PLUGIN_BASE_URL + PLUGIN_LIST_NAME;
    public static final String HTTP_URL = "http://" + XMPP_IP + ":9080/";

    // SharedPreference保存使用的tag
    public static final String SHARED_PREF_IS_FINGER_PRINT = "IS_FINGER_PRINT";
    public static String SHARED_PREF_IS_AUTO_LOGIN = "IS_AUTO_LOGIN";
    public static final String XMPP_USERNAME = "XMPP_USERNAME";
    public static final String XMPP_PASSWORD = "XMPP_PASSWORD";
    public static final String DEVICE_ID = "DEVICE_ID";

    public static final int EM_QRCODE_REQUEST_CODE = 0;
    public static final int EM_QRCODE_RESULT_CODE = 1;
    public static final String EM_QRCODE_RESULT_TAG = "QRCODE";

    public static List<Context> contexts = new ArrayList<>();
}
