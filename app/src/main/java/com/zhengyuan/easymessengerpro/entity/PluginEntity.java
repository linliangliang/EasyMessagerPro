package com.zhengyuan.easymessengerpro.entity;

import java.util.Arrays;

/**
 * Created by zy on 2017/11/15.
 */

public class PluginEntity {

//            "showName":"考勤",
//            "realName":"emattendance",
//            "isBuiltIn":"true",
//            "updateInfo":"",
//    "iconTypeAndName": "mipmap/host_ic_launcher"
    public String id;
    public String showName;
    public String realName;
    public boolean isBuiltIn;
    public boolean isShowInMainView;
    public String updateInfo;
    public String iconTypeAndName;
    public int version;
    public String requirePermission;

    public BootableActivity[] host2PluginActivities;

    public class BootableActivity {
        public String name;
        public BootableActivity(String name) {
            this.name = name;
        }
    }

    public PluginEntity(String arrayString) {

        String[] items = arrayString.split(",");
        id = items[0];
        realName = items[1];
        showName = items[2];
        isBuiltIn = Boolean.parseBoolean(items[3]);
        isShowInMainView = Boolean.parseBoolean(items[4]);
        updateInfo = items[5];
        iconTypeAndName = items[6];
        version = Integer.parseInt(items[7]);

        String[] activities = items[8].split(":");
        host2PluginActivities = new BootableActivity[activities.length];
        for (int i = 0; i < activities.length; i++) {
            host2PluginActivities[i] = new BootableActivity(activities[i]);
        }

        requirePermission = items[9];
    }

    @Override
    public String toString() {
        return "PluginEntity{" +
                "showName='" + showName + '\'' +
                ", realName='" + realName + '\'' +
                ", isBuiltIn=" + isBuiltIn +
                ", updateInfo='" + updateInfo + '\'' +
                ", iconTypeAndName='" + iconTypeAndName + '\'' +
                ", version=" + version +
                ", host2PluginActivities=" + Arrays.toString(host2PluginActivities) +
                '}';
    }
}
