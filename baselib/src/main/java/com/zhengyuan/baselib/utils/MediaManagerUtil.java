package com.zhengyuan.baselib.utils;



import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhengyuan.baselib.constants.Constants;

public class MediaManagerUtil {
	public static MediaPlayer mMediaPlayer = new MediaPlayer();
	/**
	 * 播放语音
	 * @param voiceDir 录音位置	
	 * 
	 * @param voiceImageView 录音需要进行切换动画
	 */
	public static void playMusic(String voiceDir, ImageView voiceImageView) {
		try {
			final AnimationDrawable anim=(AnimationDrawable)voiceImageView.getDrawable();
			if (mMediaPlayer.isPlaying()) {
				anim.stop();
				mMediaPlayer.stop();
			}
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(voiceDir);

			mMediaPlayer.prepare();
			mMediaPlayer.start();
			anim.setOneShot(false);
			anim.start();
			mMediaPlayer.setVolume((float) 0.81, (float) 0.82);
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					Toast.makeText(Constants.contexts.get(Constants.contexts.size()-1), "播放完成", Toast.LENGTH_SHORT).show();
					anim.stop();
					anim.setOneShot(true);//改变为最后一帧
//					anim.
//					anim.setFilterBitmap(true);
					
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
