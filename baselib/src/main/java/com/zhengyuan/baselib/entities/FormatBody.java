package com.zhengyuan.baselib.entities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import android.util.Xml;

import com.zhengyuan.baselib.utils.TimeRenderUtil;

public class FormatBody {
	
	 private Type type=Type.text;
	 private String content;
	 private String datetime;
	 private String uri;
	 //下面的字段用在表单中
		private String form_id;
		private String emp_id;
		private String name;
		private String department;
		private String instoredatetime;
		private String project_id;
		private String project_name;
		private String top_worksheet_id;
		private String top_material_id;
		private String top_material_name;
		private String instore_worksheet_id;
		private String material_id;
		private String material_name;
		private String serial_id;
		private int in_store_count;
		private String warehouse;
		private String destination;
		private String location;
	 
	 
	 
	 public FormatBody(){
		 
	 }
	 
	 
	 public void readXml(String s)
	 {
		 XmlPullParser parser=Xml.newPullParser();
		 InputStream in=new ByteArrayInputStream(s.getBytes());
			Form f=null;
			List<Form> flist=null;
		 try {
			parser.setInput(in, "UTF-8");
			int eventType=parser.getEventType();
			while(eventType!=XmlPullParser.END_DOCUMENT){
				switch(eventType){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String ss=parser.getName();
					if(ss.equalsIgnoreCase("mybody"))
					{
						String t=parser.getAttributeValue(null, "type");
					
						if(t.equalsIgnoreCase("form")){
							//初始化
							flist=new ArrayList<Form>();
							this.setType(Type.form);
						}
							if(t.equalsIgnoreCase("image"))
								this.setType(Type.image);
							
							
						datetime=parser.getAttributeValue(null, "datatime");
					}
					if(ss.equalsIgnoreCase("content")){
						this.setContent(parser.nextText());
					}
					if(ss.equalsIgnoreCase("uri")){
						this.setUri(parser.nextText());
					}
					if(ss.equalsIgnoreCase("formlist")){
						f=new Form();
					}
					if(ss.equalsIgnoreCase("emp_id")){
						f.setEmp_id(parser.nextText());
					}
					if(ss.equalsIgnoreCase("name")){
						f.setName(parser.nextText());
					}
					if(ss.equalsIgnoreCase("department")){
						f.setDepartment(parser.nextText());
					}
					if(ss.equalsIgnoreCase("instoredatetime")){
						f.setInstoredatetime(parser.nextText());
					}
					if(ss.equalsIgnoreCase("project_id")){
						f.setProject_id(parser.nextText());
					}
					if(ss.equalsIgnoreCase("project_name")){
						f.setProject_name(parser.nextText());
					}
					if(ss.equalsIgnoreCase("top_material_id")){
						f.setTop_material_id(parser.nextText());
					}
					if(ss.equalsIgnoreCase("top_material_name")){
						f.setTop_material_name(parser.nextText());
					}
					if(ss.equalsIgnoreCase("instore_worksheet_id")){
						f.setInstore_worksheet_id(parser.nextText());
					}
					if(ss.equalsIgnoreCase("material_id")){
						f.setMaterial_id(parser.nextText());
					}
					if(ss.equalsIgnoreCase("material_name")){
						f.setMaterial_name(parser.nextText());
					}
					if(ss.equalsIgnoreCase("serial_id")){
						f.setSerial_id(parser.nextText());
					}
					if(ss.equalsIgnoreCase("in_store_count")){
						f.setIn_store_count(Integer.parseInt(parser.nextText()));
					}
					if(ss.equalsIgnoreCase("warehouse")){
						f.setWarehouse(parser.nextText());
					}
					if(ss.equalsIgnoreCase("destination")){
						f.setDestination(parser.nextText());
					}
					if(ss.equalsIgnoreCase("location")){
						f.setLocation(parser.nextText());
					}					
					break;
				case XmlPullParser.END_TAG:
					if(parser.getName().equalsIgnoreCase("formlist")&&f!=null){
						flist.add(f);
						f=null;
					}
				break;
				}
				eventType=parser.next();
			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }


	 public String toXML()
	  {
		 //按照类型为image,video,text,voice,form 来封装不同类型的xml格式
	    StringBuilder localStringBuilder = new StringBuilder();	    
	    localStringBuilder.append("<mybody");
	    //消息类型为image或者text类型
	    if (getType()==type.text||getType()==type.image) {
	    //封装格式为<body type="" datetime=""></body>
	      localStringBuilder.append(" type=\"").append(getType()).append("\"");
	    if(getDatetime()!=null)
	    	localStringBuilder.append(" datetime=\"").append(getDatetime()).append("\"");
	      localStringBuilder.append(">");
	     if(getContent()!=null){
	    	 localStringBuilder.append("<content>").append(getContent()).append("</content>"); 
	    	 if(getUri()!=null){
		    	 localStringBuilder.append("<uri>").append(getUri()).append("</uri>"); 
	    	 	}
	     	}
	    }

	    if (getType()==type.form) {
		    //封装格式为<body type=""></body>
		      localStringBuilder.append(" type=\"").append(getType()).append("\"");
		      if(getDatetime()!=null)
			    	localStringBuilder.append(" datetime=\"").append(getDatetime()).append("\"");
		      if(getForm_id()!=null)
		    	  localStringBuilder.append(" form_id=\"").append(getForm_id()).append("\"");
		      localStringBuilder.append(">");
		      localStringBuilder.append("<formlist>");
		     if(getEmp_id()!=null){
		    	 localStringBuilder.append("<emp_id>").append(getEmp_id()).append("</emp_id>"); 
		     }
		    	 if(getName()!=null){
			    	 localStringBuilder.append("<name>").append(getName()).append("</name>"); 
		    	 	}
		    	 if(getDepartment()!=null){
			    	 localStringBuilder.append("<department>").append(getDepartment()).append("</department>"); 
		    	 	}
		    	 if(getInstoredatetime()!=null){
			    	 localStringBuilder.append("<instoredatetime>").append(getInstoredatetime()).append("</instoredatetime>"); 
		    	 	}
		    	 if(getProject_id()!=null){
			    	 localStringBuilder.append("<project_id>").append(getProject_id()).append("</project_id>"); 
		    	 	}
		    	 if(getProject_name()!=null){
			    	 localStringBuilder.append("<project_name>").append(getProject_name()).append("</project_name>"); 
		    	 	}
		    	 if(getTop_worksheet_id()!=null){
			    	 localStringBuilder.append("<top_worksheet_id>").append(getTop_worksheet_id()).append("</top_worksheet_id>"); 
		    	 	}
		    	 if(getTop_material_id()!=null){
			    	 localStringBuilder.append("<top_material_id>").append(getTop_material_id()).append("</top_material_id>"); 
		    	 	}
		    	 if(getTop_material_name()!=null){
			    	 localStringBuilder.append("<top_material_name>").append(getTop_material_name()).append("</top_material_name>"); 
		    	 	}
		    	 if(getInstore_worksheet_id()!=null){
			    	 localStringBuilder.append("<instore_worksheet_id>").append(getInstore_worksheet_id()).append("</instore_worksheet_id>"); 
		    	 	}
		    	 if(getMaterial_id()!=null){
			    	 localStringBuilder.append("<material_id>").append(getMaterial_id()).append("</material_id>"); 
		    	 	}
		    	 if(getMaterial_name()!=null){
			    	 localStringBuilder.append("<material_name>").append(getMaterial_name()).append("</material_name>"); 
		    	 	}
		    	 if(getSerial_id()!=null){
			    	 localStringBuilder.append("<serial_id>").append(getSerial_id()).append("</serial_id>"); 
		    	 	}
		    	 if(getIn_store_count()!=0){
			    	 localStringBuilder.append("<in_store_count>").append(getIn_store_count()).append("</in_store_count>"); 
		    	 	}
		    	 if(getWarehouse()!=null){
			    	 localStringBuilder.append("<warehouse>").append(getWarehouse()).append("</warehouse>"); 
		    	 	}
		    	 if(getDestination()!=null){
			    	 localStringBuilder.append("<destination>").append(getDestination()).append("</destination>"); 
		    	 	}
		    	 if(getLocation()!=null){
			    	 localStringBuilder.append("<location>").append(getLocation()).append("</location>"); 
		    	 	}
		    	 localStringBuilder.append("</formlist>");
		     	
		    }
	    localStringBuilder.append("</mybody>");
	    return localStringBuilder.toString();
	  }



	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDatetime() {
		return TimeRenderUtil.getDate();
	}

	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getForm_id() {
		return form_id;
	}

	public void setForm_id(String form_id) {
		this.form_id = form_id;
	}

	public String getEmp_id() {
		return emp_id;
	}

	public void setEmp_id(String emp_id) {
		this.emp_id = emp_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getInstoredatetime() {
		return instoredatetime;
	}

	public void setInstoredatetime(String instoredatetime) {
		this.instoredatetime = instoredatetime;
	}

	public String getProject_id() {
		return project_id;
	}

	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}

	public String getProject_name() {
		return project_name;
	}

	public void setProject_name(String project_name) {
		this.project_name = project_name;
	}

	public String getTop_worksheet_id() {
		return top_worksheet_id;
	}

	public void setTop_worksheet_id(String top_worksheet_id) {
		this.top_worksheet_id = top_worksheet_id;
	}

	public String getTop_material_id() {
		return top_material_id;
	}

	public void setTop_material_id(String top_material_id) {
		this.top_material_id = top_material_id;
	}

	public String getTop_material_name() {
		return top_material_name;
	}

	public void setTop_material_name(String top_material_name) {
		this.top_material_name = top_material_name;
	}

	public String getInstore_worksheet_id() {
		return instore_worksheet_id;
	}

	public void setInstore_worksheet_id(String instore_worksheet_id) {
		this.instore_worksheet_id = instore_worksheet_id;
	}

	public String getMaterial_id() {
		return material_id;
	}

	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}

	public String getMaterial_name() {
		return material_name;
	}

	public void setMaterial_name(String material_name) {
		this.material_name = material_name;
	}

	public String getSerial_id() {
		return serial_id;
	}

	public void setSerial_id(String serial_id) {
		this.serial_id = serial_id;
	}

	public int getIn_store_count() {
		return in_store_count;
	}

	public void setIn_store_count(int in_store_count) {
		this.in_store_count = in_store_count;
	}

	public String getWarehouse() {
		return warehouse;
	}

	public void setWarehouse(String warehouse) {
		this.warehouse = warehouse;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public static enum Type{
		 video,image,text,voice,form;
	 }
	
	
	

	
}




