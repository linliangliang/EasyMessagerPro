package com.zhengyuan.baselib.utils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gpsts on 17-8-23.
 */

public enum JSONUtil {
    INSTANCE;

    public <T> List<T> jsonArrayToList(JSONArray jsonArray, Class<T> tClass)
            throws JSONException {
        JSONObject item;
        T bean;
        Gson gson = new Gson();
        List<T> entities = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            item = jsonArray.getJSONObject(i);
            bean = gson.fromJson(item.toString(), tClass);
            entities.add(bean);
        }
        return entities;
    }

    public <T> T jsonToObject(JSONObject jsonObject, Class<T> tClass) throws JSONException {

        T bean;
        Gson gson = new Gson();

        bean = gson.fromJson(jsonObject.toString(), tClass);

        return bean;
    }
}