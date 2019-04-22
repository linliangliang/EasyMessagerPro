package com.zhengyuan.easymessengerpro.network;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.ChatCallbackTag;
import com.zhengyuan.baselib.xmpp.ChatUtils;
import com.zhengyuan.easymessengerpro.activity.BatchQualityTestActivity;
import com.zhengyuan.easymessengerpro.activity.BatchWorkPlanSubmitActivity;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by gpsts on 17-6-16.
 */

public enum MainPageChatter {

    INSTANCE;

    private final String LOG_TAG = "MainPageChatter";

    /**
     * 从服务器获取全部插件列表
     *
     * @param callback 回调函数
     * @param userId  请求信息的用户的id
     */
    public void getPluginList(final NetworkCallbacks.SimpleDataCallback callback, String userId) {

        Element element = new Element();
        element.addProperty("type", "requestGetPluginListInfo");
        element.addProperty("userId", userId);

        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(),
                "returnGetPluginListInfo", new NetworkCallbacks.ChatMessageListener() {
                    @Override
                    public void gotMessage(Element element, Message message, Chat chat, boolean isSuccess) {

                        callback.onFinish(isSuccess, element.getBody(), element);
                    }
                });
    }

    /**
     * @author kangkang
     *
     */
    public void getPermission(final NetworkCallbacks.SimpleDataCallback callback) {

        Element element = new Element("mybody");
        element.addProperty("type", "permission");

        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(),
                "returnPermission", new NetworkCallbacks.ChatMessageListener() {
                    @Override
                    public void gotMessage(Element element, Message message, Chat chat, boolean isSuccess) {

                        callback.onFinish(isSuccess, "", element);
                    }
                });
    }


    /**
     * 发送条码
     */
    public void sendScanningResult(String scanResult, final NetworkCallbacks.SimpleCallback callback) {

        Element element = new Element(ChatCallbackTag.ELEMENT_NAME);
        element.addProperty(ChatCallbackTag.PROPERTY_TYPE, "requestInsertScanningNormal");
        element.addProperty("scanning", scanResult);

        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString());
    }

    /**
     * 查询作业单号
     */
    public void getWorkListNumber(String scanResult, final NetworkCallbacks.SimpleDataCallback callback) {

        Element element = new Element(ChatCallbackTag.ELEMENT_NAME);
        element.addProperty(ChatCallbackTag.PROPERTY_TYPE, "requestQueryWorkPlanByScanning");
        if (scanResult != null && !scanResult.equals("")) {
            element.addProperty("scanning", scanResult);
        }

        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(), "returnQueryWorkPlanByScanning",
                new NetworkCallbacks.MessageListenerThinner() {
                    @Override
                    public void processMessage(Element element, Message message, Chat chat) {

                        boolean isSuccess = element.getBody() != null && !element.getBody().equals("");

                        if (isSuccess) {// 没有查询到结果

                            callback.onFinish(false, "没有查询到结果", null);
                        } else {
                            String queryData = element.getBody();
                            String scanning = element.getProperty("scanning");
                            String userPermission = element
                                    .getProperty("userpermisssion");
                            if (userPermission.equals("yes")) { // 质检员
                                Log.i(LOG_TAG,
                                        "intent");
                                Bundle bundle = new Bundle();
                                bundle.putCharSequence("scanning", scanning);
                                bundle.putCharSequence("allDetailDatas",
                                        queryData);
                                Intent intent = new Intent(
                                        Constants.contexts.get(Constants.contexts
                                                .size() - 1),
                                        BatchQualityTestActivity.class);
                                intent.putExtras(bundle);
                                Constants.contexts.get(
                                        Constants.contexts.size() - 1)
                                        .startActivity(intent);

                            } else if (userPermission.equals("no")) {// 通过条码扫描查询，直接跳转到工作完成填写界面
                                Log.i(LOG_TAG,
                                        "intent");
                                Bundle bundle = new Bundle();
                                bundle.putCharSequence("scanning", scanning);
                                bundle.putCharSequence("allDetailDatas",
                                        queryData);
                                Intent intent = new Intent(
                                        Constants.contexts.get(Constants.contexts
                                                .size() - 1),
                                        BatchWorkPlanSubmitActivity.class);
                                intent.putExtras(bundle);
                                Constants.contexts.get(
                                        Constants.contexts.size() - 1)
                                        .startActivity(intent);
                            }
                        }
                    }
                });
    }


    /**
     * 根据工作单号获取工作单内容
     */
    public void getWorkListByQrcode(String workListNumber, String materielCode,
                                    final NetworkCallbacks.SimpleDataCallback callback) {
        Element element = new Element(ChatCallbackTag.ELEMENT_NAME);
        element.addProperty(ChatCallbackTag.PROPERTY_TYPE, "workListScanFunction");
        element.addProperty("workListNumber", workListNumber);
        element.addProperty("materielCode", materielCode);

        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(), "returnWorkListScanFunction",
                new NetworkCallbacks.MessageListenerThinner() {
                    @Override
                    public void processMessage(Element element, Message message, Chat chat) {

                        Utils.hideCircleProgressDialog();
                        if (element.getBody() == null || element.getBody().equals("")) {// 没有查询到结果
                            callback.onFinish(false, "没有查到结果", null);
                        } else {
                            String queryData = element.getBody();
                            String workListNumber = element.getProperty("workListNumber");
                            String materielCode = element.getProperty("materielCode");
                            String status = element.getProperty("status");
                            Log.i(LOG_TAG, "inWorkListActivity= " + workListNumber
                                    + " " + materielCode + " " + status);

                            callback.onFinish(true, "",
                                    workListNumber + "&" + materielCode + "&" + status);
                        }
                    }
                });
    }

    public void getPurchaseList(String workListNumber, final NetworkCallbacks.SimpleDataCallback callback) {
        Element element = new Element(ChatCallbackTag.ELEMENT_NAME);
        element.addProperty(ChatCallbackTag.PROPERTY_TYPE, "purchaseListScanFunction");
        element.addProperty("purchaseListNumber", workListNumber);
        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(), "returnPurchaseListScanFunction",
                new NetworkCallbacks.MessageListenerThinner() {

                    @Override
                    public void processMessage(Element element, Message message, Chat chat) {

                        Utils.hideCircleProgressDialog();
                        if (element.getBody() == null || element.getBody().equals("")) {

                            callback.onFinish(false, "没有查到结果", null);
                        } else {
                            String queryData = element.getBody();
                            String workListNumber = element
                                    .getProperty("purchaseListNumber");
                            String status = element.getProperty("status");
                            Log.i(LOG_TAG, "inWorkListActivity= " + workListNumber + " " + status);

                            callback.onFinish(true, "", workListNumber + "&" + status);
                        }
                    }
                });
    }
}
