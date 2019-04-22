package com.zhengyuan.baselib.utils.xml;

import java.util.ArrayList;  
import java.util.HashMap;  
import java.util.List;  
import java.util.Map;  
  
import android.text.TextUtils;  
  
/** 
 * 元素 
 */  
public class Element {  
    /** 属性 */  
    private Map<String, String> properties;  
    /** 子元素 */  
    private List<Element> subEmelent;  
    /** 元素名 */  
    private String elementName;  
    /** 内容 */  
    private String body;

    // 默认名称为"mybody"
    public Element() {
        this.elementName = "mybody";
    }

    public void setType(String type) {
        addProperty("type", type);
    }

    public String getMsg() {
        return getProperty("msg");
    }

    public void setMsg(String type) {

        addProperty("msg", type);
    }

    public String getType() {
        return getProperty("type");
    }

    public Element(String elementName) {  
        this.elementName = elementName;  
    }  
      
    public String getElementName() {  
        return elementName;  
    }  
      
    public void setBody(String body) {  
        this.body = body;  
    }  
      
    public String getBody() {  
        return body;  
    }  
      
    public void addProperty(String propertyName, String propertyValue) {  
        if (properties == null) {  
            properties = new HashMap<String,String>();  
        }  
        properties.put(propertyName, propertyValue);  
    }  
  
    public String getProperty(String propertyName) {  
        return properties.get(propertyName);  
    }  
  
    public Map<String, String> getProperties() {  
        return properties;  
    }  
  
    public String removeProperty(String propertyName) {  
        return properties.remove(propertyName);  
    }  
  
    public void addSubElement(Element subElement) {  
        if (this.subEmelent == null) {  
            this.subEmelent = new ArrayList<Element>();  
        }  
        this.subEmelent.add(subElement);  
    }  
  
    public List<Element> getSubElements() {  
        return subEmelent;  
    }  
  
    public boolean removeSubElement(Element subElement) {  
        return this.subEmelent.remove(subElement);  
    }  
  
    /** 
     * 返回XML格式 
     * @return 
     */  
    public String toXml() {  
        StringBuilder sb = new StringBuilder("<");  
        sb.append(elementName);  
        if (properties != null) {  
            for (Map.Entry<String, String> entry : properties.entrySet()) {  
                sb.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");  
            }  
        }  
        if (TextUtils.isEmpty(body) && subEmelent == null) {  
            sb.append("/>");  
        } else {  
            sb.append(">");  
            if (!TextUtils.isEmpty(body)) {  
                sb.append(body);  
            }  
            if (subEmelent != null) {  
                for (Element elem : subEmelent) {  
                    sb.append(elem.toXml());  
                }  
            }  
            sb.append("</").append(elementName).append(">");  
        }  
        return sb.toString();  
    }  

    @Override  
    public String toString() {  
        return toXml();  
    }  
    
    
    public static enum Type{
   	 video,image,text,voice,form;
   }
}  




