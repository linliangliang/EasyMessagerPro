package com.zhengyuan.baselib.xmpp.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;

/**
 *Fun: 数据库类，提供SQLite数据库基本操作
 * 
 *Author: 徐兵
 *Time: 2016-04-21
 */

public class SqliteManager {

    public static SQLiteDatabase sqlitedb = null;
    //	private static SqliteManager sqliteManager=null;
    private static Context context = null;

    public static void openDatabase(Context context) {
        try {
            SqliteManager.context = context;
//			sqlitedb = context.openOrCreateDatabase(DBHelper.DB_NAME, 1, null);
            if (sqlitedb == null)
                sqlitedb = (new DBHelper(context, null, null, 1)).getWritableDatabase();
        } catch (Exception e) {
            Log.v("sqlitedb", "null");
        }
    }

    public static SQLiteDatabase getDatabase(Context context) {

        if (sqlitedb == null) {
            openDatabase(context);

        }
        return sqlitedb;
    }

    /**
     * 在程序退出LocationService时关闭连接
     */
    public static void closeSqitedb() {
        if (sqlitedb != null)
            sqlitedb.close();
        sqlitedb = null;
        Log.i("----->SqliteManager", "closeSqitedb()");
    }

    //在数据库使用过程中不关闭连接
    public static void closeDatabase() {
//		synchronized(SqliteManager.sqlitedb){
//		if(sqlitedb!=null)
//			sqlitedb.close();
//		sqlitedb=null;
//		}
    }

    public static void createTable(String sql) {
        openDatabase(EMProApplicationDelegate.applicationContext);
        synchronized (SqliteManager.sqlitedb) {
            sqlitedb.execSQL(sql);
            closeDatabase();
        }
    }

    public static void deleteTable(String sql) {
        openDatabase(EMProApplicationDelegate.applicationContext);
        synchronized (SqliteManager.sqlitedb) {
            sqlitedb.execSQL(sql);
            closeDatabase();
        }
    }


    public static Cursor query(String sql) {
        Cursor cursor = null;

        openDatabase(EMProApplicationDelegate.applicationContext);
        synchronized (SqliteManager.sqlitedb) {
            cursor = sqlitedb.rawQuery(sql, null);
        }
        //不能关闭连接数据库
//	    closeDatabase();
        return cursor;
    }

    public static void insert(String sql) {
        openDatabase(EMProApplicationDelegate.applicationContext);
        synchronized (SqliteManager.sqlitedb) {
            sqlitedb.execSQL(sql);
            closeDatabase();
        }
    }

    public static void update(String sql) {
        openDatabase(EMProApplicationDelegate.applicationContext);
        synchronized (SqliteManager.sqlitedb) {
            sqlitedb.execSQL(sql);
            closeDatabase();
        }
    }

    public static void delete(String sql) {
        openDatabase(EMProApplicationDelegate.applicationContext);
        synchronized (SqliteManager.sqlitedb) {
            sqlitedb.execSQL(sql);
            closeDatabase();
        }
    }
}
