package com.zhengyuan.baselib.xmpp;

import android.util.Log;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.entities.MUCInfo;
import com.zhengyuan.baselib.utils.FileManagerUtil;
import com.zhengyuan.baselib.xmpp.db.MessageDAO;
import com.zhengyuan.baselib.xmpp.db.SqliteManager;

/**
 * IQ Provider 解析接收到的IQ包
 *
 * @author 徐兵
 */

public class MUCPacketExtensionProvider implements IQProvider {

    public static List<String> mucList = new ArrayList<String>();
    private static List<String> resultLsit = new ArrayList<String>();

    public static void clearMucList() {
        mucList.clear();
    }

    public static List<String> getResultSet() {
        return resultLsit;
    }

    public static void setResultSet(List<String> resultSet) {
        MUCPacketExtensionProvider.resultLsit = resultSet;
    }

    public static void clearResultSet() {
        resultLsit.clear();
    }

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        int eventType = parser.getEventType();
        MUCInfo info = null;
        /*
		 * <iq type="result" to="xbb@xxzx/Smack" id="zydq"> <muc xmlns="zydq">
		 * <room xmlns="" account="xbb@xxzx">信息中心@conference.xxzx</room> <room
		 * xmlns="" account="xbb@xxzx">myroom@conference.xxzx</room> </muc>
		 * </iq>
		 */
        while (true) {
            if (eventType == XmlPullParser.START_TAG) {
                if ("room".equals(parser.getName())) {
//					mucList.clear();
                    String account = parser.getAttributeValue("", "account");
                    String room = parser.nextText();
                    System.out.println("房间：" + room);
                    info = new MUCInfo(account, account, room);
                    String result = "";
                    // addMUCInfo(info);
                    for (Iterator iterator = mucList.iterator(); iterator
                            .hasNext(); ) {
                        String type = (String) iterator.next();
                        result = result + "," + type;
                    }
                    if (!result.contains(info.getRoom())) {
                        mucList.add(info.getRoom());
                    }
                    // mucList.add(info.getRoom().split("@")[0]);
                    System.out.println("mucList.size():" + mucList.size());
                } else if ("member".equals(parser.getName())) {
                    Log.v("解析", "member");
                    String roomname = parser.getAttributeValue("", "room");
                    Log.v("[roomName]-roommember", roomname);
                    String members = parser.nextText();
                    Log.v("roomName-[roommember]", members);
                    resultLsit.add(members);
                } else if ("friendsphoto".equals(parser.getName())) {
                    String account = parser.getAttributeValue("", "account");
                    String result = parser.nextText();
                    System.out.println("result=" + result);
                    String[] array = result.split(";");
                    System.out.println("array:" + array.length);
                    for (int i = 0; i < array.length; i++) {
                        String[] array2 = array[i].split("\\+");
                        String usernameString = array2[0];
                        final String theavatars = array2[1];
                        System.out.println("theavatars1:" + theavatars);
                        String path = Constants.DOWNLOAD_PATH;
                        String filepathString = path + theavatars;
                        File file = new File(filepathString);
                        boolean fileExits = false;//文件是否存在
                        boolean dataTableExits = false;//数据库是否存在
                        if (file.exists()) {
                            fileExits = true;
                        } else {
                            fileExits = false;
                        }
//						String sql = "select * from the_avatars where username='"
//								+ usernameString + "' ";
//						Cursor cursor = SqliteManager.query(sql);
                        MessageDAO messageDAO = new MessageDAO();
                        List<String> list = new ArrayList<String>();
                        list = messageDAO.qureyTheAvatarsByUserName(usernameString);
                        System.out.println("cursor.getCount():"
                                + list.size());
                        if (list.size() == 0) {
                            dataTableExits = false;
                        } else {
                            dataTableExits = true;
                        }

                        if (fileExits && !dataTableExits) {// 文件存在但是数据库不存在
                            String sql2 = "insert into the_avatars(username,theavatars) values ('"
                                    + usernameString
                                    + "','"
                                    + theavatars
                                    + "')";
                            System.out.println("sql2:" + sql2);
                            SqliteManager.insert(sql2);
                        } else if (!fileExits && dataTableExits) {// 文件不存在但是数据库存在----是否更新
                            new Thread() {
                                @Override
                                public void run() {
                                    System.out.println("theavatars2:"
                                            + theavatars);
                                    String url = Constants.DownLoadBaseUrl + "/image/TheAvatars/"
                                            + theavatars;
                                    FileManagerUtil.downloadFile(url, theavatars);
                                }

                                ;
                            }.start();
//							String sql_thravatar = "select * from the_avatars where theavatars='"
//									+ theavatars + "' ";
                            List<String> userBytheavatars = messageDAO.qureyTheAvatarsByUserName(theavatars);
//							Cursor cursor_thravatar = SqliteManager
//									.query(sql_thravatar);
                            System.out.println("cursor.getCount():"
                                    + userBytheavatars.size());
                            if (userBytheavatars.size() == 0) {// 更新
                                String sql3 = "update the_avatars set theavatars ='"
                                        + theavatars
                                        + "' where username='"
                                        + usernameString + "'";
                                SqliteManager.update(sql3);
                            } else {//数据一致，不做操作
                            }

                        } else if (!fileExits && !dataTableExits) {// 文件不存在且数据库不存在
                            new Thread() {
                                @Override
                                public void run() {
                                    System.out.println("theavatars2:"
                                            + theavatars);
                                    String url = Constants.DownLoadBaseUrl + "/image/TheAvatars/"
                                            + theavatars;
                                    FileManagerUtil.downloadFile(url, theavatars);
                                }

                                ;
                            }.start();
                            String sql2 = "insert into the_avatars(username,theavatars) values ('"
                                    + usernameString
                                    + "','"
                                    + theavatars
                                    + "')";
                            System.out.println("sql2:" + sql2);
                            SqliteManager.insert(sql2);
                        } else if (fileExits && dataTableExits) {//文件存在数据库存在，判断数据库数据是否一致
//							String sql_thravatar = "select * from the_avatars where theavatars='"
//									+ theavatars + "' ";
                            List<String> userBytheavatars = messageDAO.queryUserNameByTheavatar(theavatars);
//							Cursor cursor_thravatar = SqliteManager
//									.query(sql_thravatar);
                            System.out.println("cursor.getCount():"
                                    + userBytheavatars.size());
                            if (userBytheavatars.size() == 0) {// 更新
                                String sql3 = "update the_avatars set theavatars ='"
                                        + theavatars
                                        + "' where username='"
                                        + usernameString + "'";
                                SqliteManager.update(sql3);
                            } else {//数据一致，不做操作
                            }
                        }
                    }

                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if ("muc".equals(parser.getName()))
                    break;
            }
            eventType = parser.next();
        }
        return null;
    }
}
