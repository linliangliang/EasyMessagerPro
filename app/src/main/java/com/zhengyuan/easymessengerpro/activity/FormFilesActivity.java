package com.zhengyuan.easymessengerpro.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.adapter.FileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FormFilesActivity extends Activity{

	private List<String> items = null;
	private List<String> pathlist = null;
	private ListView listview;
	private final String rootpath = "/";
	private String originalpath;
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frmfiles);
		Constants.contexts .add(this);

		//本地附件
		/////本地附件
		listview = findViewById(R.id.frmfiles_listview);
		
		getFileDir(rootpath);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				//取得路径
				File file = new File(pathlist.get(position));
				//isDirectory()是一个文件路径返回true
				if (file.isDirectory())
				{
					try 
					{
						getFileDir(file.getPath());
					}
					catch (Exception e)
					{
						Toast.makeText(FormFilesActivity.this, "该路径下没有附件", Toast.LENGTH_SHORT).show();
						getFileDir(file.getParent());
					}
				} 
				else
				{
					originalpath = file.getPath().toLowerCase();
					onExit();
				}
			}
		});
	}
	//退出附件
	private void onExit() 
	{
		Intent intent = new Intent();
		intent.putExtra("filepath", originalpath);
		setResult(2, intent);
		finish();
	}
	//获取附件显示
	private void getFileDir(String filepath)
	{
		items = new ArrayList<String>();
		pathlist = new ArrayList<String>();
		File sfile = new File(filepath);

		if (!rootpath.equals(filepath))
		{
			items.add("back");
			pathlist.add(sfile.getParent());
		}

		File[] files = sfile.listFiles();
		for (File file : files) 
		{
			items.add(file.getName());
			pathlist.add(file.getPath());
		}
		
		listview.setAdapter(new FileAdapter(this, items, pathlist));
	}
}
