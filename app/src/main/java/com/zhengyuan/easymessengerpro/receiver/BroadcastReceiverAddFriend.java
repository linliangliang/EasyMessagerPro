package com.zhengyuan.easymessengerpro.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.StaticVariable;
import com.zhengyuan.baselib.utils.ToolClass;
import com.zhengyuan.baselib.xmpp.db.AcceptFriendDAO;
import com.zhengyuan.easymessengerpro.xmpp.XmppManager;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;

/**
 * 广播接收器（接收添加好友的请求）
 *
 * @author 9811
 */
public class BroadcastReceiverAddFriend extends BroadcastReceiver {

    String response;
    String acceptAdd;
    String alertName;
    String alertSubName;

    String friendNickName;
    String addFriendReason;
    Roster roster = XmppManager.getConnection().getRoster();
    String serverNameString = XmppManager.getConnection().getServiceName();

    AcceptFriendDAO dao = AcceptFriendDAO.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        roster.setSubscriptionMode(Roster.SubscriptionMode.manual);// 设置为手动处理所有加好友的请求

        // 接收传递的字符串response
        Bundle bundle = intent.getExtras();
        response = bundle.getString("response");
        System.out.println("广播收到" + response);

        // text_response.setText(response);//设置查询的结果的用户名

        if (response == null) {// 当response==null时，为收到添加请求
            // 获取传递的字符串及发送方JID
            acceptAdd = bundle.getString("acceptAdd");
            alertName = bundle.getString("fromName");
            System.out.println("acceptAdd=" + acceptAdd + ",alertName="
                    + alertName);
            if (alertName != null) {
                // 裁剪JID得到对方用户名
                alertSubName = alertName.substring(0, alertName.indexOf("@"));
            }
            addFriendReason = bundle.getString("reason");
            friendNickName = bundle.getString("nickName");
            if (friendNickName == null) {
                friendNickName = alertName;
            }
            if (ToolClass.isFriend(alertSubName)) {
                // dialog=new SweetAlertDialog(ToolClass.getContext(), 0);
                // dialog.setTitleText("恭喜");
                // dialog.setContentText("你已经成为用户\"" + alertSubName + "\"的好友了");
                // dialog.showCancelButton(false);
                // dialog.setCancelable(true);
                // dialog.show();
                Toast.makeText(ToolClass.getContext(),
                        "你已经成为用户" + friendNickName + "的好友了", Toast.LENGTH_SHORT)
                        .show();
            } else if (acceptAdd.equals("收到添加请求！")) {
                dao.insert(alertSubName, addFriendReason, friendNickName);
                if (Constants.contexts.size() >= 2)//登陆进入主界面以后才弹窗，防止在登陆界面就弹窗
                {
                    final SweetAlertDialog dialog = new SweetAlertDialog(ToolClass.getContext(), 0);
                    dialog.setTitleText("添加好友请求");
                    dialog.setContentText("用户\"" + friendNickName + "\"请求添加你为好友");
                    dialog.setConfirmText("同意");
                    dialog.setConfirmClickListener(new OnSweetClickListener() {

                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            dao.accept(alertSubName);
                            Presence presenceRes = new Presence(
                                    Presence.Type.subscribed);
                            presenceRes.setTo(alertName);
                            XmppManager.getConnection().sendPacket(presenceRes);
                            ToolClass.addFriend(alertSubName, friendNickName);
                            ToolClass.sendAddFriendMessageVerify(alertSubName + "@"
                                    + serverNameString + "/"
                                    + Constants.XMPP_RESOURCE_NAME);// 发送加好友确认消息
                            if (StaticVariable.inContactMainActivity) {//在contact页面刷新好友列表
                                Message message = StaticVariable.handler.obtainMessage();
                                message.what = 1;
                                message.sendToTarget();
                            }
                            dialog.dismissWithAnimation();
                        }
                    });
                    dialog.setCancelText("拒绝");
                    dialog.setCancelClickListener(new OnSweetClickListener() {

                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            // 是否考虑改为什么都不做
                            Presence presenceRes = new Presence(
                                    Presence.Type.unsubscribe);
                            presenceRes.setTo(alertName);
                            XmppManager.getConnection().sendPacket(presenceRes);
                            dialog.dismissWithAnimation();
                        }
                    });
                    dialog.show();
                }
            } else if (acceptAdd.equals("收到添加确认！")) {
                // 当收到添加好友确认消息，不弹窗
                Presence presenceRes = new Presence(Presence.Type.subscribed);
                presenceRes.setTo(alertName);
                XmppManager.getConnection().sendPacket(presenceRes);
                ToolClass.addFriend(alertSubName, friendNickName);
            }
        }
    }

    /**
     * 添加好友
     *
     * @param roster
     * @param friendName
     * @param name
     * @return
     */
    public boolean addFriend(Roster roster, String friendName, String name) {
        try {
            roster.createEntry(friendName.trim() + "@"
                            + XmppManager.getConnection().getServiceName(), name,
                    new String[]{"Friends"});
            System.out.println("添加好友成功！！");
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("失败！！" + e);
            return false;
        }
    }

}
