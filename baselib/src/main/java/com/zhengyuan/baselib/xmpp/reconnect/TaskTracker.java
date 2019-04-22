package com.zhengyuan.baselib.xmpp.reconnect;

import android.util.Log;

/**
 * Created by zy on 2017/10/23.
 * Check Task 的实时数量
 */

public class TaskTracker {

    private final String LOG_TAG = "TaskTracker";

    public int count;

    public TaskTracker() {
        this.count = 0;
    }

    public void clear() {
        this.count = 0;
    }

    public synchronized void increase() {
        count++;
        Log.d(LOG_TAG, "Incremented task count to " + count);
    }

    public synchronized void decrease() {
        count--;
        Log.d(LOG_TAG, "Decremented task count to " + count);
    }
}
