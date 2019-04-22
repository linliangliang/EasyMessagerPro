package com.zhengyuan.baselib.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.http.base.HttpError;
import com.zhengyuan.baselib.http.base.HttpResult;
import com.zhengyuan.baselib.utils.Utils;

/**
 * Created by HanZhang on 2017/3/8.
 * 使用okhttp工具进行网络交互
 * <p>
 * 使用枚举中的单例模式
 */

public enum OkHttpUtil {

    INSTANCE;

    public static final int CONNECT_TIMEOUT = 10;
    public static final int READ_TIMEOUT = 10;

    private static final String TAG = "HttpHelper";

    /*private OkHttpClient client = new OkHttpClient.Builder().
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS).
            readTimeout(READ_TIMEOUT,TimeUnit.SECONDS).
            build();*/
    private OkHttpClient client = new OkHttpClient.Builder().
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS).
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).
            build();

    public OkHttpClient getClient() {
        return client;
    }

    /**
     * 明确指出网络请求是否成功，并将result的code以及message换成业务逻辑结果
     */
    public boolean isRequestSuccess(HttpResult httpResult) {

        if (httpResult == null)
            return false;

        String originCode = httpResult.getCode();

        return originCode.equals(HttpResult.CODE_SUCCESS);
    }

    /**
     * 同步请求，注意进行线程操作
     */
    public HttpResult doRequest(Request request) {
        HttpResult httpResult = new HttpResult();
        try {
            Response response = client.newCall(request).execute();
            httpResult.setMsg(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            httpResult.setCode(HttpResult.CODE_FAIL);
            httpResult.setMsg(e.getMessage());
        }
        return httpResult;
    }

    /**
     * 同步Get方法，注意进行线程操作
     */
    public HttpResult doGetRequest(String url, Map<String, String> params) {
        return doRequest(buildGetRequest(url, params));
    }

    /**
     * 同步Post方法，注意进行线程操作
     */
    public HttpResult doPostRequest(String url, Map<String, String> params) {
        return doRequest(buildPostRequest(url, params));
    }

    public interface HttpCallback {
        void onCallback(HttpResult respContent, HttpError error);
    }

    /**
     * 异步进行请求
     *
     * @param request  Get 或者 Post 请求
     * @param callback 异步回调，但是在子线程上
     */
    public void doRequestInBackground(Request request, final HttpCallback callback) {
        // check network available
        if (!isNetworkAvailable()) {
            callback.onCallback(null, new HttpError(HttpResult.CODE_FAIL, "当前网络不可用"));
            return;
        }

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                HttpError error = new HttpError(HttpResult.CODE_FAIL, e.getMessage());
                callback.onCallback(null, error);
            }

            @Override
            public void onResponse(Call call, Response response) {
                HttpError error = null;
                HttpResult httpResult = new HttpResult();
                try {
                    String resultContent = response.body().string();
                    //TODO
                    resultContent = "{" + "data:" + resultContent + "}";
//                    Log.e(TAG, response.toString()); //debug
                    Log.e(TAG, resultContent); //debug
                    JSONObject rawObject = new JSONObject(resultContent);
                    if (!rawObject.isNull("error")) {
                        String errorMsg = rawObject.getString("error");
                        error = new HttpError(HttpResult.CODE_FAIL, errorMsg);
                        httpResult.setMsg(HttpResult.MSG_SUCCESS);
                        httpResult.setCode(HttpResult.CODE_FAIL);
                    } else {
                        String data = resultContent;
                        httpResult.setData(data);
                        httpResult.setCode(HttpResult.CODE_SUCCESS);
                        httpResult.setMsg(HttpResult.MSG_SUCCESS);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    error = new HttpError(HttpResult.CODE_FAIL, e.getMessage());
                    httpResult.setCode(HttpResult.CODE_FAIL);
                    httpResult.setMsg(HttpResult.MSG_FAIL);
                }
                callback.onCallback(httpResult, error);
            }
        });
    }

    /**
     * 异步Get方法
     *
     * @param url      网络请求domain
     * @param params   请求参数
     * @param callback 异步回调，但执行在子线程，注意callback中的result只是网络请求成功了，与业务无关，真正的业务结果在result.getRecycleData()
     */
    public void doGetRequestInBackground(String url, Map<String, String> params, HttpCallback callback) {
        doRequestInBackground(buildGetRequest(url, params), callback);
    }

    /**
     * 异步Post方法
     *
     * @param url      网络请求domain
     * @param params   请求参数
     * @param callback 异步回调，但执行在子线程，注意callback中的result只是网络请求成功了，与业务无关，真正的业务结果在result.getRecycleData()
     */
    public void doPostRequestInBackground(String url, Map<String, String> params, HttpCallback callback) {
        doRequestInBackground(buildPostRequest(url, params), callback);
    }

    /**
     * 上传图片、文件、数组数据时使用，还没有封装好，未测试，暂时不要使用
     *
     * @param url
     * @param params
     * @param picPaths
     * @param arrays
     * @param fourParas
     * @param callback
     * @deprecated 还没有封装好，未测试，暂时不要使用
     */
    public void doDisPicPostRequestInBackground(String url, Map<String, String> params,
                                                 ArrayList<String> picPaths, JSONArray[] arrays,
                                                 String[][] fourParas,
                                                 HttpCallback callback) {
        doRequestInBackground(buildFilePostRequest(url, params, picPaths, arrays, fourParas), callback);
    }

    // 上传图片、文件、数组数据时使用
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Request buildFilePostRequest(String url, Map<String, String> params,
                                         ArrayList<String> picPaths, JSONArray[] arrays,
                                         String[][] fourParas) {

        if (params == null) {
            params = new HashMap<>(0);
        }
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        //遍历map中所有参数到builder
        for (String key : params.keySet()) {
            builder.addFormDataPart(key, params.get(key));
        }

        for (int i = 0; i < fourParas[0].length; i++) {
            builder.addFormDataPart("fileFlag", fourParas[0][i]);
            builder.addFormDataPart("urls", fourParas[1][i]);
            builder.addFormDataPart("ids", fourParas[2][i]);
            builder.addFormDataPart("names", fourParas[3][i]);
        }
        //遍历paths中所有图片绝对路径到builder，并约定key如“upload”作为后台接受多张图片的key
        for (String path : picPaths) {
            builder.addFormDataPart("upload", new File(path).getName(),
                    RequestBody.create(MediaType.parse("image/png"), new File(path)));
        }

        Log.d("OtherInfo", builder.toString());
//        if (arrays.length > 0) {
//            for (JSONArray array : arrays) {
//                builder.addPart(RequestBody.create(JSON, array.toString()));
//            }
//        }

        //构建请求体
        RequestBody requestBody = builder.build();
        return new Request.Builder().url(url).post(requestBody).build();
    }

    private Request buildPostRequest(String url, Map<String, String> params) {
        if (params == null) {
            params = new HashMap<>(0);
        }
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody = builder.build();
        return new Request.Builder().url(url).post(requestBody).build();
    }

    private Request buildGetRequest(String url, Map<String, String> params) {
        return new Request.Builder().url(url + mapToUrlParamsStr(params)).build();
    }

    private String mapToUrlParamsStr(Map<String, String> params) {
        if (params == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(entry.getValue(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * @param url 下载连接
     * @param saveDir 储存下载文件的SDCard目录
     * @param listener 下载监听
     */
    public void download(final String url, final String name, final OnDownloadListener listener) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                listener.onDownloadFailed();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(name);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                        listener.onDownloading(progress);
                    }
                    fos.flush();
                    // 下载完成
                    listener.onDownloadSuccess();
                } catch (Exception e) {
                    listener.onDownloadFailed();
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess();

        /**
         * @param progress
         * 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed();
    }

    /**
     * 判断当前网络是否正常
     */
    public boolean isNetworkAvailable() {

        final Context context = EMProApplicationDelegate.applicationContext;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return mNetworkInfo != null && mNetworkInfo.isAvailable();
    }

    /**
     * 解决中文乱码的post请求
     *
     * @author haoyunjixiang
     * @time 2017/4/13 21:01
     **/
    private StringBuffer getRequestData(Map<String, String> params) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stringBuffer;
    }
}