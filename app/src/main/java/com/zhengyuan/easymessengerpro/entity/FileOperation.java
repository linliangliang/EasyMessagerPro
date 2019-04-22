package com.zhengyuan.easymessengerpro.entity;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class FileOperation {

	String path;
	static HashMap<String,String>map = new HashMap<String,String>();
	
	public FileOperation(String path){
		
		this.path = path;
	}
	
	//读取文本文件的内容
	public static HashMap<String,String> ReadTxtFile(String filepath){
		
		String path = filepath;		
		
		//打开文件
		File file = new File(path);
		//判断非目录
		if(!file.isDirectory()){
			
			Log.d("123:", "456");
			try{
				Log.d("进入：", "程序运行至此！");
				Log.d(file.getName(), file.getAbsolutePath());
				InputStream instream = new FileInputStream(file);
				if(instream!=null){
					
					Log.d("a:","b");
					InputStreamReader inputreader = new InputStreamReader(instream);
					BufferedReader buffereader = new BufferedReader(inputreader);
					String line;
					
					//分行读取
					while((line=buffereader.readLine())!=null){
						
						String[] tem = line.split(" ");
						map.put(tem[0],tem[1]);
					}
				}
			}catch(java.io.FileNotFoundException e){
				
				
			}
			catch(IOException e){
				
				
			}
		}
		return map;
	}
}
