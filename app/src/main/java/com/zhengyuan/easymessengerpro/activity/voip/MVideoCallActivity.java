package com.zhengyuan.easymessengerpro.activity.voip;

import android.app.Activity;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.constants.StaticVariable;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.utils.xml.XmlParser;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.xmpp.XmppManager;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.Base64Encoder;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * Created by Administrator on 17-6-12.
 */

public class MVideoCallActivity extends Activity {

    private static final String LOG_TAG = "MVideoCallActivity";
    public static final String VIDEO_TRACK_ID = "video_track_id";
    public static final String AUDIO_TRACK_ID = "audio_track_id";
    public static final String LOCAL_MEDIA_STREAM_ID = "local_media_stream_id";

    private ChatManager cm;
    private VideoRendererGui.ScalingType scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;
    private GLSurfaceView mGLSurfaceView;

    //聊天室成员
    private String userName;
    private ArrayList<String> members;
    private String oldZhao;   //群聊发起者
    private int total;   //人数
    private String[] orders;   //进入顺序
    private int count;
    private Map<String, PeerConnection> peerConnectionMap = new HashMap<>();
    private Map<String, PCObserver> pcObserverMap = new HashMap<>();
    private Map<String, SDPObserver> sdpObserverMap = new HashMap<>();
    private Map<String, IceCandidate> iceCandidateMap = new HashMap<>();

    private MediaConstraints sdpMediaConstraints;
    private MediaConstraints pcConstraints;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();

    private boolean mIsInited;
    private boolean mIsCalled;
    private PeerConnectionFactory factory;
    private VideoCapturer videoCapturer;
    private VideoSource videoSource;
    private AudioSource audioSource;
    private AudioManager audioManager;
    private MediaStream localMediaStream;
    private VideoTrack localVideoTrack;

    private VideoRenderer localVideoRenderer;
    private Map<String, VideoRenderer> remoteVideoRendererMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_video_call);

        StaticVariable.inMVideoCallActivity = true;
        StaticVariable.handler = this.handler;

        members = getIntent().getStringArrayListExtra("members");
        total = members.size();
        orders = new String[total - 1];
        userName = getIntent().getStringExtra("userName");
        oldZhao = getIntent().getStringExtra("oldZhao");

        //打开扬声器
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);

        mGLSurfaceView = findViewById(R.id.glview_call);
        mGLSurfaceView.setKeepScreenOn(true);

        //chatManager
        cm = XmppManager.getConnection().getChatManager();

        if (!PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true, null)) {
            Log.e(LOG_TAG, "PeerConnectionFactory init fail!");
            return;
        }
        factory = new PeerConnectionFactory();

//        //Media条件信息SDP接口
//        sdpMediaConstraints = new MediaConstraints();
//        //接受远程音频
//        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
//                "OfferToReceiveAudio", "true"));
//        //接受远程视频
//        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
//                "OfferToReceiveVideo", "true"));
//
//        pcConstraints = new MediaConstraints();
//        pcConstraints.optional.add(new MediaConstraints.KeyValuePair(
//                "DtlsSrtpKeyAgreement", "true"));
//        pcConstraints.mandatory.add(new
//                MediaConstraints.KeyValuePair("VoiceActivityDetection", "false"));

        pcConstraints = new MediaConstraints();
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        iceServers.add(new PeerConnection.IceServer("turn:59.175.173.137", "root", "root"));

        //初始化连接
        for (String remoteName : members) {
            if (remoteName.equals(userName))
                continue;

            PCObserver pcObserver = new PCObserver(remoteName);
            pcObserverMap.put(remoteName, pcObserver);

            PeerConnection pc = factory.createPeerConnection(iceServers, pcConstraints, pcObserver);
            peerConnectionMap.put(remoteName, pc);

            SDPObserver sdpObserver = new SDPObserver(remoteName);
            sdpObserver.setPeerConnection(pc);
            sdpObserver.setSdpObserver(sdpObserver);
            sdpObserverMap.put(remoteName, sdpObserver);

        }

        mIsCalled = false;

        boolean offer = getIntent().getBooleanExtra("createOffer", false);

        initialSystem();
        if (!offer) {
            sendAccept();
        } else {
            callRemote();
        }
    }

    private void callRemote() {
        StringBuilder sb = new StringBuilder();
        for (String remoteName : members) {
            sb.append(remoteName + ";");
        }
        //成员名字
        String memberName = sb.substring(0, sb.length() - 1);
        for (String remoteName : members) {
            if (remoteName.equals(userName))
                continue;
            Chat chat = cm.createChat(remoteName + "@" + Constants.SERVER_NAME, null);
            Element element = new Element("video");
            element.addProperty("type", "MVideoCall");
            element.addProperty("members", memberName);
            Log.d(LOG_TAG, "callRemote");
            try {
                chat.sendMessage(element.toString());
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAccept() {

        Chat chat = cm.createChat(oldZhao + "@" + Constants.SERVER_NAME, null);
        Element element = new Element("video");
        element.addProperty("type", "MAcceptCall");
        try {
            chat.sendMessage(element.toString());
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    private void initialSystem() {
        //获取前置摄像头本地视频流
        String frontDeviceName = VideoCapturerAndroid.getNameOfFrontFacingDevice();
        Log.e(LOG_TAG, "CameraName: " + frontDeviceName);
        videoCapturer = VideoCapturerAndroid.create(frontDeviceName);
        if (videoCapturer == null) {
            Log.e(LOG_TAG, "fail to open camera");
            return;
        }

        //视频
        MediaConstraints mediaConstraints = new MediaConstraints();
        videoSource = factory.createVideoSource(videoCapturer, mediaConstraints);
        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);

        //音频
        MediaConstraints audioConstraints = new MediaConstraints();
        audioSource = factory.createAudioSource(audioConstraints);
        AudioTrack localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };

        VideoRendererGui.setView(mGLSurfaceView, runnable);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        double ratio = screenWidth * 1.0 / screenHeight;
        int localWidth = 50;
        int localHeight = (int) (localWidth * ratio);
        try {

            int count = 0;
            for (Map.Entry<String, PCObserver> entry : pcObserverMap.entrySet()) {
                String remoteName = entry.getKey();
                PCObserver pcObserver = entry.getValue();
                VideoRenderer remoteVideoRenderer = VideoRendererGui.createGui(count % 2 * localWidth, count / 2 * localHeight, localWidth, localHeight, scalingType, true);
                pcObserver.setRemoteVideoRenderer(remoteVideoRenderer);
                remoteVideoRendererMap.put(remoteName, remoteVideoRenderer);
                count++;
            }
            int x = localWidth / 2;
            int y = total > 3 ? localHeight * 2 : localHeight;
            localVideoRenderer = VideoRendererGui.createGui(x, y, localWidth, localHeight, scalingType, true);
            localVideoTrack.addRenderer(localVideoRenderer);

        } catch (Exception e) {
            e.printStackTrace();
        }


        localMediaStream = factory.createLocalMediaStream(LOCAL_MEDIA_STREAM_ID);
        localMediaStream.addTrack(localAudioTrack);
        localMediaStream.addTrack(localVideoTrack);

        //给每个peerconnection添加localMediaStream
        for (Map.Entry<String, PeerConnection> entry : peerConnectionMap.entrySet()) {
            PeerConnection pc = entry.getValue();
            pc.addStream(localMediaStream);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        release();

    }

    private void release() {
        videoCallEnded();
        StaticVariable.inMVideoCallActivity = false;

        for (Map.Entry<String, PeerConnection> entry : peerConnectionMap.entrySet()) {
            String remoteName = entry.getKey();
            PeerConnection pc = entry.getValue();
            pc.removeStream(localMediaStream);
            pc.close();
            pc.dispose();
            peerConnectionMap.put(remoteName, null);
        }
        peerConnectionMap = null;

        localMediaStream.dispose();

        //释放资源
        videoCapturer.dispose();
        videoSource.stop();
        videoSource.dispose();
        factory.dispose();

        audioManager.setSpeakerphoneOn(false);
    }

    protected void onResume() {
        super.onResume();
        StaticVariable.inMVideoCallActivity = true;
    }

    private void videoCallEnded() {
        for (String remoteName : members) {
            if (remoteName.equals(userName))
                continue;
            String chatJid = remoteName + "@" + Constants.SERVER_NAME;
            Element element = new Element("video");
            element.addProperty("type", "MVideoEnd");
            Chat chat = cm.createChat(chatJid, null);
            try {
                chat.sendMessage(element.toString());
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            Message message = (Message) msg.obj;
            Element element = XmlParser.parse(message.getBody());
            String sendType = element.getProperty("type");
            String remoteName = message.getFrom().split("@")[0];
            Log.d(LOG_TAG, sendType);
            switch (msg.what) {
                case 1:
                    Log.i(LOG_TAG, "我收到了你的SDP");
                    String sdpType = element.getProperty("sdpType");
                    String sdpDescription = Base64Encoder.getInstance().decode(element
                            .getProperty("description"));
                    PeerConnection pc = peerConnectionMap.get(remoteName);
                    SDPObserver sdpObserver = sdpObserverMap.get(remoteName);
                    if (sdpType != null) {
                        SessionDescription.Type type = SessionDescription.Type.fromCanonicalForm(sdpType);
                        SessionDescription sdp = new SessionDescription(type, sdpDescription);
                        if (pc == null) {
                            Log.e(LOG_TAG, "pc == null");
                        }
                        pc.setRemoteDescription(sdpObserver, sdp);
                    }
                    //如果是offer,则被叫方createAnswer
                    if (sdpType.equals("offer")) {
                        mIsCalled = true;
                        pc.createAnswer(sdpObserver, pcConstraints);
                    }
                    break;
                case 2:
                    String iceSdpMid = element.getProperty("sdpMid");
                    int iceSdpMLineIndex = Integer.valueOf(element.getProperty("sdpMLineIndex"));
                    String iceSdp = element.getProperty("sdp");
                    if (iceSdpMid != null) {
                        IceCandidate iceCandidate = new IceCandidate(iceSdpMid, iceSdpMLineIndex, iceSdp);

                        IceCandidate remoteIceCandidate = iceCandidateMap.get(remoteName);
                        if (remoteIceCandidate == null) {
                            remoteIceCandidate = iceCandidate;
                        }
                        drainRemoteCandidates(remoteName, remoteIceCandidate);
                    }
                    break;
                case 3:
//                    PeerConnection pc3 = peerConnectionMap.get(remoteName);
//                    if (pc3 != null) {
//                        pc3.dispose();
//                        pc3 = null;
//                    }
                    break;
                case 4:
                    orders[count] = remoteName;
                    count++;
                    sendMemberOrders(remoteName);
                    PeerConnection pc2 = peerConnectionMap.get(remoteName);
                    SDPObserver sdpObserver2 = sdpObserverMap.get(remoteName);
                    pc2.createOffer(sdpObserver2, pcConstraints);
                    break;
                case 5:
                    String[] memberWaiting = element.getProperty("orders").split(";");
                    for (String remote : memberWaiting) {
                        PeerConnection pc3 = peerConnectionMap.get(remote);
                        SDPObserver sdpObserver3 = sdpObserverMap.get(remote);
                        pc3.createOffer(sdpObserver3, pcConstraints);
                    }
                    break;
            }
        }
    };

    private void sendMemberOrders(String remoteName) {
        StringBuilder sb = new StringBuilder();
        for (String order : orders) {
            if (order.equals(remoteName)) {
                break;
            } else {
                sb.append(order + ";");
            }
        }
        if (sb.length() == 0) {
            return;
        }
        Element element = new Element("video");
        element.addProperty("type", "MemberWaiting");
        element.addProperty("orders", sb.substring(0, sb.length() - 1));
        Chat chat = cm.createChat(remoteName + "@" + Constants.SERVER_NAME, null);

        try {
            chat.sendMessage(element.toString());
        } catch (XMPPException e) {
            e.printStackTrace();
        }

    }

    private void drainRemoteCandidates(String remoteName, IceCandidate remoteIceCandidate) {
        if (remoteIceCandidate == null) {
            Log.e(LOG_TAG, "remoteIceCandidate == null");
            return;
        }
        PeerConnection pc = peerConnectionMap.get(remoteName);
        pc.addIceCandidate(remoteIceCandidate);
        Log.e(LOG_TAG, "添加IceCandidate成功");
    }

    private class PCObserver implements PeerConnection.Observer {

        private String remoteName;
        private VideoRenderer remoteVideoRenderer;

        public PCObserver(String remoteName) {
            this.remoteName = remoteName;
        }

        public void setRemoteVideoRenderer(VideoRenderer remoteVideoRenderer) {
            this.remoteVideoRenderer = remoteVideoRenderer;
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        //发送ICE候选到其他客户端
        @Override
        public void onIceCandidate(final IceCandidate iceCandidate) {
            //利用XMPP发送iceCandidate到其他客户端
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String chatJid = remoteName + "@" + Constants.SERVER_NAME;
                    String sdpMid = iceCandidate.sdpMid;
                    int sdpMLineIndex = iceCandidate.sdpMLineIndex;
                    String sdp = iceCandidate.sdp;
                    Element element = new Element("video");
                    element.addProperty("type", "MIceCandidate");
                    element.addProperty("sdpMid", sdpMid);
                    element.addProperty("sdpMLineIndex", String.valueOf(sdpMLineIndex));
                    element.addProperty("sdp", sdp);
                    Chat chat = cm.createChat(chatJid, null);
                    try {
                        chat.sendMessage(element.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        //Display a media stream from remote
        @Override
        public void onAddStream(final MediaStream mediaStream) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    if (pc == null)
//                    {
//                        Log.e("onAddStream","pc == null");
//                        return;
//                    }
                    if (mediaStream.videoTracks.size() > 1 || mediaStream.audioTracks.size() > 1) {
                        Log.e(LOG_TAG, "size > 1");
                        return;
                    }
                    if (mediaStream.videoTracks.size() == 1) {
                        Log.d(LOG_TAG, "onAddStream");
                        VideoTrack videoTrack = mediaStream.videoTracks.get(0);
                        videoTrack.addRenderer(remoteVideoRenderer);

                    }
                }
            });
        }

        @Override
        public void onRemoveStream(final MediaStream mediaStream) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mediaStream.videoTracks.get(0).dispose();
                }
            });
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {

        }

        @Override
        public void onRenegotiationNeeded() {

        }
    }

    private class SDPObserver implements SdpObserver {

        private String remoteName;
        private PeerConnection pc;
        private SDPObserver sdpObserver;

        public SDPObserver(String remoteName) {
            this.remoteName = remoteName;
        }

        private void setPeerConnection(PeerConnection pc) {
            this.pc = pc;
        }

        private void setSdpObserver(SDPObserver sdpObserver) {
            this.sdpObserver = sdpObserver;
        }

        @Override
        public void onCreateSuccess(final SessionDescription sessionDescription) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String chatJid = remoteName + "@" + Constants.SERVER_NAME;

                    String type = sessionDescription.type.canonicalForm();
                    String description = Base64Encoder.getInstance().encode(
                            sessionDescription.description);
                    Element element = new Element("video");
                    element.addProperty("type", "MSdpOffer");
                    element.addProperty("sdpType", type);
                    element.addProperty("description", description);
                    Chat chat = cm.createChat(chatJid, null);
                    try {
                        Log.i(LOG_TAG, "发送了SDP");
                        chat.sendMessage(element.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    pc.setLocalDescription(sdpObserver, sessionDescription);
                }
            });
        }

        @Override
        public void onSetSuccess() {
            Log.d(LOG_TAG, "onSetSuccess");
        }

        @Override
        public void onCreateFailure(String s) {
            Log.e(LOG_TAG, "onCreateFailure");
        }

        @Override
        public void onSetFailure(String s) {
            Log.e(LOG_TAG, "onSetFailure");
        }
    }
}
