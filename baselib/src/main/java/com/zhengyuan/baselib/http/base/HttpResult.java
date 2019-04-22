package com.zhengyuan.baselib.http.base;

/**
 * Created by gpsts on 17-8-23.
 */

public class HttpResult {

    public static final String CODE_SUCCESS = "200";
    public static final String CODE_FAIL = "210";

    public static final String MSG_SUCCESS = "请求成功";
    public static final String MSG_FAIL = "请求失败";

    private String code;

    private String msg;

    private String data;

    public HttpResult() {
        this(CODE_SUCCESS,MSG_SUCCESS);
    }

    public HttpResult(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
