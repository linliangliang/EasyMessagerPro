package com.zhengyuan.baselib.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeRenderUtil {

    public static String getDate(String format) {

        SimpleDateFormat formatBuilder = new SimpleDateFormat(format);
        return formatBuilder.format(new Date());
    }

    public static String getDate() {
//		return getDate("MM-dd  hh:mm:ss");
        //2016-12-19改为24小时
        return getDate("yyyy-MM-dd  HH:mm:ss");
    }

    public static String stringToDate(String str) throws ParseException {
        String time = "";
        SimpleDateFormat formatBuilder = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat formatBuilder2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = formatBuilder2.format(formatBuilder.parse(str));
        return time;
    }
}