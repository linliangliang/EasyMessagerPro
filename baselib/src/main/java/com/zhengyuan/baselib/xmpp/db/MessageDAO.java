package com.zhengyuan.baselib.xmpp.db;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jivesoftware.smack.packet.Message;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.xmpp.BaseXmppManager;

import com.zhengyuan.baselib.utils.TimeRenderUtil;

import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.utils.xml.XmlParser;

/**
 * Fun: 接收消息处理类
 * 将接收到的消息存入到数据库
 * <p>
 * Author: 徐兵
 * Time: 2016-04-21
 */

public class MessageDAO {

    public static String tableName = "rec_message";
    //DX改---获取当前用户的username（全局查询currentUserName关键字，就是增加字段所改的地方）
    private String currentUserName = EMProApplicationDelegate.userInfo.getUserId().split("@")[0];
    //获取当前连接的服务名
    String serviceNameString;

    /**
     * 判断是否为请求消息
     *
     * @return
     */
    private boolean isRequestMessage(String mess) {
        //如果消息中包含有<mybody并且包含有type="requestMessage"，则认定为请求消息
        return mess.contains("<mybody") && mess.contains("type=\"requestMessage\"");
    }

    public void insert(Message message) {
        //DX-改2016/10/18，添加uid字段（标识该消息属于哪个用户）

        String sql = "insert into " + tableName + " (uid,mid,mfrom,mto,body,time) values " +
                "('" + currentUserName + "','" + message.getPacketID() + "','" + message.getFrom().split("/")[0] + "','" + message.getTo().split("/")[0] + "','" + message.getBody() + "','" + TimeRenderUtil.getDate() + "')";
//		String sql="insert into "+tableName+" (mid,mfrom,body,time) values " +
//				"('"+message.getPacketID()+"','"+message.getFrom().split("/")[0]+"','"+message.getBody()+"','"+TimeRenderUtil.getDate()+"')";
        SqliteManager.insert(sql);
    }


    //modified 2016.11.11 查询数据表中mid出现的次数
    public int queryCount(String mid) {
        //String sql="select id,mfrom,body,time from "+tableName+" where uid='"+currentUserName+"' order by id desc limit 0,1000";
        String sql = "select * from " + tableName + " where  mid=\'" + mid + "\'";
        Cursor cur = null;
        cur = SqliteManager.query(sql);
        int count = 0;
//		if(cur.moveToFirst())
//		{
//			for(int i=0;i<cur.getCount();i++)
//			{
        while (cur.moveToNext()) {
            count++;
        }
//			}
//		}
        cur.close();
        return count;
    }

    //modified 2016.11.11  查询mid对应的某条消息
    public ExpendMessage queryBymid(String mid) {
        String sql = "select mid,id,mfrom,body,time from " + tableName + " where   mid=\'" + mid + "\'";
        //	String sql="select id,mfrom,body,time from "+tableName + " where uid='"+currentUserName+"' and mfrom =\'"+ jid+"\' order by id desc limit 0,1000";
        Cursor cur = null;
        cur = SqliteManager.query(sql);
        ExpendMessage e = new ExpendMessage();
        //ExpendMessage message=new ExpendMessage();


        while (cur.moveToNext()) {
            //cur.move(i);
            e.setMid(cur.getString(0));
            e.setFrom(cur.getString(2));
            e.setBody(cur.getString(3));
            e.setTime(cur.getString(4));

//				message.setFrom(cur.getString(1));
//				message.setBody(cur.getString(2));
//				message.setTime(cur.getString(3));
//				map.put(cur.getInt(0), message);
//				Log.v("----",cur.get+"");
//				Log.v("----",cur.getString(1)+"");
        }
        cur.close();
        return e;
    }


    public void insertPeerMessage(Message message) {
        //DX-改2016/10/18，添加uid字段（标识该消息属于哪个用户）
        Log.d("MessageDAO", "message " + message.getBody());
        String msgdatetime = "";
        Element body = XmlParser.parse(message.getBody());
        if (body.getProperty("type") != null) {
            if (body.getProperty("type").equals("voice")) {
                msgdatetime = body.getProperty("voicename").substring(0, 13);
                try {
                    msgdatetime = TimeRenderUtil.stringToDate(msgdatetime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if (body.getProperty("type").equals("image")) {
                msgdatetime = body.getProperty("filePath").substring(0, 13);
                try {
                    msgdatetime = TimeRenderUtil.stringToDate(msgdatetime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                msgdatetime = body.getProperty("datetime");
            }
        }
        String sql = "insert into " + tableName + " (uid,mid,mfrom,mto,body,time) values " +
                "('" + currentUserName + "','" + message.getPacketID() + "','" + message.getFrom() + "','" + message.getTo() + "','" + message.getBody() + "','" + msgdatetime + "')";
//		String sql="insert into "+tableName+" (mid,mfrom,mto,body,time) values " +
//				"('"+message.getPacketID()+"','"+message.getFrom()+"','"+message.getTo()+"','"+message.getBody()+"','"+TimeRenderUtil.getDate()+"')";
        SqliteManager.insert(sql);
    }

    public void delete(int id) {

        String sql = "delete  from " + tableName + " where uid='" + currentUserName + "' and id = " + id;
        SqliteManager.delete(sql);
    }

    //modified 2016.11.11 通过mid删除记录
    public void delete(String mid) {

        String sql = "delete  from " + tableName + " where uid='" + currentUserName + "' and mid = \'" + mid + "\'";
        SqliteManager.delete(sql);
    }

    /**
     * 返回用户
     */
    public List<String> queryUserNameByTheavatar(String theavatars) {
        String sql = "select * from the_avatars where theavatars='"
                + theavatars + "' ";
        List<String> list = new ArrayList<String>();

        Cursor cursor = SqliteManager.query(sql);
        synchronized (SqliteManager.sqlitedb) {
            while (cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }
            SqliteManager.closeDatabase();
        }
        return list;
    }

    /**
     * 返回用户头像
     */
    public List<String> qureyTheAvatarsByUserName(String username) {
        String sql = "select theavatars from the_avatars where username='" + username + "'";
        List<String> list = new ArrayList<String>();

        Cursor cursor = SqliteManager.query(sql);
        synchronized (SqliteManager.sqlitedb) {
            while (cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }
            SqliteManager.closeDatabase();
        }
        return list;
    }

    //查询所有消息，按照id先后排序。
    public Map<Integer, ExpendMessage> query() {
        String sql = "select mid,id,mfrom,body,time from " + tableName + " where uid='" + currentUserName + "' order by id desc limit 0,1000";
        Cursor cur = null;
        cur = SqliteManager.query(sql);
        Map<Integer, ExpendMessage> map = new HashMap<Integer, ExpendMessage>();
//		if(cur.moveToFirst())
//		{
//			for(int i=0;i<cur.getCount();i++)
//			{
        while (cur.moveToNext()) {
            //cur.move(i);
            ExpendMessage message = new ExpendMessage();
            message.setMid(cur.getString(0));
            message.setFrom(cur.getString(2));
            message.setBody(cur.getString(3));
            message.setTime(cur.getString(4));
            if (!isRequestMessage(message.getBody()))
                map.put(cur.getInt(1), message);
//				message.setFrom(cur.getString(1));
//				message.setBody(cur.getString(2));
//				message.setTime(cur.getString(3));
//				map.put(cur.getInt(0), message);
//				Log.v("----",cur.get+"");
//				Log.v("----",cur.getString(1)+"");
        }
//			}
//		}
        cur.close();
        return map;
    }


    //modified 2016.11.8 增加mid查询字段
    public Map<Integer, ExpendMessage> queryByJid(String jid) {
        String sql = "select mid,id,mfrom,body,time from " + tableName + " where uid='" + currentUserName + "' and mfrom=\'" + jid + "\' order by id desc limit 0,1000";
        //	String sql="select id,mfrom,body,time from "+tableName + " where uid='"+currentUserName+"' and mfrom =\'"+ jid+"\' order by id desc limit 0,1000";
        Cursor cur = null;
        cur = SqliteManager.query(sql);
        Map<Integer, ExpendMessage> map = new HashMap<Integer, ExpendMessage>();
        //ExpendMessage message=new ExpendMessage();


        while (cur.moveToNext()) {
            //cur.move(i);
            ExpendMessage message = new ExpendMessage();
            message.setMid(cur.getString(0));
            message.setFrom(cur.getString(2));
            message.setBody(cur.getString(3));
            message.setTime(cur.getString(4));
            if (!isRequestMessage(message.getBody()))
                map.put(cur.getInt(1), message);
//			message.setFrom(cur.getString(1));
//			message.setBody(cur.getString(2));
//			message.setTime(cur.getString(3));
//			map.put(cur.getInt(0), message);
//			Log.v("----",cur.get+"");
//			Log.v("----",cur.getString(1)+"");
        }
        cur.close();
        return map;
    }

    public Map<Integer, ExpendMessage> queryByUnread(int haveread) {
        //DX-改2016/10/18，添加uid字段（标识该消息属于哪个用户）
        String sql = "select id,mfrom,body,time,mto from " + tableName + " where uid='" + currentUserName + "' and unread = 1 and mfrom!='" + BaseXmppManager.getConnection().getServiceName() + "' and mto not like 'iqreceiver%'";
//		String sql="select id,mfrom,body,time,mto from "+ tableName + " where unread = 1";

        Map<Integer, ExpendMessage> map = new HashMap<Integer, ExpendMessage>();


        Cursor cur = null;
        cur = SqliteManager.query(sql);
        synchronized (SqliteManager.sqlitedb) {
            while (cur.moveToNext()) {
                //cur.move(i);
                ExpendMessage message = new ExpendMessage();
                message.setFrom(cur.getString(1));
                message.setBody(cur.getString(2));
                message.setTime(cur.getString(3));
                message.setToID(cur.getString(4));
                if (!isRequestMessage(message.getBody()))
                    map.put(cur.getInt(0), message);
            }
            cur.close();
            SqliteManager.closeDatabase();
        }
        return map;
    }

    /**
     * 查询用户-用户消息
     * 2016.4.20
     */
    //modified 2016.11.8 增加查询字段mid
    public Map<Integer, ExpendMessage> queryByBothJID(String jid1, String jid2) {
        //DX改，将查询更准确，由于用户名中含有服务器名，使得广播与普通消息分不出来(注：群聊中含有“.”号，过滤掉点号，加以区分，也可以过滤“@conference.”关键字)
        String id1 = jid1.split("@")[0];
        String id2 = jid2.split("@")[0];
        String sql = "select mid,id,mfrom,mto,body,time from " + tableName + " as rec1 where rec1.uid='" + currentUserName + "' and rec1.mfrom like \'" + id1 + "%\' and rec1.mto like \'" + id2 + "%\' and rec1.mfrom not like \'%.%\' and rec1.mto not like  \'%.%\' " +
                " union select mid,id,mfrom,mto,body,time from " + tableName + " as rec2 where rec2.uid='" + currentUserName + "' and rec2.mfrom like \'" + id2 + "%\' and rec2.mto like \'" + id1 + "%\'" + " and rec2.mfrom not like \'%.%\' and rec2.mto not like  \'%.%\' " +
                " order by id desc limit 0,100";
        Cursor cur = null;
        cur = SqliteManager.query(sql);
        Map<Integer, ExpendMessage> map = new TreeMap<Integer, ExpendMessage>();

        while (cur.moveToNext()) {

            ExpendMessage message = new ExpendMessage();
            message.setMid(cur.getString(0));
            message.setFrom(cur.getString(2));
            message.setTo(cur.getString(3));
            message.setBody(cur.getString(4));

            String time = cur.getString(5);
            Log.d("MessageDAO", "time: " + time);
            message.setTime(cur.getString(5));
            if (!isRequestMessage(message.getBody()))
                map.put(cur.getInt(1), message);
//				message.setFrom(cur.getString(1));
//				message.setTo(cur.getString(2));
//		  		message.setBody(cur.getString(3));
//				message.setTime(cur.getString(4));
//				map.put(cur.getInt(0), message);	
        }
        cur.close();
        return map;
    }

    /**
     * 查询群聊天消息
     * 2016.7.22
     */
    //modified 2016.11.8
    public Map<Integer, ExpendMessage> queryGroupByBothJID(String jid1, String jid2) {
        String sql = "select mid,id,mfrom,mto,body,time from " + tableName + " as rec1 where rec1.uid='" + currentUserName + "' and rec1.mfrom like \'%" + jid1 + "%\' and rec1.mto like \'%" + jid2 + "%\'" +
                " union select mid,id,mfrom,mto,body,time from " + tableName + " as rec2 where rec2.uid='" + currentUserName + "' and rec2.mfrom like \'%" + jid2 + "%\' and rec2.mto like \'%" + jid1 + "%\'" + " order by id desc limit 0,100";

        Cursor cur = null;
        cur = SqliteManager.query(sql);
        Map<Integer, ExpendMessage> map = new TreeMap<Integer, ExpendMessage>();

        while (cur.moveToNext()) {

            ExpendMessage message = new ExpendMessage();
            message.setMid(cur.getString(0));
            message.setFrom(cur.getString(2));
            message.setTo(cur.getString(3));
            message.setBody(cur.getString(4));
            message.setTime(cur.getString(5));
            if (!isRequestMessage(message.getBody()))
                map.put(cur.getInt(1), message);

//				message.setFrom(cur.getString(1));
//				message.setTo(cur.getString(2));
//		  		message.setBody(cur.getString(3));
//				message.setTime(cur.getString(4));
//				map.put(cur.getInt(0), message);

        }
        cur.close();
        return map;
    }

    /**
     * 查询广播消息
     * 2016.4.28 董蓓
     */
    public Map<Integer, ExpendMessage> queryNotice() {
        serviceNameString = BaseXmppManager.getConnection().getServiceName();
        //DX改，更改查询广播的sql语句
        String sql = "select id,mfrom,mto,body,time,unread from " + tableName + " where uid='" + currentUserName + "' and mfrom ='" + serviceNameString + "' order by unread desc ";
        Map<Integer, ExpendMessage> map = new TreeMap<Integer, ExpendMessage>();

        Cursor cur = SqliteManager.query(sql);

        synchronized (SqliteManager.sqlitedb) {
            while (cur.moveToNext()) {

                ExpendMessage message = new ExpendMessage();
                message.setFrom(cur.getString(1));
                message.setTo(cur.getString(2));
                message.setBody(cur.getString(3));
                message.setTime(cur.getString(4));
                message.setUnread(cur.getInt(5));
                if (!isRequestMessage(message.getBody()))
                    map.put(cur.getInt(0), message);

            }
            cur.close();
            SqliteManager.closeDatabase();
        }
        return map;
    }

    public void deleteAllNotice() {
        //DX改
        serviceNameString = BaseXmppManager.getConnection().getServiceName();
        String sql = "delete from " + tableName + " where uid='" + currentUserName + "' and mfrom ='" + serviceNameString + "'";
        SqliteManager.delete(sql);
    }

    //  功能：查询系统广播未读消息
    //  时间： 2016-05-11
    //  更改人：张礼
    public Map<Integer, ExpendMessage> queryUnreadNotice() {
        //DX改
        serviceNameString = BaseXmppManager.getConnection().getServiceName();
        String sql = "select id,mfrom,mto,body,time,unread from " + tableName + "" +
                " where mfrom ='" + serviceNameString + "' and unread = 1 and uid='" + currentUserName + "' order by unread asc,id asc";
        Map<Integer, ExpendMessage> map = new TreeMap<Integer, ExpendMessage>();

        Cursor cur = SqliteManager.query(sql);
        synchronized (SqliteManager.sqlitedb) {
            if (cur != null) {
                while (cur.moveToNext()) {
                    ExpendMessage message = new ExpendMessage();
                    message.setFrom(cur.getString(1));
                    message.setTo(cur.getString(2));
                    message.setBody(cur.getString(3));
                    message.setTime(cur.getString(4));
                    message.setUnread(cur.getInt(5));
                    if (!isRequestMessage(message.getBody()))
                        map.put(cur.getInt(0), message);

                }
                cur.close();

                //kangkang改
                SqliteManager.closeDatabase();
            }
        }
        return map;
    }

    /**
     * 修改广播通知的已读状态
     */
    public void updateBroadcastStatus(int id) {
        //DX改10/18，增加当前用户字段
        String sql = "update " + tableName + " set unread=0 where id=" + id + " and uid='" + currentUserName + "'";
        SqliteManager.update(sql);
    }

    public void closeDb() {
        SqliteManager.closeDatabase();
    }

    public void openDb(Context context) {
        SqliteManager.openDatabase(context);
    }

    //modified 2016.11.8 增加字段mid
    public static class ExpendMessage extends Message {
        private String toID;
        private int unread = 1;
        private String time;
        private String mid;

        public int getUnread() {
            return unread;
        }

        public void setUnread(int unread) {
            this.unread = unread;
        }

        public void setMid(String mid) {
            this.mid = mid;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getToID() {
            return toID;
        }

        public void setToID(String toID) {
            this.toID = toID;
        }

        public synchronized final String getMid() {
            return mid;
        }

    }
}
