package com.zhengyuan.baselib.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.entities.FormFile;

import com.zhengyuan.baselib.http.SocketHttpRequester;

/**
 * 管理文件上传和下载，复制
 *
 * @author kangkang
 */
public class FileManagerUtil {
    public static final String TAG = "FileManagerUtil";

    /**
     * 通过路径字符串截取最后的文件名返回给用户
     */
    public static String getFileName(String filePathString) {
        String fileName = filePathString.substring(filePathString.lastIndexOf("/") + 1, filePathString.length());
        return fileName;

    }

    /**
     * 下载文件时判断文件是否存在
     *
     * @param filePath 文件上传前所在的位置 如 storage/sdcard/test/test0/a.png
     * @return 返回文件所在的路径，若无则为null
     */
    public static String isFileExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {//判断源文件目录下的文件是否存在    如：storage/sdcard/test/test0/1.png
            return filePath;
        } else {
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
            String path = Constants.DOWNLOAD_PATH + fileName;
            if (new File(path).exists()) {//判断软件下载目录下的文件是否存在  如：storage/sdcard/myxmpp/download/1.png
                return path;
            } else {
                return null;
            }
        }
    }

    /**
     * 打开对应目录的文件
     */
    public static Intent openFile(String filePath) {
//		  System.out.println("filepath--->"+filePath);
        File file = new File(filePath);
        if (!file.exists()) return null;
                /* 取得扩展名 */
        String end = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).toLowerCase();
//		        System.out.println("---->end->"+end);
                /* 依扩展名的类型决定MimeType */
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") ||
                end.equals("xmf") || end.equals("ogg") || end.equals("wav")
                || end.equals("amr")) {
            return getAudioFileIntent(filePath);
        } else if (end.equals("3gp") || end.equals("mp4")) {
            return getAudioFileIntent(filePath);
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") ||
                end.equals("jpeg") || end.equals("bmp")) {
            return getImageFileIntent(filePath);
        } else if (end.equals("apk")) {
            return getApkFileIntent(filePath);
        } else if (end.equals("ppt") || end.equals("pptx")) {
            return getPptFileIntent(filePath);
        } else if (end.equals("xls") || end.equals("xlsx")) {
            return getExcelFileIntent(filePath);
        } else if (end.equals("doc")) {
            return getWordFileIntent(filePath);
        } else if (end.equals("pdf")) {
            return getPdfFileIntent(filePath);
        } else if (end.equals("chm")) {
            return getChmFileIntent(filePath);
        } else if (end.equals("txt")) {
            return getTextFileIntent(filePath, false);
        } else {
            return getAllIntent(filePath);
        }
    }

    //Android获取一个用于打开APK文件的intent
    public static Intent getAllIntent(String param) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "*/*");
        return intent;
    }

    //Android获取一个用于打开APK文件的intent
    public static Intent getApkFileIntent(String param) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        return intent;
    }

    //Android获取一个用于打开VIDEO文件的intent
    public static Intent getVideoFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "video/*");
        return intent;
    }

    //Android获取一个用于打开AUDIO文件的intent
    public static Intent getAudioFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "audio/*");
        return intent;
    }

    //Android获取一个用于打开Html文件的intent
    public static Intent getHtmlFileIntent(String param) {

        Uri uri = Uri.parse(param).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param).build();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    //Android获取一个用于打开图片文件的intent
    public static Intent getImageFileIntent(String param) {

//		        Intent intent = new Intent("android.intent.action.VIEW");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "image/*");
        System.out.println("---intent---->" + intent);
        return intent;
    }

    //Android获取一个用于打开PPT文件的intent
    public static Intent getPptFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    //Android获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    //Android获取一个用于打开Word文件的intent
    public static Intent getWordFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    //Android获取一个用于打开CHM文件的intent
    public static Intent getChmFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/x-chm");
        return intent;
    }

    //Android获取一个用于打开文本文件的intent
    public static Intent getTextFileIntent(String param, boolean paramBoolean) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (paramBoolean) {
            Uri uri1 = Uri.parse(param);
            intent.setDataAndType(uri1, "text/plain");
        } else {
            Uri uri2 = Uri.fromFile(new File(param));
            intent.setDataAndType(uri2, "text/plain");
        }
        return intent;
    }

    //Android获取一个用于打开PDF文件的intent
    public static Intent getPdfFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/pdf");
        return intent;
    }

    /**
     * 获取文件路径，19之前和之后
     */
    @TargetApi(19)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            System.err.println("文件查找失败！");
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    /***
     * 复制文件
     * */
    public static void copyFile(final String oldFile, final String newFile) {

        try {
            FileInputStream fis = new FileInputStream(oldFile);
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] buff = new byte[1024];
            while (true) {
                int numread = fis.read(buff);
                if (numread <= 0) {
                    break;
                } else {
                    fos.write(buff, 0, numread);
                }

            }
            // 关闭输入文件流
            if (fis != null) {
                fis.close();
            }
            // 关闭输出文件流
            if (fos != null) {
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @return 返回录制Voice文件格式化后的名字，用来传递给openfire和tomcat
     */
    public static String getAudioFileFormatName() {
        String userId = EMProApplicationDelegate.userInfo.getUserId().split("@")[0];
        SimpleDateFormat df_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df_date.format(new Date());// 获得规整的接收日期
        String[] str = date.split(" ");
        String[] str1 = str[0].split("-");
        String[] str2 = str[1].split(":");
        String expendname = "amr";
        String filename = str1[0] + str1[1] + str1[2] + str2[0] + str2[1]
                + str2[2] + userId + "." + expendname;// 组装文件名

        return filename;
    }

    /**
     * 通过文件后缀名返回格式化之后文件的名称
     */
    public static String getFileFormatName(String expendname) {
        String userId = EMProApplicationDelegate.userInfo.getUserId().split("@")[0];
        SimpleDateFormat df_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df_date.format(new Date());// 获得规整的接收日期
        String[] str = date.split(" ");
        String[] str1 = str[0].split("-");
        String[] str2 = str[1].split(":");
        String filename = str1[0] + str1[1] + str1[2] + str2[0] + str2[1]
                + str2[2] + userId + "." + expendname;// 组装文件名

        return filename;
    }

    /**
     * @return 返回文件格式化后的名字，用来传递给openfire和tomcat
     */
    public static String getFileFormatName(File file) {
        String userId = EMProApplicationDelegate.userInfo.getUserId().split("@")[0];
        SimpleDateFormat df_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df_date.format(new Date());// 获得规整的接收日期
        String[] str = date.split(" ");
        String[] str1 = str[0].split("-");
        String[] str2 = str[1].split(":");
        String expendname = file.getName().substring(
                file.getName().lastIndexOf(".") + 1, file.getName().length());
        String name = file.getName()
                .substring(1, file.getName().lastIndexOf(".")).toString();
        String filename = str1[0] + str1[1] + str1[2] + str2[0] + str2[1]
                + str2[2] + userId + "." + expendname;// 组装文件名

        return filename;
    }

    public static boolean uploadFileByUrl(File file, String fileFormatName,
                                          String url) {
        Log.i(TAG, "upload start");
        boolean uploadflag = false;
        try {
            String requestUrl = url;
            // 请求普通信息
            Map<String, String> params = new HashMap<String, String>();
            params.put("username", EMProApplicationDelegate.userInfo.getUserId());

            params.put("fileName", fileFormatName);
            params.put("filefunction", "uploadImage");
            // 上传文件
            FormFile formfile = new FormFile(fileFormatName, file, "image",
                    "application/octet-stream");
            Log.i(TAG, "upload ready");
            uploadflag = SocketHttpRequester.uploadFile(requestUrl, params,
                    new FormFile[]{formfile});
            Log.i(TAG, "upload success");
        } catch (Exception e) {
            Log.i(TAG, "upload error");
            e.printStackTrace();
            uploadflag = false;
        }
        Log.i(TAG, "upload end");
        return uploadflag;
    }

    /**
     * @param file           文件
     * @param fileFormatName 从format中获取的文件名
     * @param filefunction   文件功能
     * @return ture 成功 false 失败
     */
    public static boolean uploadFile(File file, String fileFormatName,
                                     String filefunction) {
        Log.i(TAG, "upload start");
        boolean uploadflag = false;
        try {
            String requestUrl = Constants.UploadBaseuUrl;
            // 请求普通信息
            Map<String, String> params = new HashMap<String, String>();
            params.put("username", EMProApplicationDelegate.userInfo.getUserId());

            params.put("fileName", fileFormatName);
            params.put("filefunction", filefunction);
            // 上传文件
            FormFile formfile = new FormFile(fileFormatName, file, "image",
                    "application/octet-stream");
            Log.i(TAG, "upload ready");
            uploadflag = SocketHttpRequester.post(requestUrl, params, formfile);
            Log.i(TAG, "upload success");
        } catch (Exception e) {
            Log.i(TAG, "upload error");
            e.printStackTrace();
            uploadflag = false;
        }
        Log.i(TAG, "upload end");
        return uploadflag;
    }

    public void createFile(String srcfile) {
        File file = new File(srcfile);
        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取用户头像要存的文件名
     */
    public static String getHeadFormatName(File file) {
        String userId = EMProApplicationDelegate.userInfo.getUserId().split("@")[0];
        SimpleDateFormat df_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df_date.format(new Date());// 获得规整的接收日期
        String[] str = date.split(" ");
        String[] str1 = str[0].split("-");
        String[] str2 = str[1].split(":");
        // String
        // expendname=file.getName().substring(file.getName().lastIndexOf(".")+1,file.getName().length());
        // System.out.println("lastIndexOf="+file.getName().lastIndexOf(".")+",expendname="+expendname);
        // String
        // name=file.getName().substring(1,file.getName().lastIndexOf(".")).toString();
        String filename = str1[0] + str1[1] + str1[2] + str2[0] + str2[1]
                + str2[2] + userId + "head.png";// 组装文件名

        return filename;
    }

    /**
     * 2016-12-21
     * 下载之后文件需要存储的位置 如
     *
     * @param urlString   如http://10.2.41.52:8088/imag/chat/a.jpg
     * @param newFileName 下载之后文件需要存储的位置 如 a.png
     */
    public static void downloadFile(final String urlString, String newFileName) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConn = (HttpURLConnection) url
                    .openConnection();
            InputStream is = urlConn.getInputStream();
            if (is != null) {
//				String fileName = urlString.substring(
//						urlString.lastIndexOf("/") + 1,
//						urlString.lastIndexOf(".")).toString();
//				String expendName = urlString.substring(
//						urlString.lastIndexOf(".") + 1, urlString.length())
//						.toString();
                String path = Constants.DOWNLOAD_PATH;
                File mediaStorageDir = new File(path);
                if (!mediaStorageDir.exists())
                    mediaStorageDir.mkdirs();
                File file = new File(path + newFileName);
//				File file = new File(path + fileName + "." + expendName);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buff = new byte[1024];
                while (true) {
                    int numread = is.read(buff);
                    if (numread <= 0) {
                        break;
                    } else {
                        fos.write(buff, 0, numread);
                    }

                }
                fos.close();//
            }
            is.close();
            urlConn.disconnect();

        } catch (FileNotFoundException e) {

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
