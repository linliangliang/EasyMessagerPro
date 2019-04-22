package com.zhengyuan.baselib.entities.biz;

import com.zhengyuan.baselib.constants.Constants;

public class ContactPass {

    private static ContactPass cp = null;
    private String contactId = "empty";

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        if (contactId.split("@").length > 1)
            this.contactId = contactId;
        else
            this.contactId = contactId + "@" + Constants.SERVER_NAME; //EMProApplicationDelegate.userInfo.getUserId().split("@")[1];
    }

    private ContactPass() {

    }

    public static ContactPass getInstance() {
        if (cp == null)
            cp = new ContactPass();
        return cp;
    }
}
