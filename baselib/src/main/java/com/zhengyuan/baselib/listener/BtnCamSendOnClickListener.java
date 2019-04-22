package com.zhengyuan.baselib.listener;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;

import com.zhengyuan.baselib.utils.TimeRenderUtil;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.xml.Element;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;

public class BtnCamSendOnClickListener implements OnClickListener {
    private EditText et_picDescribe;
    private Context context;
    private Uri imageFileUri;
    private ImageView iv_camera;
    private LinearLayout ll_talkbar;
    private FrameLayout fl_cameraPreview;
    private GridView gv_functionbutton;
    private GridView gv_face;
    private TableLayout tl_cameraFunction;
    private ListView listview;
    private Handler handler;


    public BtnCamSendOnClickListener(EditText et_picDescribe, Context context,
                                     Uri imageFileUri, ImageView iv_camera, LinearLayout ll_talkbar,
                                     FrameLayout fl_cameraPreview, GridView gv_functionbutton,
                                     GridView gv_face, TableLayout tl_cameraFunction, ListView listview,
                                     Handler handler) {
        super();
        this.et_picDescribe = et_picDescribe;
        this.context = context;
        this.imageFileUri = imageFileUri;
        this.iv_camera = iv_camera;
        this.ll_talkbar = ll_talkbar;
        this.fl_cameraPreview = fl_cameraPreview;
        this.gv_functionbutton = gv_functionbutton;
        this.gv_face = gv_face;
        this.tl_cameraFunction = tl_cameraFunction;
        this.listview = listview;
        this.handler = handler;
    }


    @Override
    public void onClick(View arg0) {

        Utils.showToast("请:" + imageFileUri);

        if (et_picDescribe.getText().toString().length() == 0)
            Utils.showToast("请填写说明文字，再单击发送！");
        else {
            Log.v("path", (imageFileUri == null) ? "空的对象" : imageFileUri.getEncodedPath());
            Log.e("send message", "-1");
            iv_camera.setVisibility(View.VISIBLE);
            ll_talkbar.setVisibility(View.VISIBLE);
            et_picDescribe.setVisibility(View.GONE);
            fl_cameraPreview.setVisibility(View.GONE);
            gv_functionbutton.setVisibility(View.GONE);
            gv_face.setVisibility(View.GONE);
            tl_cameraFunction.setVisibility(View.GONE);
            listview.setVisibility(View.VISIBLE);
            Log.e("send message", "0");
            // /将图片在聊天窗口显示出来,并发送到对端
            // 将图片转成字符串
            // System.out.println(PicExchangeUtil.GetImageStr(imageFileUri));

            //	String s = PicExchangeUtil.GetImageStr(imageFileUri);
            String s = "dasasdasfasdas";
            Element element = new Element("mybody");
            element.addProperty("type", "image");
            element.addProperty("datetime", TimeRenderUtil.getDate());
            Element subelement1 = new Element("content");
            subelement1.setBody(et_picDescribe.getText().toString());
            Element subelement2 = new Element("uri");
            subelement2.setBody(s);
            Element subelement3 = new Element("imgfilename");
            subelement3.setBody(imageFileUri.getEncodedPath().split("/")[imageFileUri
                    .getEncodedPath().split("/").length - 1]);
            element.addSubElement(subelement1);
            element.addSubElement(subelement2);
            element.addSubElement(subelement3);
            et_picDescribe.setText("");
            String args[] = new String[]{
                    EMProApplicationDelegate.userInfo.getUserId(), element.toString(),
                    TimeRenderUtil.getDate(), "OUT"};
            android.os.Message msg = handler.obtainMessage();
            msg.what = 7;
            msg.obj = args;
            msg.sendToTarget();
        }
    }

}
