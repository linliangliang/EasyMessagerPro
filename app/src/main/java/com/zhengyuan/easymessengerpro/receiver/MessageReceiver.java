package com.zhengyuan.easymessengerpro.receiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.qihoo360.replugin.RePlugin;
import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.StaticVariable;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.ChatUtils;
import com.zhengyuan.baselib.xmpp.db.MessageDAO;
import com.zhengyuan.baselib.xmpp.db.MessageDAO.ExpendMessage;
import com.zhengyuan.easymessengerpro.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author rd0166 张礼 时间：2016-04-12 自定义一个接受广播的类,用于进行接受广播消息
 *         其中的onReceive消息会在在有广播时进行消息接收
 * 
 */

public class MessageReceiver extends BroadcastReceiver {

	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {

		//使用时间戳，使得消息栏消息的ID都不同
		//int requestcode = (int)System.currentTimeMillis();
		
		//对本地未读消息，每次从数据库中读取并统计
		String type=intent.getStringExtra("TYPE");
		if (type.equals("NoticePage")) {//系统消息
			Map<Integer, ExpendMessage> map = new HashMap<Integer,ExpendMessage>(); 
			MessageDAO messagedao = new MessageDAO();
//			messagedao.openDb(context);
			map = messagedao.queryUnreadNotice();
//			messagedao.closeDb();
			int messagenumber = map.size();
			
			//通过参数intent获取值
			String name = intent.getStringExtra("NAME");
			String time = intent.getStringExtra("TIME");
			String message = intent.getStringExtra("MESSAGE");
			
			//notification进行消息的广播，PendingIntent传值与消息设置
			// TODO 如何在这里打开插件
			Intent notice = RePlugin.createIntent("emmessage",
					"com.zhengyuan.emmessage.MyMessageActivity");
//			RePlugin.startActivity(activity, i2);
			//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);//使用intent启动activity标记
			//i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//ActivityManager manager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
			//List<ActivityManager.RunningServiceInfo> runningTaskInfos=manager.getRunningTasks(1);
			//if(runningTaskInfos!=null)
			notice.putExtra("NAME", name);
			notice.putExtra("TIME", time);
			notice.putExtra("MESSAGE", message);
			//此处id并没有其他作用，仅在传输到notifiedActivity界面做填充，因为如果从消息模块进入，需要获取id。
			//如果从本activity进入，不传送id则会报错，所以传一个无效的id
			notice.putExtra("ID", "0");
			PendingIntent pi = PendingIntent.getActivity(context, 0, notice,
					PendingIntent.FLAG_UPDATE_CURRENT);
			Notification myNotification = new Notification();

			Builder myNotificationBuilder = new Notification.Builder(context);
			// 通过设置PendingIntent来直接跳转到Activity
			myNotification.contentIntent = pi;
			// 消息点击后自动消失
			myNotification.flags = Notification.FLAG_AUTO_CANCEL;
			myNotification.tickerText = "新消息!";
			// 设置消息的一些属性
			myNotificationBuilder.setAutoCancel(true);// 点击后自动消失
			myNotificationBuilder.setPriority(0);// 设置优先级
			//myNotificationBuilder.setContentTitle(i.getStringExtra("NAME"));//设置消息标题
			//myNotificationBuilder.setContentText(i.getStringExtra("MESSAGE"));//设置消息文本内容
			myNotificationBuilder.setContentTitle("系统消息");
			myNotificationBuilder.setContentText("你有"+ messagenumber +"条消息未读.");
		//	myNotificationBuilder.setContentText("你有未读的系统消息.");
			myNotificationBuilder.setSmallIcon(R.drawable.notice);//设置消息图标
			myNotificationBuilder.setContentIntent(pi);
			myNotification = myNotificationBuilder.build();
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(0, myNotification);
		}else if (type.equals("WorkListChange")) {//
//			String name = intent.getStringExtra("NAME");
			String time = intent.getStringExtra("TIME");
			String message = intent.getStringExtra("MESSAGE");
			
			//notification进行消息的广播，PendingIntent传值与消息设置
			// TODO Auto-generated method stub
//			Intent notice = new Intent(context, WorkPlanShowActivity.class);
			Intent notice = new Intent(Constants.contexts.get(Constants.contexts.size()-1),MessageReceiver.class);
			notice.putExtra("TYPE","RequestWorkPlan");
			//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);//使用intent启动activity标记
			//i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//ActivityManager manager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
			//List<ActivityManager.RunningServiceInfo> runningTaskInfos=manager.getRunningTasks(1);
			//if(runningTaskInfos!=null)
//			notice.putExtra("NAME", name);
//			notice.putExtra("TIME", time);
//			notice.putExtra("MESSAGE", message);
			//此处id并没有其他作用，仅在传输到notifiedActivity界面做填充，因为如果从消息模块进入，需要获取id。
			//如果从本activity进入，不传送id则会报错，所以传一个无效的id
//			notice.putExtra("ID", "0");
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, notice,
					PendingIntent.FLAG_UPDATE_CURRENT);
			Notification myNotification = new Notification();

			Builder myNotificationBuilder = new Notification.Builder(context);
			// 通过设置PendingIntent来直接跳转到Activity
			myNotification.contentIntent = pi;
			// 消息点击后自动消失
			myNotification.flags = Notification.FLAG_AUTO_CANCEL;
			myNotification.tickerText = "新消息!";
			// 设置消息的一些属性
			myNotificationBuilder.setAutoCancel(true);// 点击后自动消失
			myNotificationBuilder.setPriority(0);// 设置优先级
			//myNotificationBuilder.setContentTitle(i.getStringExtra("NAME"));//设置消息标题
			//myNotificationBuilder.setContentText(i.getStringExtra("MESSAGE"));//设置消息文本内容
			myNotificationBuilder.setContentTitle("班组工作消息");
			myNotificationBuilder.setContentText(""+message);
		//	myNotificationBuilder.setContentText("你有未读的系统消息.");
			myNotificationBuilder.setSmallIcon(R.drawable.notice);//设置消息图标
			myNotificationBuilder.setContentIntent(pi);
			myNotification = myNotificationBuilder.build();
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(0, myNotification);
		}else if (type.equals("RequestWorkPlan")) {//点击班组notification
			System.out.println("----RequestWorkPlan----->");
			if (StaticVariable.inMainActivity) {//在mainactivity中则跳转到workplanshowactivity中
				requestWorkPlan();
			}else if (StaticVariable.inWorkPlanShowActivity) {//在workplanShowActivity中，不做操作
				
			}else {//在其它页面，提示用户返回主页面刷新
				Toast.makeText(Constants.contexts.get(Constants.contexts.size()-1), "请到主界面查询物料代码", Toast.LENGTH_LONG).show();
			}
			
		}
		
	}
	/**
	 * 
	 * 向服务器发送请求获取可用物料代码
	 * 
	 * */
    protected void requestWorkPlan() {
    	Element element=new Element("mybody");
		element.addProperty("type", "requestWorkPlanSum");
//		ChatManager chatmanager = XmppManager.getConnection().getChatManager();

//    	Chat newchat0 = chatmanager.createChat("iqreceiver@"+XmppManager.getConnection().getServiceName(),null);//xxzx-gyj8860
    	Utils.createCircleProgressDialog(Constants.contexts.get(Constants.contexts.size() - 1), "正在获取物料代码，请等待...");
    	ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString());
//		try {
//			newchat0.sendMessage(element.toString());
//		} catch (XMPPException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
  }