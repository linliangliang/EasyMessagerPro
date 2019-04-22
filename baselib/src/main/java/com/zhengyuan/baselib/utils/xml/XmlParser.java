package com.zhengyuan.baselib.utils.xml;

import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.SparseArray;

/**
 * XmlParser将Message转换成Element对象
 */

public class XmlParser {

    private static SparseArray<Element> cache = new SparseArray<Element>();

    public static Element parse(String xmlStr) {
        Element rootElement = null;
        StringReader input = null;
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            input = new StringReader(xmlStr);
            parser.setInput(input);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                boolean hasNext = false;
                if (eventType == XmlPullParser.START_TAG) {
                    int curDepth = parser.getDepth();
                    Element curElement = new Element(parser.getName());
                    //设置元素属性  
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        curElement.addProperty(parser.getAttributeName(i), parser.getAttributeValue(i));
                    }
                    if (!parser.isEmptyElementTag()) {
                        eventType = parser.next();
                        if (eventType == XmlPullParser.TEXT) {
                            //设置元素内容  
                            curElement.setBody(parser.getText());
                        } else if (eventType == XmlPullParser.START_TAG) {
                            hasNext = true;
                        }
                    }

                    if (rootElement == null) {
                        rootElement = curElement;

                        cache.put(curDepth, curElement);
                    } else {
                        cache.put(curDepth, curElement);

                        Element parentElement = cache.get(curDepth - 1);
                        if (parentElement != null) parentElement.addSubElement(curElement);
                    }
                }
                if (!hasNext) eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                input.close();
            }
            cache.clear();
        }
        return rootElement;
    }

}