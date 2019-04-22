package com.zhengyuan.baselib.entities;

/**
 * Created by gpsts on 17-8-23.
 */

public class KeyValueEntity {

    public String repName;
    public String repHttp;

    public KeyValueEntity(String repName, String value) {
        this.repName = repName;
        this.repHttp = value;
    }
}
