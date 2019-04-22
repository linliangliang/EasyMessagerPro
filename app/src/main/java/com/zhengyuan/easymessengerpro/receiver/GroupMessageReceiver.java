package com.zhengyuan.easymessengerpro.receiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.qihoo360.replugin.RePlugin;
import com.zhengyuan.easymessengerpro.R;

/**
 * 广播消息（群组聊天消息）
 * 时间：2016-07-21
 * 自定义一个接受广播的类,用于进行接受广播消息
 * 其中的onReceive消息会在在有广播时进行接收消息
 * 
 */

public class GroupMessageReceiver extends BroadcastReceiver {

	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {

		//使用时间戳，使得消息栏消息的ID都不同
		//int requestcode = (int)System.currentTimeMillis();
		
		//通过参数intent获取值
		String name = intent.getStringExtra("NAME");
		//由同一字符串获取唯一的hashcode来区分消息的来源
		int hashcode = name.hashCode();
		//notification进行消息的广播，PendingIntent传值与消息设置
		// TODO Auto-generated method stub

		Class class1 = null;
		try {
			class1 = RePlugin.fetchClassLoader("EMChat").loadClass(
					"com.zhengyuan.emchat.activity.GroupChatActivity");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Intent i = new Intent(context, class1);
		i.putExtra("GROUPID", name);
		//i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pi = PendingIntent.getActivity(context, hashcode, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification myNotification = new Notification();
		Builder myNotificationBuilder = new Notification.Builder(context);
		// 通过设置PendingIntent来直接跳转到Activity
		myNotification.contentIntent = pi;
		// 消息点击后自动消失
		myNotification.flags = Notification.FLAG_AUTO_CANCEL;
		// 设置消息的一些属性
		myNotificationBuilder.setAutoCancel(true);// 点击后自动消失
		myNotificationBuilder.setPriority(0);// 设置优先级
		myNotificationBuilder.setContentTitle("新的聊天消息");//设置消息标题
		myNotificationBuilder.setContentText("来自群组"+name.split("/")[0].split("@")[0]+"的消息，点击查看");//设置消息文本内容
		myNotificationBuilder.setSmallIcon(R.drawable.peerchat);//设置消息图标
		myNotificationBuilder.setContentIntent(pi);
		myNotification = myNotificationBuilder.build();
		long[] vibrates = {0,1000,1000,1000};
		myNotification.vibrate = vibrates;
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(hashcode, myNotification);
	}
}
