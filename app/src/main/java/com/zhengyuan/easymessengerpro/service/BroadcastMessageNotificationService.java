package com.zhengyuan.easymessengerpro.service;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.zhengyuan.easymessengerpro.receiver.MessageReceiver;

/**
 * @author rd0166 张礼
 * 时间：2016-04-11
 * 作用：自定义一个Service类，用于开启一个后台服务
 * 开启服务时，service中自带的OnCreate()方法创建service
 * 
 */
public class BroadcastMessageNotificationService extends Service {

	String name;//发送用户名
	String time;//发送接收时间
	String message;//发送消息文本
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	// service被创建时回调该方法
	public void onCreate() {

		super.onCreate();
		/*Intent intent = new Intent(this, MessageReceiver.class);
	    intent.putExtra("NAME", name);
	    intent.putExtra("TIME", time);
	    intent.putExtra("MESSAGE", message);
		PendingIntent pendingintent = PendingIntent.getBroadcast(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);	
		//try-catch捕获异常
		try {
			pendingintent.send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}*/
	}

	// service被启动时回调该方法
	//public int onStartCommand(Intent intent,int flag,int startId) {
	public int onStartCommand(Intent in,int flag,int startId) {	

		super.onStartCommand(in, flag, startId);
		name = in.getStringExtra("USER");
		message = in.getStringExtra("BODY");
		time = in.getStringExtra("TIME");
		
		Intent intent = new Intent(this,MessageReceiver.class);	    
	    intent.putExtra("NAME", name);
	    intent.putExtra("TIME", time);
	    intent.putExtra("MESSAGE", message);
		PendingIntent pendingintent = PendingIntent.getBroadcast(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);	
		//try-catch捕获异常
		try {
			pendingintent.send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
		return START_STICKY;
	}

	// service关闭之前回调该方法
	public void onDestroy() {

		super.onDestroy();
	}
}
