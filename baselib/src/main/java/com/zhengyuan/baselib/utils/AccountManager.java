package com.zhengyuan.baselib.utils;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.xmpp.BaseXmppManager;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import com.zhengyuan.baselib.xmpp.db.SqliteManager;

/**
 * 账号管理类，管理该账号的各种操作，如好友分组
 *
 * @category 用法：AccountManager.getInstance().xxx();
 */
public class AccountManager {
    private static AccountManager accountManager = null;

    /**
     * 获取AccountManager的实例
     *
     * @return
     */
    public static AccountManager getInstance() {
        if (accountManager == null) {
            accountManager = new AccountManager();
        }
        return accountManager;
    }

    /**
     * 加入群
     *
     * @param roomName 房间名
     * @param nickname 用户昵称
     * @return
     */
    public boolean joinRoom(String roomName, String nickname) {
        XMPPConnection connection = BaseXmppManager.getConnection();
        MultiUserChat muc = new MultiUserChat(connection, roomName
                + "@conference." + connection.getServiceName());
        try {
//			muc.join(nickname, "");
            muc.join(nickname);
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.d("joinRoom", "other_error");
        }
        Log.d("joinMultiUserCh", roomName);
        return true;
    }

    /**
     * 删除好友(存在问题，若该好友为该组最后一个用户，则该组将被删除)
     *
     * @param userName
     * @return
     */
    public boolean removeUser(String userName) {
        Roster roster = BaseXmppManager.getConnection().getRoster();
        try {
            if (userName.contains("@")) {
                userName = userName.split("@")[0];
            }
            RosterEntry entry = roster.getEntry(userName);
            roster.removeEntry(entry);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 通过组名获取所有好友的jid，若组为空则返回的list.size()==0
     *
     * @param groupName
     * @return
     */
    public List<String> getFriendsByGroupName(String groupName) {
        Roster roster = BaseXmppManager.getConnection().getRoster();
        List<String> friendsList = new ArrayList<String>();
        RosterGroup group = roster.getGroup(groupName);
        if (group.getEntryCount() > 0)//当该组下存在好友时
        {
            for (RosterEntry entry : group.getEntries()) {
                friendsList.add(entry.getUser());//将该组下所有好友的jid添加进来
            }
        }
        return friendsList;
    }

    /**
     * 删除某个组下所有好友
     *
     * @param groupName
     * @return
     */
    public boolean removeGroupFriend(String groupName) {
        List<String> friendsList = getFriendsByGroupName(groupName);
        if (friendsList.size() > 0) {
            for (int i = 0; i < friendsList.size(); i++) {
                removeUser(friendsList.get(i));
            }
        }
        return true;
    }

    /**
     * 删除好友（有问题，还是为好友关系）
     *
     * @param friendName
     */
    public void deleteFriend(String friendName) {
        Roster roster = BaseXmppManager.getConnection().getRoster();
    }

    /**
     * 修改组名
     *
     * @param oldGroupName
     * @param newGroupName
     * @return
     */
    public boolean updateGroupName(String oldGroupName, String newGroupName) {
        Roster roster = BaseXmppManager.getConnection().getRoster();
        RosterGroup rosterGroup = roster.getGroup(oldGroupName);
        rosterGroup.setName(newGroupName);
        return true;
//		if(getFriendsCountByGroupName(oldGroupName)>0)
//		{
//			RosterGroup rosterGroup=roster.getGroup(oldGroupName);
//			rosterGroup.setName(newGroupName);
//			return true;
//		}else {
//			roster.reload();//删除空组
//			createGroup(newGroupName);//创建建新的分组
//			return true;
//		}
    }

    /**
     * 修改好友昵称，当传入的groupName=null时，使用原来的groupName（代码和移动分组差不多一样）
     *
     * @param friendName
     * @param nickname
     * @param groupName
     * @return
     */
    public boolean updateFriendNickname(String friendName, String nickname, String groupName) {
        Roster roster = BaseXmppManager.getConnection().getRoster();
        String groupNameString = groupName;
        if (groupNameString == null) {
            groupNameString = getGroupNameByFriendName(friendName);
        }
        try {
            roster.createEntry(friendName, nickname, new String[]{groupNameString});
            System.out.println("更新昵称成功！！");
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("更新昵称失败！！" + e);
            return false;
        }
    }

    /**
     * 将指定好友移动到一个新的分组,当传入的nickname=null时，使用原来的nickname(存在问题，若该好友为该组最后一个用户，则该组将被删除)
     *
     * @param friendName 好友名
     * @param nickname   好友昵称
     * @param newGroupName  组名
     * @return
     */
    public boolean moveGroup(String friendName, String nickname, String newGroupName) {
        Roster roster = BaseXmppManager.getConnection().getRoster();
        String nickNameString = nickname;
        if (nickNameString == null) {
            nickNameString = getFriendNickname(friendName);
        }
        try {
            roster.createEntry(friendName, nickNameString, new String[]{newGroupName});
            System.out.println("好友移动成功！！");
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println("好友移动失败！！" + e);
            return false;
        }
    }

    /**
     * 删除分组/
     *
     * @param groupName    原组名
     * @param newGroupName 新组名（默认将被删除的组下的所有好友移动到newGroupName），若传入null，则移动到Friends组里面
     * @return
     */
    public boolean deleteGroup(String groupName, String newGroupName) {
        Roster roster = BaseXmppManager.getConnection().getRoster();
        if (newGroupName != null && groupName.equals(newGroupName)) {
            return false;
        }
        if ("Friends".equals(groupName)) {//删除Friends组时，不允许传入newGroupName为null
            if (newGroupName == null) {
                return false;
            }
        }
        if (getFriendsCountByGroupName(groupName) == 0)//如果删除的是空组，只需要reload()一下就行了
        {
            roster.reload();
            return true;
        }
        RosterGroup groupTemp = roster.getGroup(groupName);
        //遍历该用户所有的组以及组下的用户，获取该用户所有的好友名
        for (RosterGroup group : roster.getGroups()) {
            if (groupName.equals(group.getName()))//找出指定的组
            {
                if (groupTemp.getEntryCount() > 0)//如果该组下存在好友，才进行操作
                {
                    for (RosterEntry entry : group.getEntries()) {
                        String friendName = entry.getUser().split("@")[0];//获取该用户的JID
                        String friendNickname = entry.getName();//获取该用户的nickName
                        if (newGroupName == null) {
                            moveGroup(friendName, friendNickname, "Friends");
                        } else {
                            moveGroup(friendName, friendNickname, newGroupName);
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 获取好友的nickName，若不存在则返回""
     *
     * @param userName
     * @return
     */
    public String getFriendNickname(String userName) {
        String nickname = "";
        String userNameString = userName.split("@")[0];
        Roster roster = BaseXmppManager.getConnection().getRoster();
        Collection<RosterEntry> entryList = roster.getEntries();
        for (RosterEntry entry : entryList) {
            if (userNameString.equals(entry.getUser().split("@")[0])) {
                nickname = entry.getName();//获取该用户的nickName
                break;
            }
        }
        return nickname;
    }

    /**
     * 获取某个组下的好友数量
     *
     * @param groupName
     * @return
     */
    public int getFriendsCountByGroupName(String groupName) {
        Roster roster = BaseXmppManager.getConnection().getRoster();
        RosterGroup group = roster.getGroup(groupName);
        if (group == null) {
            return 0;
        } else {
            return group.getEntryCount();
        }
    }

    /**
     * 获取某个好友所在的组名，若不存在则返回""
     *
     * @param friendName
     * @return
     */
    public String getGroupNameByFriendName(String friendName) {
        Roster roster = BaseXmppManager.getConnection().getRoster();

//		RosterGroup group=roster.getGroup(friendName);
//		return group.getName();

        for (RosterGroup group : roster.getGroups()) {
            for (RosterEntry entry : group.getEntries()) {
                if (friendName.split("@")[0].equals(entry.getUser().split("@")[0])) {
                    return group.getName();
                }
            }
        }
        return "";
    }

    /**
     * 获取所有组名
     *
     * @return
     */
    public List<String> getAllGroupName() {
        List<String> groupList = new ArrayList<String>();
        Roster roster = BaseXmppManager.getConnection().getRoster();
        for (RosterGroup group : roster.getGroups()) {
            groupList.add(group.getName());
        }
        return groupList;
    }

    /**
     * 判断是否已经存在groupName这个组了
     *
     * @param groupName
     * @return
     */
    public boolean haveThisGroup(String groupName) {
        List<String> groupList = getAllGroupName();
        if (groupList == null) {
            return false;
        } else {
            for (int i = 0; i < groupList.size(); i++) {
                if (groupName.equals(groupList.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 创建一个新的分组
     *
     * @param groupName 新的组的组名
     * @return
     */
    public boolean createGroup(String groupName) {
        Roster roster = BaseXmppManager.getConnection().getRoster();
        if (!haveThisGroup(groupName)) {
            roster.createGroup(groupName);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取当前用户的头像，如果不存在则返回null
     *
     * @return
     */
    public Bitmap getUserPhoto() {
        return getFriendPhoto(EMProApplicationDelegate.userInfo.getUserId().split("@")[0]);
    }

    /**
     * 获取好友的头像，如果不存在则返回null
     *
     * @param userName
     * @return
     */
    public Bitmap getFriendPhoto(String userName) {
        String userString = userName.split("@")[0];
        String path = Environment.getExternalStorageDirectory()
                .toString() + "/myxmpp/" + "downloadfile/";
        String sql = "select theavatars from the_avatars where username='" + userString + "'";
        Cursor cursor = SqliteManager.query(sql);
        System.out.println("getUserPhoto--->DX" + cursor.getCount());
        cursor.moveToFirst();
        if (cursor.getCount() > 0)//判断本地数据库是否存有该用户的头像
        {
            String taresult = cursor.getString(cursor.getColumnIndex("theavatars"));
            if ((taresult == null) || taresult.equals("null")) {
                return null;
            } else {
                Bitmap bt = BitmapFactory.decodeFile(path + taresult);
                return bt;
            }
        } else {
            return null;
        }
    }
}
