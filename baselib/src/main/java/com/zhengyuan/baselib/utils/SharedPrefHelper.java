package com.zhengyuan.baselib.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 *
 * Created by HanZhang on 16/5/6.
 */
public class SharedPrefHelper {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static final String SHARED_PREFERENCE_NAME = "EMSharedPre";
    public SharedPrefHelper(Context context) {

        sharedPreferences = context.getSharedPreferences(
                SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public String getString(String key) {

        return sharedPreferences.getString(key, null);
    }

    public void saveString(String key, String content) {

        editor.putString(key, content);
        editor.apply();
    }

    public void saveStrings(Map<String, String> data) {

        for (int i = 0; i < data.size(); i++) {

            editor.putString(data.get("key"), data.get("value"));
        }
        editor.apply();
    }

    public boolean getBool(String key) {

        return sharedPreferences.getBoolean(key, false);
    }

    public void saveBool(String key, boolean value) {

        editor.putBoolean(key, value);
        editor.apply();
    }

    public Long getLong(String key) {
        long value = sharedPreferences.getLong(key, -1);
        return value == -1 ? null : value;
    }

    public void saveLong(String key, Long value) {
        editor.putLong(key,value);
        editor.apply();
    }

    public void remove(String key) {

        editor.remove(key);
        editor.apply();
    }

    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }
}
