package com.zhengyuan.easymessengerpro.activity;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

import com.zhengyuan.easymessengerpro.R;

/**
 * 显示网页
 */
public class LoadWebActivity extends Activity{
	private WebView web;
	private String url;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_web);
		//web = (WebView) findViewById(R.id.web);
		Intent in = getIntent();
		url = in.getStringExtra("url");
		Uri u = Uri.parse(url);
		Intent it = new Intent(Intent.ACTION_VIEW,u);
		startActivity(it);
		//web.getSettings().setJavaScriptEnabled(true);
//		web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//		web.getSettings().setSupportMultipleWindows(true);
		//web.setWebChromeClient(new WebChromeClient());
		//web.getSettings().setDomStorageEnabled(true);
		//web.setWebViewClient(new WebViewClient());
		//web.loadUrl(url);
//		web.setWebViewClient(new WebViewClient(){
//			@Override
//			public boolean shouldOverrideUrlLoading(WebView view, String url) {
//				if(url!=null&&!url.equals("")){
//					view.loadUrl(url);
//				}
//				return true;
//			}
//		});
		
		
	}
	@Override
	protected void onResume() {
		super.onResume();
		this.finish();
	}
	
}
