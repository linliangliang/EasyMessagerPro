package com.zhengyuan.easymessengerpro.activity.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yalantis.ucrop.UCrop;
import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.constants.StaticVariable;
import com.zhengyuan.baselib.utils.FileManagerUtil;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.baselib.utils.ViewUtil;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.views.ProgressDialogManager;
import com.zhengyuan.baselib.xmpp.ChatUtils;
import com.zhengyuan.baselib.xmpp.db.MessageDAO;
import com.zhengyuan.baselib.xmpp.db.SqliteManager;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.util.GlideCircleTransform;
import com.zhengyuan.easymessengerpro.util.ImageUtil;
import com.zhengyuan.easymessengerpro.util.LogUtil;
import com.zhengyuan.easymessengerpro.xmpp.XmppManager;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import static com.zhengyuan.baselib.constants.Constants.DOWNLOAD_PATH;

/**
 * 修改头像界面
 */
@SuppressLint("SdCardPath")
public class ChangeAvatarsActivity extends Activity implements View.OnClickListener {

    private final static String TAG = "ChangeAvatarsActivity";

    public final static int TAKEPHOTO = 158;
    public final static int CHOSE_IMAGE = 159;
    private String imageName = "";
    private String imagePath = DOWNLOAD_PATH;
    private String choosedImageName = "";
    private String choosedImagePath = "";
    private int flag = 0;//flag==1表示图库，flag==2表示拍照
    private Uri ImageUri = null;//记录拍照或者选图后，图片的Uri,用于裁剪

    private ProgressDialogManager progressDialogManager;
    private ImageView iv_personal_icon;
    private MyHandle myHandle = null;


    @Override
    protected void onStart() {
        StaticVariable.inTheAvatarsActivity = true;
        super.onStart();
    }

    @Override
    protected void onRestart() {
        StaticVariable.inTheAvatarsActivity = true;
        super.onRestart();
    }

    @Override
    protected void onPause() {
        StaticVariable.inTheAvatarsActivity = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        StaticVariable.inTheAvatarsActivity = true;
        super.onResume();
    }

    @Override
    protected void onStop() {
        StaticVariable.inTheAvatarsActivity = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        StaticVariable.inTheAvatarsActivity = false;
        super.onDestroy();
        Constants.contexts.remove(Constants.contexts.size() - 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_avatars);
        Constants.contexts.add(this);
        StaticVariable.inTheAvatarsActivity = true;

        progressDialogManager = new ProgressDialogManager(Constants.contexts.get(Constants.contexts.size() - 1));
        Button btn_change = findViewById(R.id.btn_change);
        iv_personal_icon = findViewById(R.id.iv_personal_icon);
        String userId = EMProApplicationDelegate.userInfo.getUserId().toUpperCase();

        MessageDAO messageDAO = new MessageDAO();
        List<String> theavatars = messageDAO.qureyTheAvatarsByUserName(userId);

        if (theavatars.size() == 0 || theavatars.get(0).equals("null")) {
            Glide
                    .with(this)
                    .load(R.drawable.user_avater_default)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transform(new GlideCircleTransform(this))
                    .into(iv_personal_icon);
        } else {
            String taresult = theavatars.get(0);
            Glide
                    .with(this)
                    .load(new File(Constants.DOWNLOAD_PATH + taresult))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transform(new GlideCircleTransform(this))
                    .into(iv_personal_icon);

        }
        btn_change.setOnClickListener(this);
        iv_personal_icon.setOnClickListener(this);

        init();
    }

    /**
     * activity初始化
     */
    private void init() {
        findViewById(R.id.title_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.main_menu_bn).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.title_tv)).setText("修改头像");


        myHandle = new MyHandle(this);
        //解决7.0以上拍照的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }

    /**
     * 组件点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_change:
                changeAvatarDialog();
                break;
            case R.id.iv_personal_icon:
                changeAvatarDialog();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.i(TAG, "onActivityResult:flag=" + flag + " requestCode=" + requestCode + " resultCode" + resultCode);
        switch (requestCode) {
            case CHOSE_IMAGE:
                if (data != null && RESULT_OK == resultCode) {
                    ImageUri = data.getData();
                    String path = ImageUtil.getImageAbstractPath(this, ImageUri);
                    LogUtil.i(TAG, "ImageUtil.getImageAbstractPath :path=" + path);
                    choosedImageName = path.substring(path.lastIndexOf("/") + 1, path.length());
                    choosedImagePath = path.substring(0, path.lastIndexOf("/"));
                    startUCrop(Uri.fromFile(new File(path)));
                }
                break;
            case TAKEPHOTO:
                LogUtil.d(TAG, "TAKEPHOTO:RESULT_OK=" + RESULT_OK + "   data=" + data);
                if (RESULT_OK == resultCode) {
                    startUCrop(ImageUri);
                }
                break;
            //裁剪后的效果
            case UCrop.REQUEST_CROP:
                if (flag == 1) {//选图
                    changeAvatar(choosedImagePath + File.separator + choosedImageName);
                    LogUtil.i(TAG, "choosedImagePath=" + choosedImagePath + "  choosedImageName=" + choosedImageName);
                    LogUtil.i(TAG, "choosedImagePath=" + ImageUri);
                } else if (flag == 2) {//拍照
                    changeAvatar(imagePath + imageName);
                }
                break;
            //错误裁剪的结果
            case UCrop.RESULT_ERROR:
                if (resultCode == RESULT_OK) {
                    final Throwable cropError = UCrop.getError(data);
                    handleCropError(cropError);
                }
                break;
        }
    }

    /**
     * 修改头像
     *
     * @param path 头像路径
     */
    private void changeAvatar(String path) {
        LogUtil.i(TAG, "onActivityResult" + path);
        Glide
                .with(this)
                .load(new File(path))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform(new GlideCircleTransform(this))
                .into(iv_personal_icon);

        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd卡是否可用
            return;
        }
        String userId = EMProApplicationDelegate.userInfo.getUserId(); // 把图片信息写入数据库
        String sql = "update the_avatars set theavatars='" + imageName + "'where username='" + userId + "'";
        SqliteManager.update(sql);

        final File file2 = new File(imagePath, imageName);
        progressDialogManager.showProgressDialog("正在上传头像，请等待。。。");
        new Thread() {
            @Override
            public void run() {
                //将选择的图片拷贝到指定文件夹/myxmpp/download/
                if (flag == 1) {
                    String targetPicPath = Constants.DOWNLOAD_PATH + imageName;
                    // 拷贝到指定文件夹
                    try {
                        Utils.copyFileUsingFileStreams(
                                new File(choosedImagePath, choosedImageName),
                                new File(targetPicPath)
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (flag == 2) {
                    //头像默认在/myxmpp/download/文件夹下面
                }

                boolean res = FileManagerUtil.uploadFile(file2,
                        imageName,
                        "TheAvatars");
                Message message = myHandle.obtainMessage();
                message.what = 0;
                message.obj = res;
                myHandle.sendMessage(message);
            }

        }.start(); // 上传图片
    }


    /**
     * 修改头像的对话框
     */
    private void changeAvatarDialog() {
        imageName = EMProApplicationDelegate.userInfo.getUserId() + "avatar" + System.currentTimeMillis();
        //对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("图片来源");
        builder.setNegativeButton("图库", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                flag = 1;
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, CHOSE_IMAGE);
            }
        });
        builder.setPositiveButton("照相", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                flag = 2;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File resultImage = new File(imagePath, imageName);
                ImageUri = Uri.fromFile(resultImage);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUri);
                startActivityForResult(intent, TAKEPHOTO);
            }
        });
        builder.create().show();
    }

    /**
     * 裁剪图片
     *
     * @param sourceUri 被裁减图像的Uri
     */
    private void startUCrop(Uri sourceUri) {
        LogUtil.i(TAG, "startUCrop begin sourceUri=" + sourceUri);
        UCrop.Options options = new UCrop.Options();
        //裁剪后图片保存在文件夹中
        Uri destinationUri = Uri.fromFile(new File(imagePath, imageName));
        UCrop uCrop = UCrop.of(sourceUri, destinationUri);//第一个参数是裁剪前的uri,第二个参数是裁剪后的uri
        uCrop.withAspectRatio(1, 1);//设置裁剪框的宽高比例
        //下面参数分别是缩放,旋转,裁剪框的比例
        options.setAllowedGestures(com.yalantis.ucrop.UCropActivity.ALL, com.yalantis.ucrop.UCropActivity.NONE, com.yalantis.ucrop.UCropActivity.ALL);
        options.setToolbarTitle("移动和缩放");//设置标题栏文字
        options.setCropGridStrokeWidth(2);//设置裁剪网格线的宽度(我这网格设置不显示，所以没效果)
        //options.setCropFrameStrokeWidth(1);//设置裁剪框的宽度
        options.setMaxScaleMultiplier(3);//设置最大缩放比例
        //options.setHideBottomControls(true);//隐藏下边控制栏
        options.setShowCropGrid(true);  //设置是否显示裁剪网格
        //options.setOvalDimmedLayer(true);//设置是否为圆形裁剪框
        options.setShowCropFrame(true); //设置是否显示裁剪边框(true为方形边框)
        options.setToolbarWidgetColor(Color.parseColor("#ffffff"));//标题字的颜色以及按钮颜色
        options.setDimmedLayerColor(Color.parseColor("#AA000000"));//设置裁剪外颜色
        options.setToolbarColor(Color.parseColor("#000000")); // 设置标题栏颜色
        options.setStatusBarColor(Color.parseColor("#000000"));//设置状态栏颜色
        options.setCropGridColor(Color.parseColor("#ffffff"));//设置裁剪网格的颜色
        options.setCropFrameColor(Color.parseColor("#ffffff"));//设置裁剪框的颜色
        uCrop.withOptions(options);
        uCrop.start(this);
    }

    //处理剪切失败的返回值
    private void handleCropError(Throwable cropError) {
        deleteTempPhotoFile();
        if (cropError != null) {
            Toast.makeText(getApplicationContext(), cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "无法剪切选择图片", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 删除拍照临时文件
     */
    private void deleteTempPhotoFile() {
        File tempFile = new File(Environment.getExternalStorageDirectory() + File.separator + "output_iamge.jpg");
        if (tempFile.exists() && tempFile.isFile()) {
            tempFile.delete();
        }
    }

    /**
     * 处理上传完图片后的事件，静态内部类+弱引用
     */
    public static class MyHandle extends Handler {
        WeakReference<ChangeAvatarsActivity> weakReference = null;

        MyHandle(ChangeAvatarsActivity activity) {
            weakReference = new WeakReference<ChangeAvatarsActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (weakReference == null) {
                return;
            }
            final ChangeAvatarsActivity activity = weakReference.get();
            switch (msg.what) {
                case 0:
                    if ((Boolean) msg.obj) {
                        Element element = new Element("mybody");
                        element.addProperty("type", "uploadTheAvatars");
                        element.addProperty("name", activity.imageName);
                        org.jivesoftware.smack.packet.Message message = new org.jivesoftware.smack.packet.Message();
                        message.setFrom(EMProApplicationDelegate.userInfo.getUserId());
                        message.setTo("iqreceiver@" + XmppManager.getConnection().getServiceName());
                        message.setBody(element.toString());

                        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString());
                        ViewUtil.showToast("上传完成");
                        activity.progressDialogManager.hideProgressDialog();
                    }
                    break;

                default:
                    break;
            }
        }
    }
}
