package com.zhengyuan.baselib.xmpp.interfaces;

/**
 * Created by zy on 2017/11/4.
 * Xmpp管理接口
 */

public interface IXmppManager {

    /**
     * 开始建立连接准备。
     */
    void start();

    /**
     * 停止连接。
     */
    void stop();

    /**
     * 建立连接。
     *
     * @param isReconnection 判断是否是重连
     */
    void connect(boolean isReconnection);

    /**
     * 取消连接。
     */
    void disconnect();
}
