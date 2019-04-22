package com.zhengyuan.easymessengerpro.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.zhengyuan.baselib.utils.xml.Element;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 2016-12-15
 * 后台监听用户位置信息，向服务器发送用户的位置,并判断当前用户是否在公司范围
 */
public class LocationService extends Service {
    private List<LatLng> vertexs = new ArrayList<LatLng>();

    private static final String LOGTAG = "LocationService";
    private MyLocationListenner myListener = new MyLocationListenner();
    private LatLng position = null;//用户位置
    private String address = null;//用户位置地址
    private float radius = 999.9f;//用户所在精度半径
    // 定位相关
    private LocationClient mLocClient;

    //发送用户位置定时器
    private Timer timer;

    static LocationService locationService;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public static LocationService getInstance() {
        return locationService;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        locationService = this;
        Log.i(LOGTAG, "onCreate");

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);// 设置定位结果包含地址信息
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(3000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        LatLng l1 = new LatLng(30.635759334041552, 114.2894662393896);
        LatLng l2 = new LatLng(30.63453971372633, 114.28982001392255);
        LatLng l3 = new LatLng(30.633669656622512, 114.28744848737621);
        LatLng l4 = new LatLng(30.634749458453086, 114.28687357184982);
        vertexs.add(l1);
        vertexs.add(l2);
        vertexs.add(l3);
        vertexs.add(l4);

        timer = new Timer();
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //每隔30秒将位置信息发送给服务器
                if (position != null) {
                    Element element = new Element("subapk");
                    element.addProperty("type", "theUserPosition");
//                    Log.i(LOGTAG, "latlng=" + position.latitude + ","
//                            + position.longitude);

                    Message message = handler.obtainMessage();
                    message.what = 0;
                    message.obj = isInPolygon(position, vertexs);
                    message.sendToTarget();

                    element.addProperty("position", "" + position.latitude
                            + "," + position.longitude);
                    Intent intent = new Intent();
                    intent.setAction("LocationBroadCast");
                    intent.putExtra("content", element.toString());
                    intent.putExtra("toUser", "iqreceiver");
                    sendBroadcast(intent);
                }
            }
        }, date, 30000);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
//				Toast.makeText(Constants.contexts.get(Constants.contexts.size()-1), "您当前"+msg.obj+"公司范围内", 1000).show();
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 根据传入的点设置公司范围 latitude/longitude
     */
    public void setCompanyRange(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            String[] point = list.get(i).split("/");
            LatLng latLng = new LatLng(Double.parseDouble(point[0]), Double.parseDouble(point[1]));
            vertexs.add(latLng);
        }
    }

    /**
     * 获取公司范围
     */
    public List<LatLng> getCompanyRange() {
        return vertexs;
    }

    public float getRadius() {
        return radius;
    }

    /**
     * 获取用户地址
     */
    public String getUserAddress() {
        return this.address;
    }

    /**
     * 获取用户位置
     * 结果：latitude/longitude
     * 返回如30.635759334041552/114.2894662393896
     */
    public String getUserPosition() {
        String positionString = null;
        if (position != null) {
            positionString = this.position.latitude + "/" + this.position.longitude;
        }
        return positionString;
    }

    /**
     * 返回用户是否在公司范围内
     */
    public boolean isUserInCompany() {
        if (position == null) {
            return false;
        } else {
            return isInPolygon(position, vertexs);
        }
    }

    /**
     * 判断一个点是否在多边形范围内
     */
    public boolean isInPolygon(LatLng pt, List<LatLng> poly) {
        return SpatialRelationUtil.isPolygonContainsPoint(poly, pt);
    }

    @Override
    public void onDestroy() {
        Log.i(LOGTAG, "onDestroy");
        // 退出时销毁定位
        mLocClient.stop();

        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

    public class MyLocationListenner implements BDLocationListener {

        //        @Override
//        public void onConnectHotSpotMessage(String s, int i) {
//
//        }

        @Override
        public void onReceiveLocation(BDLocation location) {
//	        	System.out.println("locaionservice--test---->111");
            if (location == null) {
                return;
            }
//	        	System.out.println("locaionservice--test---->location=" + " latitude:" + location.getLatitude()  
//	        			 + " longitude:" + location.getLongitude());
//            if (!location.isIndoorLocMode()) {
//                mLocClient.startIndoorMode();// 开启室内定位模式，只有支持室内定位功能的定位SDK版本才能调用该接口
//                Log.i("indoor", "start indoormod");
//            }

            address = location.getAddrStr();
            radius = location.getRadius();
//	        	 Log.i("address", "address:" + address + " latitude:" + location.getLatitude()  
//	        			 + " longitude:" + location.getLongitude() + "---");   
            position = new LatLng(location.getLatitude(), location.getLongitude());
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

}
