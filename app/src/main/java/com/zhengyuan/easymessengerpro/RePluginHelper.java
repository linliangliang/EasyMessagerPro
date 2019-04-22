package com.zhengyuan.easymessengerpro;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;
import com.zhengyuan.baselib.utils.Utils;
import com.zhengyuan.easymessengerpro.entity.PluginEntity;
import com.zhengyuan.easymessengerpro.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zy on 2017/10/25.
 * RePlugin使用的辅助类
 */

public enum RePluginHelper {

    INSTANCE;

    private static final String TAG = "RePluginHelper";
    // installedPluginInfos 存储掉方法获取的插件列表,但是有重复
    // installedPurePluginInfos 为没有重复的插件列表
    public List<PluginInfo> installedPluginInfos;
    public List<PluginInfo> installedPurePluginInfos = new ArrayList<>();
    public HashMap<String, PluginInfo> installedPurePluginsMap = new HashMap<>();

    /**
     * 初始化已安装插件信息
     */
    public void initInstalledPluginInfos() {

        installedPluginInfos = RePlugin.getPluginInfoList();

        installedPurePluginInfos.clear();
        installedPurePluginsMap.clear();

        for (PluginInfo pluginInfo : installedPluginInfos) {
//            Log.d(TAG, pluginInfo.toString());

            if (!installedPurePluginsMap.keySet().contains(pluginInfo.getName())) {

                installedPurePluginInfos.add(pluginInfo);
                //当不是内置插件的时候,getName获取的是包名,这时要指定alias(别名)
                // 内直插件不设置alias时,默认为jar名称
//                Log.d(TAG, "getType: " + pluginInfo.getType() + " " + PluginInfo.TYPE_BUILTIN);
                if (pluginInfo.getType() == PluginInfo.TYPE_BUILTIN)
                    installedPurePluginsMap.put(pluginInfo.getName(), pluginInfo);
                else
                    installedPurePluginsMap.put(pluginInfo.getAlias(), pluginInfo);
            }
        }
//        for (String temp: installedPurePluginsMap.keySet()) {
//            Log.d(TAG, "map name: " + temp);
//        }
    }

    public List<PluginEntity> serverPluginEntities = new ArrayList<>();
    public List<PluginEntity> installedPluginEntities = new ArrayList<>();
    public HashMap<String, String> realName2ShowNameMaps = new HashMap<>();

    /**
     * 为了解决插件卸载需要重启的问题，定义一个deletedPluginEntities的List, 在主界面显示的时候，不显示这些插件达到卸载的目的
     public List<PluginEntity> deletedPluginEntities = new ArrayList<>();*/

    /**
     * 初始化从服务器获取插件信息
     *
     * @param xmppArray 插件列表信息，以";"隔开，插件内部信息用","隔开,(为什么不用json呢)
     */
    public void initNetworkPluginInfos(String xmppArray) {

//        String jsonContent = Utils.getFileContentFromSDCard(
//                Utils.getPluginDir() + Constants.PLUGIN_LIST_NAME
//        );

        realName2ShowNameMaps.clear();
        serverPluginEntities.clear();
        installedPluginEntities.clear();

        String[] items = xmppArray.split(";");
        for (String item : items) {
            PluginEntity pluginEntity = new PluginEntity(item);
            serverPluginEntities.add(pluginEntity);
        }

        for (int i = 0; i < serverPluginEntities.size(); i++) {

            realName2ShowNameMaps.put(serverPluginEntities.get(i).realName,
                    serverPluginEntities.get(i).showName);

            if (installedPurePluginsMap.keySet().contains(serverPluginEntities.get(i).realName)) {
                installedPluginEntities.add(serverPluginEntities.get(i));
            }
        }

    }

    public String getPluginStringById(String realName, String stringName) {

        Resources resources = RePlugin.fetchResources(realName);
        if (resources == null) {
            Log.d(TAG, realName + " 无法获取到对应resource对象");
            return null;
        }

        int id = resources.getIdentifier(
                stringName,
                "string",
                RePluginHelper.INSTANCE.getPackageName(realName)
        );
        if (id == 0) {
            Log.d(TAG, realName + " 找不到对应id " + stringName);
            return null;
        }
        return resources.getString(id);
    }

    /**
     * 提供给安装插件的时候使用
     *
     * @param realName 安装插件的名字，根据名字将新安装的插件添加进来
     * @return
     */
    public boolean addToInstalledPluginEntities(String realName) {
        int i = 0;
        boolean flag = false;
        int pluginsCount = RePluginHelper.INSTANCE.serverPluginEntities.size();
        for (; i < pluginsCount; i++) {//找到本次安装的插件在serverPluginEntities中的角标i
            PluginEntity pluginEntity = RePluginHelper.INSTANCE.serverPluginEntities.get(i);
            if (pluginEntity != null) {
                if (pluginEntity.realName.contains(realName)) {
                    flag = true;
                    break;
                }
            }
        }
        installedPluginEntities.add(serverPluginEntities.get(i));//根据i，将获取的PluginEntities对象添加到RePluginHelper.INSTANCE.installedPluginEntities
        installedPluginInfos = RePlugin.getPluginInfoList();//更新installedPluginInfos，包含本次安装的插件
        for (PluginInfo temPluginInfo : installedPluginInfos) {
            if (!installedPurePluginsMap.keySet().contains(temPluginInfo.getName())) {//将刚安装的插件信息添加到installedPurePluginsMap
                installedPurePluginInfos.add(temPluginInfo);
                if (temPluginInfo.getType() == PluginInfo.TYPE_BUILTIN || temPluginInfo.getType() == 1) {//补充temPluginInfo.getType() == 1的条件，不知道为什么，内置插件在第二次调用replugin。getPluginList后，getType() 返回的值为1
                    installedPurePluginsMap.put(temPluginInfo.getName(), temPluginInfo);//内置插件，包名
                    LogUtil.i("test==", temPluginInfo.getName() + "内置插件，包名" + temPluginInfo.getType());
                } else {
                    installedPurePluginsMap.put(temPluginInfo.getAlias(), temPluginInfo);//外置插件别名
                    LogUtil.i("test==", temPluginInfo.getName() + "外置插件别名");
                }
            }
        }
        if (flag) {//找到了
            Utils.showToast("安装成功! 主界面刷新即可生效");
        } else {//没有找到，手动启动程序刷新。
            Utils.showToast("安装成功! 重启程序即可生效");
        }
        return flag;
    }

    /**
     * 获取插件资源
     *
     * @param realName 插件名称
     * @param picDir   图片所在目录: drawable/mipmap
     * @param picName  图片名称: 如ic_launcher
     * @return
     */
    public Drawable getPluginDrawable(String realName, String picDir, String picName) {

        Resources resources = RePlugin.fetchResources(realName);
        if (resources == null) {
            Log.d(TAG, realName + " resources is null");
            return null;
        }

        int id = resources.getIdentifier(
                picName,
                picDir,
                RePluginHelper.INSTANCE.getPackageName(realName)
        );
        if (id == 0) {
            Log.d(TAG, realName + " 找不到对应id " + picDir + " " + picName);
            return null;
        }
        return resources.getDrawable(id);
    }

    /**
     * 获取插件包名
     *
     * @param realName
     * @return
     */
    public String getPackageName(String realName) {
        return installedPurePluginsMap.get(realName).getPackageName();
    }
}
