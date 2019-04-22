package com.zhengyuan.baselib.xmpp;

/**
 * Created by zy on 2017/11/14.
 */

public class ChatCallbackTag {

    //发送时element基本的标签, type为必加的标签
    public static String ELEMENT_NAME = "mybody";
    public static String PROPERTY_TYPE = "type";

    //获取所有用户的tag
    public static String REQUEST_ALL_USER = "requestAllUser";
    public static String GOT_ALL_USER = "returnAllUser";
}
