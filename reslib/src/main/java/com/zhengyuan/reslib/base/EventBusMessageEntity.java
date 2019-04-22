package com.zhengyuan.reslib.base;

/**
 * EventBus用来传递message
 * Created by gpsts on 17-6-8.
 */

public class EventBusMessageEntity {

    public enum EVENT_TYPE {

        ALL,
        LOGIN,
        SEND_TXT_MSG,
        WORK_PLAN_BY_DATE,
    }

    public final String message;
    public boolean isSuccess;
    public EVENT_TYPE eventType;
    public Object dataObject;

    // 过滤消息的tag
    private String className;

    public boolean isThisClass(String className) {

        return this.className.equals(className);
    }

    public EventBusMessageEntity(
            String className,
            String message, boolean isSuccess, EVENT_TYPE eventType) {

        this.className = className;
        this.message = message;
        this.isSuccess = isSuccess;
        this.eventType = eventType;
    }

    public EventBusMessageEntity(
            String className,
            String message, boolean isSuccess, EVENT_TYPE eventType,
            Object dataObject) {

        this.dataObject = dataObject;
        this.className = className;
        this.message = message;
        this.isSuccess = isSuccess;
        this.eventType = eventType;
    }
}
