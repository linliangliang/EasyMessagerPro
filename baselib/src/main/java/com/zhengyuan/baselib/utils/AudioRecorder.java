package com.zhengyuan.baselib.utils;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.utils.FileManagerUtil;
import com.zhengyuan.baselib.utils.Utils;

/**
 * 录音
 * Created by gpsts on 17-6-16.
 */

public class AudioRecorder {

    private MediaRecorder mRecorder = null;

    private final String LOG_TAG = "AudioRecorder";

    private String mVoiceFileName;
    private String mVoiceName;

    public String[] startRecord() {

        // 设置录音保存路径
        mVoiceName = FileManagerUtil.getAudioFileFormatName();
        String path = Constants.DOWNLOAD_PATH;
        File mediaStorageDir = new File(path);
        if (!mediaStorageDir.exists())
            mediaStorageDir.mkdirs();

        mVoiceFileName = path + mVoiceName;
        System.out.println(mVoiceFileName);
        String state = android.os.Environment.getExternalStorageState();
        if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
            Log.i(LOG_TAG, "SD Card is not mounted,It is  " + state + ".");
        }
        File directory = new File(mVoiceFileName).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            Log.i(LOG_TAG, "Path to file could not be created");
        }
        Utils.showToast("开始录音");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mRecorder.setOutputFile(mVoiceFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mRecorder.start();

        return new String[]{ mVoiceFileName, mVoiceName};
    }

    public void stopRecord() {

        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        Utils.showToast("保存录音" + mVoiceFileName);
    }
}
