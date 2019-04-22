package com.zhengyuan.easymessengerpro.util;

/**
 * @author 林亮
 * @description:
 * @date :2019/2/25 15:08
 */

public class JsonUtil {
    /**
     * 将json字符串中的&quot;换成"
     *
     * @return
     */
    public static String Quot2DoubleQuotationMarks(String jsonStr) {
        if (jsonStr == null || "".equals(jsonStr)) {
            return "";
        }
        jsonStr = jsonStr.replaceAll("&quot;", "\"");
        return jsonStr;
    }

    /**
     * 将json字符串中的"换成&quot;
     *
     * @return
     */
    public static String DoubleQuotationMarks2oQuot(String jsonStr) {
        if (jsonStr == null || "".equals(jsonStr)) {
            return "";
        }
        jsonStr = jsonStr.replaceAll("\"", "&quot;");
        return jsonStr;
    }
}
