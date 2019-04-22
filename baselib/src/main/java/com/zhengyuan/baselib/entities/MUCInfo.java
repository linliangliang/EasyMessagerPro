package com.zhengyuan.baselib.entities;

public class MUCInfo {

    private String room;
    private String nickname;
    private String account;

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public MUCInfo(String account, String nickname, String room) {
        this.account = account;
        this.nickname = account;
        this.room = room;
    }

    public MUCInfo() {

    }

}
