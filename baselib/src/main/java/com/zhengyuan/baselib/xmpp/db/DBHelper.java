package com.zhengyuan.baselib.xmpp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;


/**
 *Fun: 数据库管理类，提供SQLite数据库对象
 * 
 *Author: 徐兵
 *Time: 2016-04-21
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "contact.db";
    public static final String TABLE_AVATAR = "the_avatars";
    public static final String TABLE_NORMAL_MESSAGE = "rec_message";
    public static final String TABLE_ADD_FRIEND = "accept_friend";

    public DBHelper(Context context, String name, CursorFactory factory,
                    int version) {
        super(context, EMProApplicationDelegate.userInfo.getUserId().split("@")[0] + DB_NAME, factory, version);

    }

    //第一次时建表
    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建加好友请求的表(只存储接收到的好友请求)
        db.execSQL("create table if not exists " +
                TABLE_ADD_FRIEND +
                "(id integer primary key autoincrement,uid varchar,fid varchar,nickname varchar,time varchar,reason varchar,state integer)");

        //创建好友头像表
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                TABLE_AVATAR +
                "(username nvarchar(64) PRIMARY KEY,theavatars nvarchar(100))");
        //收发消息表
        //DX-改2016/10/18，添加uid字段（标识该消息属于哪个用户）
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                TABLE_NORMAL_MESSAGE +
                " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uid VARVHAR , mid VARVHAR , mfrom VARCHAR, mto VARCHAR,body NTEXT, time VARCHAR ,unread INTEGER default 1)");


        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                "rcheck_message" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uid VARVHAR , mid VARVHAR , mfrom VARCHAR, mto VARCHAR, time VARCHAR ,unread INTEGER default 1)");

        //未成功发送消息确认表，考虑需要经常访问，设置一个单独表
        //mid为外键，参考rec_message表中的消息(id)
        //DX-改2016/10/18，添加uid字段（标识该消息属于哪个用户）
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                "unack_message " +
                " (id INTEGER PRIMARY KEY AUTOINCREMENT, count INTEGER, timeout integer," +
                "uid VARVHAR ,mid INTEGER, foreign key (mid) references rec_message(id))");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("CREATE TABLE IF NOT EXISTS rcheck_message" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uid VARVHAR , mid VARVHAR , mfrom VARCHAR, mto VARCHAR, time VARCHAR ,unread INTEGER default 1)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {

    }
}
