package com.zhengyuan.baselib.http.base;

/**
 * Created by gpsts on 17-8-23.
 */

public class HttpError extends Exception {

    private String code;

    public HttpError(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
