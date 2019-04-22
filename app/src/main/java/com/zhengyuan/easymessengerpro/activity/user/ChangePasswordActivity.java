package com.zhengyuan.easymessengerpro.activity.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.network.DataObtainer;
import com.zhengyuan.easymessengerpro.util.LogUtil;
import com.zhengyuan.easymessengerpro.xmpp.XmppManager;

import org.jivesoftware.smack.XMPPConnection;

import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;

/**
 * 修改密码界面
 */
public class ChangePasswordActivity extends Activity {

    XMPPConnection xmppCon = XmppManager.getConnection();

    EditText newPasswordEditText;
    EditText newPassword2EditText;
    EditText oldPasswordEditText;
    Button changePassBtn;
    ImageButton backBtn;

    String newPassString;
    String newPass2String;
    String oldPassString;

    SweetAlertDialog dialog;

    boolean forceChangePassowrd = false;//true超过30没有修改密码，强制修改密码;false 主动更新密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Constants.contexts.add(this);

        Intent intent = getIntent();
        String forceChange = intent.getStringExtra("forceChange");
        LogUtil.i("test==", "forceChange" + forceChange);
        if (forceChange != null && "true".equals(forceChange)) {//强制修改密码
            Toast.makeText(this, "密码超过30天未更新，请及时更新", Toast.LENGTH_SHORT).show();
            forceChangePassowrd = true;
        }

        LogUtil.i("test==", "" + forceChangePassowrd);


        newPasswordEditText = findViewById(R.id.new_pass);
        newPassword2EditText = findViewById(R.id.new_pass2);
        oldPasswordEditText = findViewById(R.id.old_pass);
        changePassBtn = findViewById(R.id.change_password_btn);
        backBtn = findViewById(R.id.add_reback_btn);
        changePassBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                newPassString = newPasswordEditText.getText().toString().trim();
                newPass2String = newPassword2EditText.getText().toString().trim();
                oldPassString = oldPasswordEditText.getText().toString().trim();

                if (newPassString.equals("") || oldPassString.equals("") || newPass2String.equals("")) {
                    Toast.makeText(ChangePasswordActivity.this, "还有没输入的哦$_$", Toast.LENGTH_SHORT).show();
                } else {
                    if (!newPassString.equals(newPass2String)) {
                        Toast.makeText(ChangePasswordActivity.this, "新密码不一致-_-|||", Toast.LENGTH_SHORT).show();
                    } else if (!EMProApplicationDelegate.userInfo.getPassword().equals(oldPassString)) {
                        Toast.makeText(ChangePasswordActivity.this, "原密码错误⊙﹏⊙b汗", Toast.LENGTH_SHORT).show();
                    } else if (EMProApplicationDelegate.userInfo.getUserId().equals(newPassString)) {
                        Toast.makeText(ChangePasswordActivity.this, "密码不能与用户名相同-_-|||", Toast.LENGTH_SHORT).show();
                    } else {
                        //防止连续点击
                        changePassBtn.setClickable(false);
                        showChangePassDialog2();
                    }
                }
            }
        });

        backBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void showChangePassDialog2() {
        dialog = new SweetAlertDialog(ChangePasswordActivity.this, 3);
        dialog.setTitleText("重要提醒");
        dialog.setContentText("确认修改密码？");
        dialog.setCancelable(false);
        dialog.setConfirmText("确认");
        dialog.setCancelText("取消");
        dialog.setConfirmClickListener(new OnSweetClickListener() {

            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                changePassBtn.setClickable(true);
                int alertType = sweetAlertDialog.getAlerType();
                if (alertType == 3) {
                    if (changePassword(xmppCon, newPassString)) {
                        EMProApplicationDelegate.userInfo.setPassword(newPassString);

                        EMProApplicationDelegate.sharedPrefHelper.saveString(Constants.XMPP_PASSWORD, newPassString);
                        dialog.changeAlertType(2);
                        dialog.setTitleText("恭喜");
                        dialog.setContentText("密码修改成功");
                        dialog.showCancelButton(false);
                        dialog.setCancelable(true);

                        //添加修改密码的时间
                        uploadChangePasswordDate();
                        if (forceChangePassowrd == true) {
                            //数据是使用Intent返回
                            Intent intent = new Intent();
                            //把返回数据存入Intent
                            intent.putExtra("result", "true");//按key-value对的形式存入数据
                            //设置返回数据
                            setResult(RESULT_OK, intent);
                            //关闭Activity
                            finish();
                        }
                    } else {
                        dialog.changeAlertType(1);
                        dialog.setTitleText("很遗憾");
                        dialog.setContentText("密码修改失败");
                        dialog.showCancelButton(false);
                        dialog.setCancelable(true);
                    }
                    newPasswordEditText.setText("");
                    newPassword2EditText.setText("");
                    oldPasswordEditText.setText("");
                } else {
                    dialog.dismissWithAnimation();
                }
            }
        });
        dialog.setCancelClickListener(new OnSweetClickListener() {

            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                dialog.dismissWithAnimation();
                changePassBtn.setClickable(true);
            }
        });
        dialog.show();
    }

    /**
     * 修改密码
     *
     * @param connection
     * @param pwd
     * @return
     */
    public boolean changePassword(XMPPConnection connection, String pwd) {
        try {
            connection.getAccountManager().changePassword(pwd);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        changePassBtn.setClickable(true);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Constants.contexts.remove(Constants.contexts.size() - 1);
    }

    /**
     * 更新修改密码的时间
     */
    private void uploadChangePasswordDate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataObtainer.INSTANCE.uploadChangePasswordDate(EMProApplicationDelegate.userInfo.getUserId(), new NetworkCallbacks.SimpleDataCallback() {
                    @Override
                    public void onFinish(boolean isSuccess, String msg, Object data) {
                        //时间更新成功
                    }
                });
            }
        }).start();
    }
}
