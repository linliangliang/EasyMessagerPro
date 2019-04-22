package com.zhengyuan.easymessengerpro.activity.voip;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.constants.StaticVariable;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.utils.xml.XmlParser;
import com.zhengyuan.easymessengerpro.R;
import com.zhengyuan.easymessengerpro.xmpp.XmppManager;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPConnection;
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

import java.util.LinkedList;

/**
 * 视频聊天界面
 * Created by Administrator on 2017/5/17.
 */

public class VideoCallActivity extends Activity {

    public static final String VIDEO_TRACK_ID = "video_track_id";
    public static final String AUDIO_TRACK_ID = "audio_track_id";
    public static final String LOCAL_MEDIA_STREAM_ID = "local_media_stream_id";
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;

    private XMPPConnection connection;
    private VideoRendererGui.ScalingType scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;
    private GLSurfaceView mGLSurfaceView;

    private PeerConnection pc;
    private ChatManager cm;
    private static String toJID;
    private static String fromJID;
    private PCObserver pcObserver;
    private SDPObserver sdpObserver;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
    private MediaConstraints pcConstraints;

    IceCandidate remoteIceCandidate;

    private boolean mIsInited;
    private boolean mIsCalled;
    PeerConnectionFactory factory;
    VideoCapturer videoCapturer;
    VideoSource videoSource;
    AudioSource audioSource;

    VideoRenderer.Callbacks localVideoRenderer;
    VideoRenderer.Callbacks remoteVideoRenderer;

    AudioManager audioManager;

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

        StaticVariable.handler = handler;
        StaticVariable.inVideoCallActivity = true;

        toJID = getIntent().getStringExtra("remoteName");
        fromJID = EMProApplicationDelegate.userInfo.getUserId().split("/")[0];
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);

        mGLSurfaceView = findViewById(R.id.glview_call);
        mGLSurfaceView.setKeepScreenOn(true);

        pcObserver = new PCObserver();
        sdpObserver = new SDPObserver();

        cm = XmppManager.getConnection().getChatManager();
        if (!PeerConnectionFactory.initializeAndroidGlobals(this, true, true,
                true, null)) {
            Log.e("init", "PeerConnectionFactory init fail!");
            return;
        }

        factory = new PeerConnectionFactory();

        pcConstraints = new MediaConstraints();
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("turn:59.175.173.137",
                "root", "root"));

        pc = factory
                .createPeerConnection(iceServers, pcConstraints, pcObserver);


        mIsInited = false;
        mIsCalled = false;

        boolean offer = getIntent().getBooleanExtra("createOffer", false);


        if (!offer)

        {
            initialSystem();
            sendReturnCall();
        } else

        {
            callRemote();
        }
        // processExtraData();
    }

    private void sendReturnCall() {
        Message msg = new Message();
        msg.setFrom(fromJID);
        msg.setTo(toJID);
        Element element = new Element("video");
        element.addProperty("type", "returnVideoCall");
        msg.setBody(element.toXml());
        Chat chat = cm.createChat(toJID + "@xxzx", null);
        try {
            chat.sendMessage(msg);
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.d(LOG_TAG, "sendReturnCall" + fromJID + " jid " + toJID + " msg: "
                + msg);
    }

    private final String LOG_TAG = "VideoCallActivity";

    private void drainRemoteCandidates() {
        if (remoteIceCandidate == null) {
            Log.e(LOG_TAG, "remoteIceCandidate == null");
            return;
        }
        pc.addIceCandidate(remoteIceCandidate);
        Log.e(LOG_TAG, "addIceCandidate");
        remoteIceCandidate = null;
    }

    protected void onResume() {
        super.onResume();
        StaticVariable.inVideoCallActivity = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
//        processExtraData();

        StaticVariable.inVideoCallActivity = true;
    }

    protected void onStop() {
        super.onStop();
        StaticVariable.inVideoCallActivity = false;
    }

    @Override
    protected void onDestroy() {

        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();

        release();
    }

    private void release() {

        StaticVariable.inVideoCallActivity = false;
        StaticVariable.handler = null;

        videoCallEnded();
        if (pc != null) {
            pc.dispose();
            pc = null;
        }
        videoCapturer.dispose();
        videoSource.stop();
        videoSource.dispose();

        factory.dispose();

        audioManager.setSpeakerphoneOn(false);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

            finish();
            return true;
            /*if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), R.string.quit_hint, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(  0);
            }*/
        }
        return super.onKeyDown(keyCode, event);
    }

    private void videoCallEnded() {

        Message msg = new Message();
        msg.setFrom(fromJID);
        msg.setTo(toJID);
        String sendType = "videoEnd";
        Element element = new Element("video");
        element.addProperty("type", sendType);

        msg.setBody(element.toXml());
        Chat chat = cm.createChat(toJID + "@xxzx", null);
        try {
            chat.sendMessage(msg);
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void callRemote() {
        initialSystem();
        // createOffer
//        pc.createOffer(sdpObserver, pcConstraints);
        Element element = new Element("video");
        element.addProperty("type", "videoCall");
        Message msg = new Message();
        msg.setBody(element.toXml());
        msg.setFrom(fromJID);
        msg.setTo(toJID);
        Chat chat = cm.createChat(toJID + "@xxzx", null);
        try {
            chat.sendMessage(msg);
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void initialSystem() {
        if (mIsInited) {
            return;
        }
        String frontDeviceName = VideoCapturerAndroid
                .getNameOfFrontFacingDevice();
        videoCapturer = VideoCapturerAndroid.create(frontDeviceName);
        if (videoCapturer == null) {
            Log.e("open", "fail to open camera");
            return;
        }

        MediaConstraints mediaConstraints = new MediaConstraints();
        videoSource = factory
                .createVideoSource(videoCapturer, mediaConstraints);
        VideoTrack localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID,
                videoSource);

        MediaConstraints audioConstraints = new MediaConstraints();
        audioSource = factory.createAudioSource(audioConstraints);
        AudioTrack localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID,
                audioSource);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };
        VideoRendererGui.setView(mGLSurfaceView, runnable);
        try {
            remoteVideoRenderer = VideoRendererGui.create(REMOTE_X, REMOTE_Y,
                    REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, true);
            localVideoRenderer = VideoRendererGui.create(LOCAL_X_CONNECTED,
                    LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED,
                    LOCAL_HEIGHT_CONNECTED, scalingType, true);

            localVideoTrack.addRenderer(new VideoRenderer(localVideoRenderer));
        } catch (Exception e) {
            e.printStackTrace();
        }

        MediaStream localMediaStream = factory
                .createLocalMediaStream(LOCAL_MEDIA_STREAM_ID);
        localMediaStream.addTrack(localAudioTrack);
        localMediaStream.addTrack(localVideoTrack);

        pc.addStream(localMediaStream);
//        localMediaStream.videoTracks.get(0).addRenderer(
//                new VideoRenderer(localVideoRenderer));
//        VideoRendererGui.update(localVideoRenderer, 72, 72, 25, 25,
//                VideoRendererGui.ScalingType.SCALE_ASPECT_FILL);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {

            Message message = (Message) msg.obj;
            Element element = XmlParser.parse(message.getBody());
            String sendType = element.getProperty("type");
            Log.d(LOG_TAG, sendType);
            switch (msg.what) {
                case 0:
                    String sdpType = element.getProperty("SdpType");
                    String sdpDescription = Base64Encoder.getInstance().decode(element
                            .getProperty("SdpDescription"));
                    if (sdpType != null) {
                        SessionDescription.Type type = SessionDescription.Type
                                .fromCanonicalForm(sdpType);
                        SessionDescription sdp = new SessionDescription(
                                type, sdpDescription);
                        if (pc == null) {
                            Log.e("pc", "pc == null");
                        }

                        pc.setRemoteDescription(sdpObserver, sdp);

                        if (sdpType.equals("offer")) {
                            mIsCalled = true;
                            Log.d(LOG_TAG, "createAnswer");
                            pc.createAnswer(sdpObserver,
                                    pcConstraints);
                        }
                    }
                    break;
                case 1:
                    String iceSdpMid = element.getProperty("SdpMid");
                    int iceSdpMLineIndex = Integer.valueOf(element
                            .getProperty("SdpMLineIndex"));
                    String iceSdp = Base64Encoder.getInstance().decode(
                            element.getProperty("Sdp"));
                    Log.d(LOG_TAG, "iceSdp " + iceSdp);
                    if (iceSdpMid != null) {
                        IceCandidate iceCandidate = new IceCandidate(
                                iceSdpMid, iceSdpMLineIndex, iceSdp);
                        if (remoteIceCandidate == null) {
                            remoteIceCandidate = iceCandidate;
                        }

                        drainRemoteCandidates();
                    }
                    break;
                case 2:
                    finish();
                    break;
                case 3:
                    pc.createOffer(sdpObserver, pcConstraints);
                    break;
            }
        }
    };

    private class PCObserver implements PeerConnection.Observer {

        @Override
        public void onSignalingChange(
                PeerConnection.SignalingState signalingState) {

            Log.d(LOG_TAG, "onSignalingChange");
        }

        @Override
        public void onIceConnectionChange(
                PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(LOG_TAG, "onIceConnectionChange " + iceConnectionState);
        }

        @Override
        public void onIceGatheringChange(
                PeerConnection.IceGatheringState iceGatheringState) {
            Log.d(LOG_TAG, "onIceGatheringChange");
        }

        @Override
        public void onIceCandidate(final IceCandidate iceCandidate) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.setFrom(fromJID);
                    msg.setTo(toJID);
                    String sendType = "IceCandidate";
                    String sdpMid = iceCandidate.sdpMid;
                    int sdpMLineIndex = iceCandidate.sdpMLineIndex;
                    String sdp = iceCandidate.sdp;

                    Element element = new Element("video");
                    element.addProperty("type", sendType);
                    element.addProperty("SdpMid", sdpMid);
                    element.addProperty("SdpMLineIndex",
                            String.valueOf(sdpMLineIndex));
                    element.addProperty("Sdp", Base64Encoder.getInstance().encode(sdp));
                    msg.setBody(element.toXml());

                    Chat chat = cm.createChat(toJID + "@xxzx", null);
                    try {
                        chat.sendMessage(msg);
                    } catch (XMPPException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }

        // Display a media stream from remote
        @Override
        public void onAddStream(final MediaStream mediaStream) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pc == null) {
                        Log.e(LOG_TAG, "onAddStream pc == null");
                        return;
                    }
                    if (mediaStream.videoTracks.size() > 1
                            || mediaStream.audioTracks.size() > 1) {
                        Log.e(LOG_TAG, "onAddStream size > 1");
                        return;
                    }
                    Log.e(LOG_TAG, "onAddStream");
                    if (mediaStream.videoTracks.size() == 1) {

                        Log.d(LOG_TAG, "remote video render");
                        VideoTrack videoTrack = mediaStream.videoTracks.get(0);
                        videoTrack.addRenderer(new VideoRenderer(remoteVideoRenderer));
//                        VideoRendererGui.update(remoteVideoRenderer, REMOTE_X,
//                                REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT,
//                                scalingType);
//                        VideoRendererGui.update(localVideoRenderer,
//                                LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
//                                LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
//                                scalingType);

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
            Log.d(LOG_TAG, "onDataChannel");
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.d(LOG_TAG, "onRenegotiationNeeded");
        }
    }

    private class SDPObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(final SessionDescription sessionDescription) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.setFrom(fromJID);
                    msg.setTo(toJID);
                    String sendType = "SdpOffer";
                    String type = sessionDescription.type.canonicalForm();
                    String description = Base64Encoder.getInstance().encode(
                            sessionDescription.description);

                    Element element = new Element("video");
                    element.addProperty("type", sendType);
                    element.addProperty("SdpType", type);
                    element.addProperty("SdpDescription", description);

                    String convert = element.toXml();
                    msg.setBody(convert);

                    Log.d(LOG_TAG, "onCreateSuccess ");
                    Chat chat = cm.createChat(toJID + "@xxzx", null);
                    try {
                        chat.sendMessage(msg);
                    } catch (XMPPException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    pc.setLocalDescription(sdpObserver, sessionDescription);
                }
            });
        }

        @Override
        public void onSetSuccess() {
            Log.d(LOG_TAG, "setSuccess");
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
