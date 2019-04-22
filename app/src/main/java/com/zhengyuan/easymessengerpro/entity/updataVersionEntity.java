package com.zhengyuan.easymessengerpro.entity;

/**
 * @author 林亮
 * @description:
 * @date :2019/2/25 14:16
 */

public class updataVersionEntity {
    //app名字
    private String appname;
    //服务器版本号
    private String serverVersion;
    //服务器标志
    private String serverFlag;
    //是否强制更新
    private String lastForce;
    //apk下载地址，
    private String updateurl;
    //版本的更新的描述
    private String upgradeinfo;

    private String appType;

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getServerFlag() {
        return serverFlag;
    }

    public void setServerFlag(String serverFlag) {
        this.serverFlag = serverFlag;
    }

    public String getLastForce() {
        return lastForce;
    }

    public void setLastForce(String lastForce) {
        this.lastForce = lastForce;
    }

    public String getUpdateurl() {
        return updateurl;
    }

    public void setUpdateurl(String updateurl) {
        this.updateurl = updateurl;
    }

    public String getUpgradeinfo() {
        return upgradeinfo;
    }

    public void setUpgradeinfo(String upgradeinfo) {
        this.upgradeinfo = upgradeinfo;
    }

    @Override
    public String toString() {
        return "updataVersionEntity{" +
                "appname='" + appname + '\'' +
                ", serverVersion='" + serverVersion + '\'' +
                ", serverFlag='" + serverFlag + '\'' +
                ", lastForce='" + lastForce + '\'' +
                ", updateurl='" + updateurl + '\'' +
                ", upgradeinfo='" + upgradeinfo + '\'' +
                '}';
    }
}
