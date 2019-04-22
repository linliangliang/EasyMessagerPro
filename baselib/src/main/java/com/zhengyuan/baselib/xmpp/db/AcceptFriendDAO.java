package com.zhengyuan.baselib.xmpp.db;

import java.util.ArrayList;
import java.util.List;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.entities.AddFriendRequest;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.utils.TimeRenderUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * <h3>接收加好请求的数据库管理类(对表<font color=blue>accept_friend</font>进行操作,<br>
 * 该类使用了单例模式，使用<font color=blue>getInstance()</font>方法获取实例)</h3>
 * <b><font color=blue>accept_friend</font>表中字段的解释</b><br>
 * <b>id:</b>该条记录的id<br>
 * <b>uid:</b>当前用户的id（@前面的部分）<br>
 * <b>fid:</b>对方的id（@前面的部分）<br>
 * <b>time：</b>时间（接收到该条消息的时间）<br>
 * <b>nickname:</b>对方的昵称<br>
 * <b>reason:</b>原因（为什么要加你为好友）<br>
 * <b>state:</b>该条消息的状态（是否已经同意了:0、表示还未做处理；1、表示已经同意；2、拒绝）<br>
 */
public class AcceptFriendDAO {
    private String tableName = "accept_friend";
    //	private DBHelper dbHelper;
    private static String currentUserName;
    static SQLiteDatabase db;
    private static AcceptFriendDAO dao;

    public static AcceptFriendDAO getInstance() {
        SqliteManager.openDatabase(EMProApplicationDelegate.applicationContext);
        db = SqliteManager.sqlitedb;
        currentUserName = EMProApplicationDelegate.userInfo.getUserId().split("@")[0];

        if (dao == null) {
            if (Constants.contexts.size() == 0) {
                Log.e("AcceptFriendDAO", "初始化AcceptFriendDAO时，获取不到context");
            }
            dao = new AcceptFriendDAO(Constants.contexts.get(0));//使用第一个Activity作为Context
        }
        return dao;
    }

    private AcceptFriendDAO(Context context) {
//		dbHelper = new DBHelper(context, DBHelper.DB_NAME, null, 1);
        SqliteManager.openDatabase(Constants.contexts.get(Constants.contexts.size() - 1));
        db = SqliteManager.sqlitedb;
    }

    /**
     * 不会插入两条fid重复的记录
     *
     * @param fid
     * @param reason
     * @param nickname
     */
    public void insert(String fid, String reason, String nickname) {
        fid = fid.split("@")[0];
        if (haveThis(fid))//如果已经存在的，则删除原来的
        {
            delete(fid);
        }
        ContentValues values = new ContentValues();
        values.put("uid", currentUserName);
        values.put("fid", fid);
        values.put("time", TimeRenderUtil.getDate());
        values.put("reason", reason);
        values.put("nickname", nickname);
        values.put("state", 0);// 表示未同意状态
        db.insert(tableName, null, values);
    }

    public void insert(AddFriendRequest afq) {
        insert(afq.getFid(), afq.getReason(), afq.getNickname());
    }

    private void changeState(String state, String fid) {
        fid = fid.split("@")[0];
//		SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "update " + tableName + " set state=? where uid=? and fid=?";
        db.execSQL(sql, new String[]{state, currentUserName, fid});
    }

    public void accept(String fid) {
        changeState("" + 1, fid);//改为同意状态
    }

    public void refuse(String fid) {
        changeState("" + 2, fid);//改为拒绝状态
    }

    public void deleteByState(String state) {
//		SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "delete from " + tableName + " where uid=? and state=?";
        db.execSQL(sql, new String[]{currentUserName, state});// 删除fid=fid的数据
    }

    public void deleteAccepted() {
        deleteByState("1");
    }

    public void deleteRefused() {
        deleteByState("2");
    }

    public void delete(String fid) {
        fid = fid.split("@")[0];
//		SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "delete from " + tableName + " where uid=? and fid=?";
        db.execSQL(sql, new String[]{currentUserName, fid});// 删除fid=fid的数据
    }

    /**
     * 删除当前用户的所有请求
     */
    public void deleteAll() {
//		SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "delete from " + tableName + " where uid=?";
        db.execSQL(sql, new String[]{currentUserName});
    }

    /**
     * 获取所有加好友的请求消息，若没有则返回一个size为0的List
     *
     * @return
     */
    public List<AddFriendRequest> queryAll() {
        List<AddFriendRequest> list = new ArrayList<AddFriendRequest>();
//		SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select * from " + tableName + " where uid=?";
        System.out.println("db----->" + db == null);
        Cursor cursor = db.rawQuery(sql, new String[]{EMProApplicationDelegate.userInfo.getUserId().split("@")[0]});
        if (cursor.moveToFirst()) {
            do {
                AddFriendRequest afq = new AddFriendRequest();
                afq.setUid(cursor.getString(cursor.getColumnIndex("uid")));
                afq.setFid(cursor.getString(cursor.getColumnIndex("fid")));
                afq.setNickname(cursor.getString(cursor.getColumnIndex("nickname")));
                afq.setTime(cursor.getString(cursor.getColumnIndex("time")));
                afq.setReason(cursor.getString(cursor.getColumnIndex("reason")));
                afq.setState(cursor.getInt(cursor.getColumnIndex("state")));
                list.add(afq);
            } while (cursor.moveToNext());
        }
        for (int i = 0; i < list.size(); i++) {
            Log.d("queryAllAddFrie", "fid=" + list.get(i).getFid() + "--uid=" + list.get(i).getUid());
        }
        return list;
    }

    /**
     * 若没有则返回一个size为0的List
     *
     * @return
     */
    public List<AddFriendRequest> queryAccepted() {
        List<AddFriendRequest> list = new ArrayList<AddFriendRequest>();
//		SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select * from " + tableName + " where uid=? and state=?";
        Cursor cursor = db.rawQuery(sql, new String[]{currentUserName, "1"});
        if (cursor.moveToFirst()) {
            do {
                AddFriendRequest afq = new AddFriendRequest();
                afq.setUid(cursor.getString(cursor.getColumnIndex("uid")));
                afq.setFid(cursor.getString(cursor.getColumnIndex("fid")));
                afq.setNickname(cursor.getString(cursor.getColumnIndex("nickname")));
                afq.setTime(cursor.getString(cursor.getColumnIndex("time")));
                afq.setReason(cursor.getString(cursor.getColumnIndex("reason")));
                afq.setState(cursor.getInt(cursor.getColumnIndex("state")));
                list.add(afq);
            } while (cursor.moveToNext());
        }
        return list;
    }

    /**
     * 若没有则返回一个size为0的List
     *
     * @return
     */
    public List<AddFriendRequest> queryRefused() {
        List<AddFriendRequest> list = new ArrayList<AddFriendRequest>();
//		SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select * from " + tableName + " where uid=? and state=?";
        Cursor cursor = db.rawQuery(sql, new String[]{currentUserName, "2"});
        if (cursor.moveToFirst()) {
            do {
                AddFriendRequest afq = new AddFriendRequest();
                afq.setUid(cursor.getString(cursor.getColumnIndex("uid")));
                afq.setFid(cursor.getString(cursor.getColumnIndex("fid")));
                afq.setNickname(cursor.getString(cursor.getColumnIndex("nickname")));
                afq.setTime(cursor.getString(cursor.getColumnIndex("time")));
                afq.setReason(cursor.getString(cursor.getColumnIndex("reason")));
                afq.setState(cursor.getInt(cursor.getColumnIndex("state")));
                list.add(afq);
            } while (cursor.moveToNext());
        }
        return list;
    }

    /**
     * 是否已经存在这条请求了
     *
     * @param fid
     * @return
     */
    public boolean haveThis(String fid) {
//		SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select * from " + tableName + " where uid=? and fid=?";
        Cursor cursor = db.rawQuery(sql, new String[]{currentUserName, fid});
        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
