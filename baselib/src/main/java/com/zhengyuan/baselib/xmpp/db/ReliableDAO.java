package com.zhengyuan.baselib.xmpp.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.packet.Message;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.entities.Msg;
import com.zhengyuan.baselib.utils.TimeRenderUtil;

public class ReliableDAO {

    private static String tableRecCheck = "rcheck_message";
    private static String tableName = "unack_message";
    //DX改---获取当前用户的username（全局查询currentUserName关键字，就是增加字段所改的地方）
    private String currentUserName = EMProApplicationDelegate.userInfo.getUserId().split("@")[0];

    public void closeDb() {
        SqliteManager2.closeDatabase();
    }

    public void openDb(Context context) {
        SqliteManager2.openDatabase(context);
    }

    // 表结构： id mid

    public void instert(String mid) {
        try {
            String sql = "insert into " + tableName + " (uid, mid,count,timeout) values " + "('" + currentUserName + "','" + mid + "' , 1," + (new Date()).getTime() + ")";
            SqliteManager2.insert(sql);
        } catch (Exception e) {
            Log.e("sqlerror", e.getStackTrace().toString());
        }
    }

    // 重发计数 +1

    public void updateCount(String mid) {
        try {
            String sql = "update " + tableName
                    + " set count = count + 1 where uid='" + currentUserName + "' and mid = " + "('" + mid + "')";
            SqliteManager2.update(sql);
        } catch (Exception e) {
            Log.e("sqlerror", e.getStackTrace().toString());
        }
    }

    //重发次数清0
    public void updateClearCount(String mid) {
        try {
            String sql = "update " + tableName
                    + " set count = 0 where uid='" + currentUserName + "' and mid = " + "('" + mid + "')";
            SqliteManager2.update(sql);
        } catch (Exception e) {
            Log.e("sqlerror", e.getStackTrace().toString());
        }
    }

    //发送成功，删除消息
    public void delete(String mid) {
        try {
            String sql = "delete from " + tableName + " where uid='" + currentUserName + "' and mid = '" + mid + "'";
            Log.v("delete", sql);
            SqliteManager2.delete(sql);
        } catch (Exception e) {
            Log.e("sqlerror", e.getStackTrace().toString());
        }
    }

    /**
     * 获取所有尚需重发的消息(//DX疑问，是否需要limit1)
     *
     * @param retry
     * @return
     */
    public List<UnackMessage> getRetryMessage(int retry) {
        List<UnackMessage> messageList = new ArrayList<UnackMessage>();
        //DX改，由于两张表都有uid字段，所以会冲突，MessageDAO.tableName是rec_message表，ReliableDAO.tableName为unackmessage表
        String sql = "select " + MessageDAO.tableName + ".id,mfrom,mto,body,time,unread,timeout,count," + MessageDAO.tableName + ".mid from " + MessageDAO.tableName + "," + ReliableDAO.tableName
                + " where " + MessageDAO.tableName + ".uid='" + currentUserName + "'and " + ReliableDAO.tableName + ".uid='" + currentUserName + "' and " + ReliableDAO.tableName + ".mid = " + MessageDAO.tableName + ".mid and " + ReliableDAO.tableName
                + ".count <= " + retry + " order by timeout desc";
//		String sql = "select " +MessageDAO.tableName +".id,mfrom,mto,body,time,unread,timeout,count," +MessageDAO.tableName +".mid from " + MessageDAO.tableName + "," + ReliableDAO.tableName
//				+ " where uid='"+currentUserName+"' and " + ReliableDAO.tableName + ".mid = " +MessageDAO.tableName +".mid and "+ ReliableDAO.tableName 
//				+ ".count <= "+ retry + " order by timeout desc limit 1";
//select rec_message.id,mfrom,mto,body,time,unread,timeout,count from rec_message,unack_message where unack_message.mid = rec_message.mid and unack_message.count < 3 order by timeout desc limit 1

        try {
            Cursor cur = null;

            cur = SqliteManager2.query(sql);
            synchronized (SqliteManager2.sqlitedb) {
                while (cur.moveToNext()) {
                    UnackMessage message = new UnackMessage();
                    message.setFrom(cur.getString(1));
                    message.setTo(cur.getString(2));
                    message.setBody(cur.getString(3));
                    message.setTime(cur.getString(4));
                    message.setTimestamp(cur.getLong(6));
                    message.setCount(cur.getInt(7));
                    message.setPacketID(new String(cur.getString(8)));
                    // message.setUnread(cur.getInt(5));
                    messageList.add(message);
                }
                cur.close();
                SqliteManager2.closeDatabase();
            }
        } catch (Exception e) {
            Log.e("sqlerror", e.getStackTrace().toString());
        }
        return messageList;
    }

    public static void insertReceiveChack(Message message) {
        //查重插入
        String sql = "insert into " + tableRecCheck + " (uid,mid,mfrom,mto,time) values " +
                "('" + EMProApplicationDelegate.userInfo.getUserId().split("@")[0] + "','" + message.getPacketID() + "','" + message.getFrom() + "','" + message.getTo() + "','" + TimeRenderUtil.getDate() + "')";
        Log.v("-----------", sql);
        SqliteManager.insert(sql);
    }

    public static int reReceiveCheck(String MessageID) {
        //DX改
        String currentUserID = EMProApplicationDelegate.userInfo.getUserId().split("@")[0];
//		SQL语法  //" + MessageStorageDAO.getMessagetable() +".id,MessageDAO.tableName
        String sql = "select mid from " + tableRecCheck + " where uid='" + currentUserID + "' and mid = '" + MessageID + "' ";
        int resultNum = 0;
        try {

            Cursor cur = null;

            cur = SqliteManager2.query(sql);
            synchronized (SqliteManager2.sqlitedb) {
                if (cur != null) {
                    while (cur.moveToNext()) {
                        resultNum++;
                        break;
                    }
                    cur.close();
                    SqliteManager2.closeDatabase();
                }
                Log.e("sql", sql);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            resultNum = 0;
        }
        Log.e("check resultnum=", resultNum + ".....");
        return resultNum;
    }


    //modified 2016.11.9 获取所有未发送成功消息的记录
    public List<Msg> getUnsendMessage(String uid1, String uid2) {
        List<Msg> messageList = new ArrayList<Msg>();
        String sql = "select " + MessageDAO.tableName + ".mid,body,time from " + MessageDAO.tableName + "," + ReliableDAO.tableName
                + " where " + ReliableDAO.tableName + ".mid = " + MessageDAO.tableName + ".mid  and mfrom like \'" + uid1 + "%\' and mto like \'" + uid2 + "%\'";
        Log.v("[getUnsendMessage]", sql);
        try {
            Cursor cur = null;
            cur = SqliteManager2.query(sql);

            while (cur.moveToNext()) {
                Msg message = new Msg(cur.getString(0), uid1, cur.getString(1), cur.getString(2), "OUT");
                messageList.add(message);
            }
            cur.close();
        } catch (Exception e) {
            Log.e("sqlerror", e.getStackTrace().toString());
        }
        Log.v("messageList", Integer.toString(messageList.size()));
        return messageList;
    }


    public static boolean reReceiveChack(String messageID) {
        return ReliableDAO.reReceiveCheck(messageID) > 0;
    }

    public class UnackMessage extends MessageDAO.ExpendMessage {
        private long timestamp;
        private int count;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

}
