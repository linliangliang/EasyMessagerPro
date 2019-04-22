package com.zhengyuan.baselib.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;


/**
 * 聊天页面上显示的图片转换。发送的消息为纯字符形式，接收方在本地进行消息格式的转换
 */
public class PicExchangeUtil {
	// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
//	public static String GetImageStr(String path) {
//		
////		String imgFile = uri.getEncodedPath();// 待处理的图片
//		
//		InputStream in = null;
//		byte[] data = null;
//		// 读取图片字节数组
//		try {
//			in = new FileInputStream(path);
//			data = new byte[in.available()];
//			in.read(data);
//			in.close();
//		} catch (IOException e) {
//			Log.v("这里捕捉到错误啦2", "这里捕捉到错误啦");
//			e.printStackTrace();
//		}
//		// 对字节数组Base64编码
//		// BASE64Encoder encoder = new BASE64Encoder();
//		// return "卐"+Base64.encodeToString(data, 0)+"卐";//返回Base64编码过的字节数组字符串
//		return Base64.encodeToString(data, Base64.NO_WRAP);// 返回Base64编码过的字节数组字符串,注意不要换行符
////		return new String(data);
//	}
	/**
	 * 在Bitmap上添加文字
	 * @param mContext
	 * @param resourceId
	 * @param mText
	 * @return
	 */
	public static Bitmap drawTextToBitmap(Context mContext,int resourceId,String mText, int red,int green,int blue)
	{
		try{
			Resources resources=mContext.getResources();
//			float scale=resources.getDisplayMetrics().density;
			float scale=resources.getDisplayMetrics().scaledDensity;
			Bitmap bitmap=BitmapFactory.decodeResource(resources, resourceId);
			Config bitmapConfig=bitmap.getConfig();
			if(bitmapConfig==null)
			{
				bitmapConfig=Config.ARGB_8888;
			}
			
			bitmap=bitmap.copy(bitmapConfig, true);
			Canvas canvas=new Canvas(bitmap);
			Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
			//text color-#3D3D3D
			paint.setColor(Color.rgb(red,green,blue));//设置字体的颜色
			//text size in pixels
//			paint.setTextSize(40*scale);
//			paint.setTextSize((bitmap.getWidth()/2)*scale);
			
			float size=bitmap.getWidth()/2.0f;
			paint.setTextSize(size);
			
			//text shadow
			paint.setShadowLayer(1f,0f,1f,Color.DKGRAY);
			
			//draw text to the Canvas center
			Rect bounds=new Rect();
			paint.getTextBounds(mText, 0, mText.length(), bounds);
			int x=(bitmap.getWidth()-bounds.width())/2;
			int y=(bitmap.getHeight()+bounds.height())/2;
			
			canvas.drawText(mText, x, y, paint);
			return bitmap;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 在Bitmap上添加文字
	 * @param mContext
	 * @param bitmap
	 * @param mText
	 * @return
	 */
	public static Bitmap drawTextToBitmap(Context mContext,Bitmap bitmap,String mText)
	{
		try{
			Resources resources=mContext.getResources();
			float scale=resources.getDisplayMetrics().density;
			Config bitmapConfig=bitmap.getConfig();
			if(bitmapConfig==null)
			{
				bitmapConfig=Config.ARGB_8888;
			}
			
			bitmap=bitmap.copy(bitmapConfig, true);
			Canvas canvas=new Canvas(bitmap);
			Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
			//text color-#3D3D3D
			paint.setColor(Color.rgb(110,110,110));//设置字体的颜色
			//text size in pixels
//			paint.setTextSize(40*scale);
//			paint.setTextSize((bitmap.getWidth()/2)*scale);
			
			float size=bitmap.getWidth()/2.0f;
			
			paint.setTextSize(size/scale);
			//text shadow
			paint.setShadowLayer(1f,0f,1f,Color.DKGRAY);
			//draw text to the Canvas center
			Rect bounds=new Rect();
			paint.getTextBounds(mText, 0, mText.length(), bounds);
			int x=(bitmap.getWidth()-bounds.width())/2;
			int y=(bitmap.getHeight()+bounds.height())/2;
			
			canvas.drawText(mText, x, y, paint);
			return bitmap;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 缩放Bitmap(缩放为指定的长宽)
	 * @param bm
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	public static Bitmap zoomImg(Bitmap bm,int newWidth,int newHeight)
	{
		int width=bm.getWidth();
		int height=bm.getHeight();
		
		float scaleWidth=((float)newWidth)/width;
		float scaleHeight=((float)newHeight)/width;
		
		Matrix matrix=new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbm=Bitmap.createBitmap(bm, 0, 0,width,height,matrix,true);
		return newbm;
	}
	/**
	 * 缩放ImageView(缩放为指定的长宽)
	 * @param iv
	 * @param newWidth
	 * @param newHeight
	 */
	public static void zoomImg(ImageView iv,int newWidth,int newHeight)
	{
//		iv.setImageResource(R.drawable.test);
		LayoutParams params=iv.getLayoutParams();
		params.width=newWidth;
		params.height=newHeight;
		iv.setLayoutParams(params);
	}
/**
 * 将对应路径的图片压缩为10k左右然后转换成base64字符串
 * */
public static String GetImageStr(String srcPath) {
	Bitmap bitmap=getimageByCompress(srcPath);
//		String imgFile = uri.getEncodedPath();// 待处理的图片
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] data=baos.toByteArray();
		Log.v("bytesssss", ""+data.length);
		// 对字节数组Base64编码
		// BASE64Encoder encoder = new BASE64Encoder();
		// return "卐"+Base64.encodeToString(data, 0)+"卐";//返回Base64编码过的字节数组字符串
		return Base64.encodeToString(data, Base64.NO_WRAP);// 返回Base64编码过的字节数组字符串,注意不要换行符
		
//		return new String(data);
	}
	// 将编码字符串转化成为Bitmap并保存到本地
	public static String generateImage(String imgStr, Context context,
			String filepath) throws Exception {
		String filePath = Environment.getExternalStorageDirectory().toString()
				+ "/myxmpp/" + filepath;
		File mediaStorageDir = new File(Environment
				.getExternalStorageDirectory().toString() + "/myxmpp");
		if (!mediaStorageDir.exists())
			mediaStorageDir.mkdirs();
		InputStream input = null;
		// Bitmap bitmap=null;
		// BitmapFactory.Options options=new BitmapFactory.Options();
		// options.inSampleSize=1;
		try {
			if (imgStr == null)
				;
		} catch (Exception e1) {
			throw e1;
		}
		try {
			// imgStr=imgStr.split("卐")[1];
			byte[] b = Base64.decode(imgStr, 0);
			for (int i = 0; i < b.length; i++) {
				if (b[i] < 0)
					b[i] += 256;
			}
			input = new ByteArrayInputStream(b);
			// SoftReference softRef=new
			// SoftReference(BitmapFactory.decodeStream(input, null, options));
			// bitmap=(Bitmap)softRef.get();
			// String timeStamp = new
			// SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			// filePath = "IMG_"+timeStamp+".jpg";
			// filePath=Environment.getExternalStorageDirectory().toString()+File.separator+filePath;
			// filePath=Environment.getExternalStorageDirectory().toString()+File.separator+"2.jpg";
			OutputStream out = new FileOutputStream(filePath);
			out.write(b);
			out.flush();
			out.close();
			if (b != null) {
				b = null;

			}
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				Log.v("这里捕捉到错误啦3", "这里捕捉到错误啦");
				e.printStackTrace();
			}
			// if(bitmap!=null)
			// {

			// createLocalFile(bitmap);
			// bitmap.recycle();
			// System.gc();
			// return bitmap;
			// 将bitmap保存在本地再回收bitmap
			// }

			// 图片加载失败时候显示默认的图片
			// Drawable
			// d=context.getResources().getDrawable(R.drawable.defaultimg);
			// BitmapDrawable bd=(BitmapDrawable)d;
			// return bd.getBitmap();
		} catch (Exception e2) {
			Log.v("这里捕捉到错误啦4", "这里捕捉到错误啦");
			throw e2;
		}
		return filePath;
	}
	 
		// 质量压缩方法
		public static Bitmap compressImage(Bitmap image) {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
			int options = 90;
			while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
				System.out.println("image.length="+baos.toByteArray().length/1024+",option="+options);
				baos.reset();// 重置baos即清空baos
				options -= 10;// 每次都减少10
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中

			}
			ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
			Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
			return bitmap;
		}
		/** 图片按比例大小压缩方法（根据路径获取图片并压缩）：
		 * 
		 * @param srcPath
		 * @param height
		 * @param width
		 * @return
		 */
		public static Bitmap getimageByCompress(String srcPath,float height,float width) {

			float hh = height;// 这里设置高度为800f
			float ww = width;// 这里设置宽度为480f

			return getnarrowimage(srcPath,ww,hh);
		}
		/** 图片按比例大小压缩方法（根据路径获取图片并压缩）：10k
		 * 
		 * @param srcPath
		 * @return
		 */
		public static Bitmap getimageByCompress(String srcPath) {
//			BitmapFactory.Options newOpts = new BitmapFactory.Options();
//			// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
//			newOpts.inJustDecodeBounds = true;
//			Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
//
//			newOpts.inJustDecodeBounds = false;
//			int w = newOpts.outWidth;
//			int h = newOpts.outHeight;
			// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
			float hh = 100f;// 这里设置高度为800f
			float ww = 100f;// 这里设置宽度为480f
//			// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
//			int be = 1;// be=1表示不缩放
//			if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
//				be = (int) (newOpts.outWidth / ww);
//			} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
//				be = (int) (newOpts.outHeight / hh);
//			}
//			if (be <= 0)
//				be = 1;
//			newOpts.inSampleSize = be;// 设置缩放比例
//			// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
//			bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
////			return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
//			return bitmap;
			return getnarrowimage(srcPath,ww,hh);
		}
		private static Bitmap getnarrowimage(String path,float ww,float hh){
			return ThumbnailUtil.extractMiniThumb(path, (int)ww, (int)hh);
		}

//	 
//	 
//	 
//	 public static Drawable getImageDrawable(String filepath) throws IOException{
//		 File f=new File(filepath);
//		 if(!f.exists())
//			 return nul           l;
//		 ByteArrayOutputStream outStream=new ByteArrayOutputStream();
//		 byte bt[] = new byte[100000000];
//		 InputStream in=new FileInputStream(f);
//		 int readLength=in.read(bt);
//		 while(readLength!=-1){
//			 outStream.write(bt, 0, readLength);
//			 readLength=in.read(bt);
//		 }
//		 byte data[]=outStream.toByteArray();
//		 Bitmap bitmap=BitmapFactory.decodeByteArray(data, 0, data.length);
//		 BitmapDrawable bd=new BitmapDrawable(bitmap);
//		return bd;
//		 
//	 }
//	 
//	 
	 
	 
	 
	 

//		 public static Drawable generateImage(String imgStr,Context context)
//		 throws Exception{
//		
//		 Drawable d=context.getResources().getDrawable(R.drawable.defaultimg);
//		 BitmapDrawable bd=(BitmapDrawable)d;
//		 //return bd.getBitmap();
//		 return bd;
//		 }
	 
//	private static void createLocalFile(Bitmap bm){
//		File f=new File(Environment.getExternalStorageDirectory().toString()+"//2.png");
//		if(f.exists()){
//			f.delete();
//		}
//			try {
//				f.createNewFile();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		try{
//			FileOutputStream out=new FileOutputStream(f);
//			bm.compress(Bitmap.CompressFormat.PNG, 90, out);
//			
//			out.flush();
//			out.close();
//		}
//		catch(FileNotFoundException e){
//			e.printStackTrace();
//			
//		}
//		catch(IOException e1){
//			e1.printStackTrace();
//		}
//		
//		
//	}
	 

	/**
	 * 
	 * @param imgStr
	 *            编码后的图片字符串
	 * @param context
	 * @return 返回保存在本地图片的路径
	 * @throws Exception
	 */
//	public static Drawable generateImage(String imgStr, Context context) throws Exception 
//	{
//		String path = null;
//		InputStream input = null;
//		Bitmap bitmap = null;
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inSampleSize = 1;
//		try {
//			if (imgStr == null)
//				return null;
//		} catch (Exception e1) {
//			throw e1;
//		}
//		try {
//			// imgStr=imgStr.split("卐")[1];
//			byte[] b = Base64.decode(imgStr, 0);
//			for (int i = 0; i < b.length; i++) {
//				if (b[i] < 0)
//					b[i] += 256;
//			}
//			// input=new (b);
//			// SoftReference softRef=new
//			// SoftReference(BitmapFactory.decodeStream(input, null, options));
//			// bitmap=(Bitmap)softRef.get();
//			// bitmap=BitmapFactory.decodeByteArray(b, 0, b.length);
//			Log.v("---", "" + b.length);
//
//			// Drawable d = new BitmapDrawable(new ByteArrayInputStream(b));
//			// return d;
//			String imgFilePath = "/sdcard/Pictures/1.jpg";
//			
//			File file = new File(imgFilePath);
//			if(!file.exists())
//			{
//				file.createNewFile();
//			}
//			OutputStream out = new FileOutputStream(file, true);
//			out.write(b);
//			out.flush();
//			out.close();
//			if (b != null) {
//
//				b = null;
//			}
//			try {
//				if (input != null) {
//					input.close();
//				}
//			} catch (IOException e) {
//				Log.v("这里捕捉到错误啦3", "这里捕捉到错误啦");
//				e.printStackTrace();
//			}
//
//			// if(bitmap!=null){
//			// // Drawable d= new BitmapDrawable(bitmap);
//			// // bitmap.recycle();
//			// System.gc();
//			//
//			// return d;
//			// }
//
//			// 图片加载失败时候显示默认的图片
//			// Drawable d=context.getResources().BitmapDrawable("./1.jpeg");
//			context.getResources().
//			BitmapDrawable bd = new BitmapDrawable(imgFilePath);
//			return bd;
//
//		} catch (Exception e2) {
//			Log.v("这里捕捉到错误啦4", "这里捕捉到错误啦");
//			throw e2;
//		}

//	}

	// public static Drawable generateImage(String imgStr,Context context)
	// throws Exception{
	//
	// Drawable d=context.getResources().getDrawable(R.drawable.defaultimg);
	// BitmapDrawable bd=(BitmapDrawable)d;
	// //return bd.getBitmap();
	// return bd;
	// }

//	 public static Bitmap  generateImage1(String imgStr,Context context) throws Exception{	 
//	 InputStream input=null;
//	 Bitmap bitmap=null;
//	 BitmapFactory.Options options=new BitmapFactory.Options();
//	 options.inSampleSize=1;
//		 try{
//		 if(imgStr==null)
//			 return null;
//		 	}catch(Exception e1){
//		 		throw e1;
//		 	}
//		 	try{
//		 		//imgStr=imgStr.split("卐")[1];
//		 		byte[] b=Base64.decode(imgStr, 0);
//		 		for(int i=0;i<b.length;i++)
//		 			{
//		 			if(b[i]<0)
//		 			b[i]+=256;
//		 			}
//		 		input=new ByteArrayInputStream(b);
//		 		SoftReference softRef=new SoftReference(BitmapFactory.decodeStream(input, null, options));
//		 		bitmap=(Bitmap)softRef.get();
//		 		//String imgFilePath=imgFile;
//			 //	OutputStream out=new FileOutputStream(imgFilePath);
//			 //	out.write(b);
//			 //	out.flush();
//			 //	out.close(); 
//		 		if(b!=null){
//		 			b=null;
//		 		}
//		 		try{
//		 			if(input!=null){
//		 				input.close();
//		 			}
//		 		}catch(IOException e){
//		 			Log.v("这里捕捉到错误啦3", "这里捕捉到错误啦");
//		 			e.printStackTrace();
//		 		}
//		 		if(bitmap!=null)
//		 		return bitmap;
//		 		
//		 		//图片加载失败时候显示默认的图片
//		 		Drawable d=context.getResources().getDrawable(R.drawable.defaultimg);
//		 		BitmapDrawable  bd=(BitmapDrawable)d;
//		 		return bd.getBitmap();
//		 			
//		 			
//		 		
//		 	}
//		 	catch(Exception e2){
//		 		Log.v("这里捕捉到错误啦4", "这里捕捉到错误啦");
//		 		throw e2;
//		 	}
//		 	
//	 }

//		public static Bitmap compressImage(Bitmap image) {
//	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//	        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//	        int options = 100;
//	        while ( baos.toByteArray().length / 1024>100) {   
//	            baos.reset();
//	            options -= 10;
//	            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
//	        }
//	        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
//	        Bitmap bitmap = null;
//	        	 bitmap = BitmapFactory.decodeStream(isBm, null, null);
//	        return bitmap;
//	    }
	 
//		public static Bitmap getimage(String srcPath) {
//	        BitmapFactory.Options newOpts = new BitmapFactory.Options();
//	        newOpts.inJustDecodeBounds = true;
//	        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts); 
//	        newOpts.inJustDecodeBounds = false;
//	        int w = newOpts.outWidth;
//	        int h = newOpts.outHeight;
//	        float hh = 800f;
//	        float ww = 480f;
//	        int be = 1;
//	        if (w > h && w > ww) {
//	            be = (int) (newOpts.outWidth / ww);
//	        } else if (w < h && h > hh) {
//	            be = (int) (newOpts.outHeight / hh);
//	        }
//	        if (be <= 0)
//	            be = 1;
//	        newOpts.inSampleSize = be;
//	        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
//	        return compressImage(bitmap);
//	    }
	 
	 public   Bitmap getimage(String srcPath){
		 BitmapFactory.Options opt=new  BitmapFactory.Options();
		 opt.inPreferredConfig=Bitmap.Config.RGB_565;
		 opt.inPurgeable=true;
		 opt.inInputShareable=true;
		 opt.inSampleSize=3;
		 
		 FileInputStream fis=null;
		 try {
			fis=new FileInputStream(srcPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Bitmap	 bitmap=BitmapFactory.decodeStream(fis,null,opt);
		// SoftReference softRef=new SoftReference(BitmapFactory.decodeStream(fis,null,opt));
		// SoftReference softRef=new SoftReference(BitmapFactory.decodeStream(fis,null,opt));
		 SoftReference softRef = null;
	//	 Bitmap	 bitmap = null;
		try {
			 	// bitmap=BitmapFactory.decodeStream(fis,null,opt);
			softRef = new SoftReference(BitmapFactory.decodeFileDescriptor(fis.getFD(),null,opt));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return (Bitmap)softRef.get();
		return (Bitmap) softRef.get();
	 }
	 
	 
	 
	 

	public static SpannableString messageExchange(Bitmap bitmap, String s) {
		//

		// //Bitmap bitmap=getBitmapFormUri(s);
		// SpannableString ss = new SpannableString(s);
		// //得到要显示的图片资源
		// Drawable d =new BitmapDrawable(bitmap);
		// //设置图片宽高
		// d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		// //跨度底部应与周围文本的基线对齐
		// // ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
		// //添加图片
		// // ss.setSpan(span,s.length()-2, s.length()-1,
		// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// // getEditableText().insert(getSelectionStart(), ss);
		// t.setBackground(d);
		SpannableString ss = new SpannableString(s);
		Drawable d = new BitmapDrawable(bitmap);
		d.setBounds(0, 0, d.getIntrinsicWidth() / 2, d.getIntrinsicHeight() / 2);
		// 跨度底部应与周围文本的基线对齐
		ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
		ss.setSpan(span, 0, s.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		return ss;
	}

}
