package com.zhengyuan.baselib.constants;

import android.net.Uri;
import android.os.Handler;

public class StaticVariable {
	//判断是否处于通知页
	public static boolean inNoticePage=false;
    //判断是否处于通知页
	public static Uri  tempImageUri=null;
	/**
	 * 判断是否处于Formclient页面
	 * */
	public static boolean inFormClient=false;
	/**
	 * 判断是否处于VideoCallActivity页面
	 * */
	public static boolean inVideoCallActivity=false;
	/**
	 * 判断是否处于MVideoCallActivity页面
	 */
	public static boolean inMVideoCallActivity=false;
	/**
	 * 判断是否处于MainActivity页面
	 * */
	public static boolean inMainActivity=false;
	/**
	 * 判断是否处于GroupClient页面
	 * */
	public static boolean inGroupClient=false;
	
	/**
	 * 判断是否处于TheAvatarsActivity页面
	 * */
	public static boolean inTheAvatarsActivity=false;
	
	/**
	 * 判断是否处于WorkPlanShowActivity页面
	 * */
	public static boolean inWorkPlanShowActivity=false;
	/**
	 * 判断是否处于SubWorkPlanShowActivity页面
	 * 
	 * */
	public static boolean inSubWorkPlanShowActivity=false;
	/**
	 * 判断是否处于WorkPlanDetailActivity页面
	 * 
	 * */
	public static boolean inWorkPlanDetailActivity=false;
	/**
	 * 判断是否处于ContactMainActivity页面
	 * 
	 * */
	public static boolean inContactMainActivity=false;
//	/**
//	 * 判断是否处于最近消息界面
//	 */
//	public static boolean inContactMessageFragment=false;
	/**
	 * 判断是否处于BatchWorkPlanSubmitActivity页面
	 * 
	 * */
	public static boolean inBatchWorkPlanSubmitActivity=false;
	/**
	 * 判断是否处于BatchQualityTestActivity页面
	 * 
	 * */
	public static boolean inBatchQualityTestActivity=false;
	/**
	 * 判断是否处于RadarViewActivity页面
	 * 
	 * */
	public static boolean inDisplayPosition=false;
	public static Handler handler=null;

	/**
	 * 判断是否处于WorkListScanSendActivity页面
	 */
	public static boolean inWorkListScanSendActivity=false;
}
