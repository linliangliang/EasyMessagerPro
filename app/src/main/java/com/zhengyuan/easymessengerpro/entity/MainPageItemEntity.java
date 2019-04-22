package com.zhengyuan.easymessengerpro.entity;

/**
 * Created by zy on 2017/11/16.
 */

public class MainPageItemEntity {
    public String name;
    public String realName;

    public MainPageItemEntity(String name, String realName) {
        this.name = name;
        this.realName = realName;
    }

    @Override
    public String toString() {
        return "MainPageItemEntity{" +
                "name='" + name + '\'' +
                ", realName='" + realName + '\'' +
                '}';
    }
}
