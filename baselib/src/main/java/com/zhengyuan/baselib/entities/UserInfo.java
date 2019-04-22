package com.zhengyuan.baselib.entities;

import com.zhengyuan.baselib.utils.ToolClass;

import java.util.Map;

/**
 * Created by zy on 2017/11/6.
 */

public class UserInfo {
    private String userId;
    private String password;
    private String userName;
    public boolean isAutoLogin = false;

    // 名字; 部门; 手机号; 职位
    public String nickName;
    public String department;
    public String mobile;
    public String position;

    public UserInfo() {

    }

    public boolean isValideUserInfo() {

        if (userId != null && !userId.equals("") && password != null && !password.equals("")) {
            return true;
        }
        return false;
    }

    public String getUserName() {
        if (userName != null) {
            return userName;
        } else {
            Map<String, String> mapTemp = ToolClass.getUserInfo(userId.split("@")[0]);
            userName = mapTemp.get("name");
            return userName;
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {

        String[] ids = userId.split("@");
        if (ids.length == 2) {
            this.userId = ids[0].toLowerCase() + "@" + ids[1];
        } else {
            this.userId = userId.toLowerCase();
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
