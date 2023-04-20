package io.agora.metachat.example.ui.game;

import static io.agora.rtc2.video.VideoEncoderConfiguration.STANDARD_BITRATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jakewharton.rxbinding2.view.RxView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.agora.base.FaceCaptureInfo;
import io.agora.base.VideoFrame;
import io.agora.meta.renderer.unity.AgoraAvatarView;
import io.agora.meta.renderer.unity.api.AvatarProcessImpl;
import io.agora.metachat.IMetachatScene;
import io.agora.metachat.SceneDisplayConfig;
import io.agora.metachat.example.MainActivity;
import io.agora.metachat.example.adapter.DressTypeAdapter;
import io.agora.metachat.example.adapter.DressTypeAssetAdapter;
import io.agora.metachat.example.adapter.SurfaceViewAdapter;
import io.agora.metachat.example.inf.IMetaEventHandler;
import io.agora.metachat.example.inf.IRtcEventCallback;
import io.agora.metachat.example.metachat.MetaChatContext;
import io.agora.metachat.example.R;
import io.agora.metachat.example.databinding.GameActivityBinding;
import io.agora.metachat.example.dialog.CustomDialog;
import io.agora.metachat.example.models.DressItemResource;
import io.agora.metachat.example.models.SurfaceViewInfo;
import io.agora.metachat.example.models.UnityMessage;
import io.agora.metachat.example.utils.DressAndFaceDataUtils;
import io.agora.metachat.example.utils.KeyCenter;
import io.agora.metachat.example.utils.MetaChatConstants;
import io.agora.rtc2.Constants;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.IVideoFrameObserver;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class GameActivity extends Activity implements IMetaEventHandler, IRtcEventCallback {

    private final String TAG = GameActivity.class.getSimpleName();
    private GameActivityBinding binding;

    private boolean mIsFront;

    private final List<SurfaceViewInfo> mLocalSurfaceViewList = new ArrayList<>();
    private final List<SurfaceViewInfo> mRemoteSurfaceViewList = new ArrayList<>();

    private SurfaceViewAdapter mRemoteViewAdapter;
    private SurfaceViewAdapter mLocalViewAdapter;
    private SurfaceView mLocalPreviewSurfaceView;

    private TextureView mLocalAvatarTextureView;

    private SurfaceTexture mSaveLocalAvatarSurfaceTexture;

    private int mFrameWidth = -1;
    private int mFrameHeight = -1;

    private AgoraAvatarView mAvatarView;

    private DressTypeAdapter mDressTypeAdapter;
    private DressTypeAssetAdapter mDressTypeAssetAdapter;
    private List<DressItemResource> mDressResourceDataList;

    private DressItemResource mCurrentDressItemResource;

    private final ObservableBoolean isEnterScene = new ObservableBoolean(false);
    private final ObservableBoolean enableMic = new ObservableBoolean(true);
    private final ObservableBoolean enableSpeaker = new ObservableBoolean(true);
    private final ObservableBoolean isBroadcaster = new ObservableBoolean(true);
    private final Observable.OnPropertyChangedCallback callback =
            new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (sender == isEnterScene) {
                        if (MetaChatConstants.SCENE_DRESS == MetaChatContext.getInstance().getCurrentScene()) {
                            binding.sceneDressAndFaceGroup.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.sceneGameGroup.setVisibility(View.GONE);

                            if (isEnterScene.get()) {
                                MetaChatContext.getInstance().sendRoleDressInfo();
                            }
                        } else if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
                            binding.card.nickname.setText(MetaChatContext.getInstance().getRoleInfo().getName());

                            binding.sceneGameGroup.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.sceneDressAndFaceGroup.setVisibility(View.GONE);
                        }

                    } else if (sender == enableMic) {
                        if (!MetaChatContext.getInstance().enableLocalAudio(enableMic.get())) {
                            return;
                        }
                        binding.mic.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        enableMic.get() ? R.mipmap.microphone_on : R.mipmap.microphone_off,
                                        getTheme()
                                )
                        );
                    } else if (sender == enableSpeaker) {
                        if (!MetaChatContext.getInstance().muteAllRemoteAudioStreams(!enableSpeaker.get())) {
                            return;
                        }
                        binding.speaker.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        enableSpeaker.get() ? R.mipmap.voice_on : R.mipmap.voice_off,
                                        getTheme()
                                )
                        );
                    } else if (sender == isBroadcaster) {
                        if (!MetaChatContext.getInstance().updateRole(isBroadcaster.get() ?
                                Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE)) {
                            return;
                        }
                        binding.card.nickname.setText(MetaChatContext.getInstance().getRoleInfo().getName());
                        binding.card.mode.setText(isBroadcaster.get() ? "语聊模式" : "游客模式");
                        binding.card.tips.setVisibility(isBroadcaster.get() ? View.GONE : View.VISIBLE);
                        binding.card.role.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        isBroadcaster.get() ? R.mipmap.offbtn : R.mipmap.onbtn,
                                        getTheme()
                                )
                        );
                        binding.mic.setVisibility(isBroadcaster.get() ? View.VISIBLE : View.GONE);
                        if (isBroadcaster.get()) {
                            enableMic.set(true);
                        }
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //just for call setRequestedOrientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        binding = GameActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        isEnterScene.addOnPropertyChangedCallback(callback);
        enableMic.addOnPropertyChangedCallback(callback);
        enableSpeaker.addOnPropertyChangedCallback(callback);
        isBroadcaster.addOnPropertyChangedCallback(callback);
        MetaChatContext.getInstance().registerMetaChatSceneEventHandler(this);
        MetaChatContext.getInstance().registerMetaChatEventHandler(this);
        MetaChatContext.getInstance().setRtcEventCallback(this);

        initListener();

        createScene();
    }

    private void initLocalSurfaceView() {
        if (MetaChatConstants.SCENE_GAME != MetaChatContext.getInstance().getCurrentScene()) {
            return;
        }
        if (null == mLocalPreviewSurfaceView) {
            mLocalPreviewSurfaceView = new SurfaceView(getApplicationContext());
        }

        RtcEngine rtcEngine = MetaChatContext.getInstance().getRtcEngine();

        rtcEngine.setupLocalVideo(new VideoCanvas(mLocalPreviewSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));

        rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);

        rtcEngine.registerVideoFrameObserver(new IVideoFrameObserver() {
            @Override
            public boolean onCaptureVideoFrame(VideoFrame videoFrame) {
                if (null != videoFrame.getMetaInfo() && videoFrame.getMetaInfo().getCustomMetaInfo(MetaChatConstants.KEY_FACE_CAPTURE_INFO).size() > 0) {
                    FaceCaptureInfo faceCaptureInfo = (FaceCaptureInfo) videoFrame.getMetaInfo().getCustomMetaInfo(MetaChatConstants.KEY_FACE_CAPTURE_INFO).get(0);
                    UnityMessage unityMessage = new UnityMessage();
                    unityMessage.setKey(MetaChatConstants.KEY_UNITY_MESSAGE_FACE_CAPTURE);
                    unityMessage.setValue(faceCaptureInfo.toString());
                    MetaChatContext.getInstance().sendSceneMessage(JSONObject.toJSONString(unityMessage));
                }
                return false;
            }

            @Override
            public boolean onPreEncodeVideoFrame(VideoFrame videoFrame) {
                return false;
            }

            @Override
            public boolean onScreenCaptureVideoFrame(VideoFrame videoFrame) {
                return false;
            }

            @Override
            public boolean onPreEncodeScreenVideoFrame(VideoFrame videoFrame) {
                return false;
            }

            @Override
            public boolean onMediaPlayerVideoFrame(VideoFrame videoFrame, int mediaPlayerId) {
                return false;
            }

            @Override
            public boolean onRenderVideoFrame(String channelId, int uid, VideoFrame videoFrame) {
                return false;
            }

            @Override
            public int getVideoFrameProcessMode() {
                return 0;
            }

            @Override
            public int getVideoFormatPreference() {
                return 0;
            }

            @Override
            public boolean getRotationApplied() {
                return false;
            }

            @Override
            public boolean getMirrorApplied() {
                return false;
            }

            @Override
            public int getObservedFramePosition() {
                return 0;
            }
        });
        rtcEngine.startPreview();


        mLocalSurfaceViewList.add(new SurfaceViewInfo(mLocalPreviewSurfaceView, KeyCenter.RTC_UID));

        mLocalAvatarTextureView = new TextureView(this);
        mLocalAvatarTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "localTextureView onSurfaceTextureAvailable surface=" + surface);
                addLocalAvatarView();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });


        //for init TextureView
        //binding.unity.addView(mLocalAvatarTextureView, 1, new ViewGroup.LayoutParams(0, 0));
        mLocalSurfaceViewList.add(new SurfaceViewInfo(mLocalAvatarTextureView, KeyCenter.RTC_UID));

        if (mLocalViewAdapter == null) {
            mLocalViewAdapter = new SurfaceViewAdapter(getApplicationContext());
            mLocalViewAdapter.setSurfaceViewData(mLocalSurfaceViewList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            binding.rvLocalView.setLayoutManager(linearLayoutManager);
            binding.rvLocalView.setAdapter(mLocalViewAdapter);
        } else {
            localViewDataChanged();
        }

        binding.rvLocalView.setVisibility(View.VISIBLE);
    }

    private void addLocalAvatarView() {
        SceneDisplayConfig sceneDisplayConfig = new SceneDisplayConfig();
        sceneDisplayConfig.width = mLocalPreviewSurfaceView.getMeasuredWidth();
        sceneDisplayConfig.height = mLocalPreviewSurfaceView.getMeasuredHeight();
        MetaChatContext.getInstance().addSceneView(mLocalAvatarTextureView, sceneDisplayConfig);
    }

    private void localAvatarViewReady() {
        binding.unity.removeView(mLocalAvatarTextureView);
        mLocalSurfaceViewList.add(new SurfaceViewInfo(mLocalAvatarTextureView, 0));
        localViewDataChanged();
    }

    private void initRemoteSurfaceView() {
        if (MetaChatConstants.SCENE_GAME != MetaChatContext.getInstance().getCurrentScene()) {
            return;
        }
        if (mRemoteViewAdapter == null) {
            mRemoteViewAdapter = new SurfaceViewAdapter(getApplicationContext());
            mRemoteViewAdapter.setSurfaceViewData(mRemoteSurfaceViewList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            binding.rvRemoteView.setLayoutManager(linearLayoutManager);
            binding.rvRemoteView.setAdapter(mRemoteViewAdapter);
        }
        binding.rvRemoteView.setVisibility(View.VISIBLE);

        binding.btnSwitchRemoteView.setEnabled(mRemoteSurfaceViewList.size() != 0);
    }

    private void removeLocalSurfaceView() {
        ViewGroup viewGroup = (ViewGroup) mLocalAvatarTextureView.getParent();
        if (viewGroup.getChildCount() >= 1) {
            viewGroup.removeView(mLocalAvatarTextureView);
        }
        RtcEngine rtcEngine = MetaChatContext.getInstance().getRtcEngine();
        rtcEngine.stopPreview();
        mLocalSurfaceViewList.clear();
        binding.rvLocalView.setVisibility(View.GONE);
        mLocalAvatarTextureView = null;
        mFrameWidth = -1;
        mFrameHeight = -1;
    }

    private void removeRemoteSurfaceView() {
        mRemoteSurfaceViewList.clear();
        binding.rvRemoteView.setVisibility(View.GONE);
    }

    @SuppressLint("CheckResult")
    private void initListener() {
        RxView.clicks(binding.back).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isEnterScene.set(false);
            MetaChatContext.getInstance().resetRoleInfo();
            //fix here
            if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene() && false) {
                MetaChatContext.getInstance().removeSceneView(mLocalAvatarTextureView);
            } else {
                MetaChatContext.getInstance().leaveScene();
            }
            MetaChatContext.getInstance().setCurrentScene(MetaChatConstants.SCENE_NONE);
        });

        RxView.clicks(binding.cancelBt).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isEnterScene.set(false);
            MetaChatContext.getInstance().cancelRoleDressInfo(MetaChatContext.getInstance().getRoleInfo().getName()
                    , MetaChatContext.getInstance().getRoleInfo().getGender());
            MetaChatContext.getInstance().setNextScene(MetaChatConstants.SCENE_GAME);
            MetaChatContext.getInstance().leaveScene();
        });


        RxView.clicks(binding.saveBtn).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isEnterScene.set(false);
            MetaChatContext.getInstance().saveRoleDressInfo(MetaChatContext.getInstance().getRoleInfo().getName()
                    , MetaChatContext.getInstance().getRoleInfo().getGender());
            MetaChatContext.getInstance().setNextScene(MetaChatConstants.SCENE_GAME);
            MetaChatContext.getInstance().leaveScene();
        });

        RxView.clicks(binding.dressSetting).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isEnterScene.set(false);
            if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
                MetaChatContext.getInstance().removeSceneView(mLocalAvatarTextureView);
            } else {
                MetaChatContext.getInstance().leaveScene();
            }
            MetaChatContext.getInstance().setNextScene(MetaChatConstants.SCENE_DRESS);
        });

        RxView.clicks(binding.speaker).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            enableSpeaker.set(!enableSpeaker.get());
        });


        RxView.clicks(binding.mic).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            enableMic.set(!enableMic.get());
        });

        RxView.clicks(binding.users).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            Toast.makeText(this, "暂不支持", Toast.LENGTH_LONG)
                    .show();
        });

        RxView.clicks(binding.card.tips).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            if (!isBroadcaster.get()) {
                CustomDialog.showTips(this);
            }
        });

        RxView.clicks(binding.card.mode).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            if (!isBroadcaster.get()) {
                CustomDialog.showTips(this);
            }
        });

        RxView.clicks(binding.card.role).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isBroadcaster.set(!isBroadcaster.get());
        });

        RxView.clicks(binding.btnSwitchLocalView).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            if (binding.rvLocalView.getVisibility() == View.VISIBLE) {
                binding.btnSwitchLocalView.setText(getApplicationContext().getResources().getString(R.string.show_local_view));
                binding.rvLocalView.setVisibility(View.GONE);
            } else {
                binding.btnSwitchLocalView.setText(getApplicationContext().getResources().getString(R.string.hide_local_view));
                binding.rvLocalView.setVisibility(View.VISIBLE);
            }
        });

        RxView.clicks(binding.btnSwitchRemoteView).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            if (binding.rvRemoteView.getVisibility() == View.VISIBLE) {
                binding.btnSwitchRemoteView.setText(getApplicationContext().getResources().getString(R.string.show_remote_view));
                binding.rvRemoteView.setVisibility(View.GONE);
            } else {
                binding.btnSwitchRemoteView.setText(getApplicationContext().getResources().getString(R.string.hide_remote_view));
                binding.rvRemoteView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initUnityView() {
        if (null == mAvatarView) {
            mAvatarView = AvatarProcessImpl.createAgoraAvatarView(GameActivity.this);
            binding.layout.addView(mAvatarView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //just for call setRequestedOrientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        super.onNewIntent(intent);

        createScene();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        isEnterScene.removeOnPropertyChangedCallback(callback);
        enableMic.removeOnPropertyChangedCallback(callback);
        enableSpeaker.removeOnPropertyChangedCallback(callback);
        isBroadcaster.removeOnPropertyChangedCallback(callback);
        MetaChatContext.getInstance().unregisterMetaChatEventHandler(this);
    }

    private void createScene() {
        Log.i(TAG, "createScene");
        resetViewVisibility();
        DressItemResource[] dressItemResources = DressAndFaceDataUtils.getInstance().getDressResources(MetaChatContext.getInstance().getRoleInfo().getAvatarType());
        if (null != dressItemResources) {
            mDressResourceDataList = Arrays.asList(dressItemResources);
        }
        AvatarProcessImpl.setActivity(this);
        MetaChatContext.getInstance().createScene(this, KeyCenter.CHANNEL_ID, null);
    }

    private void resetViewVisibility() {
        binding.sceneDressAndFaceGroup.setVisibility(View.GONE);
        binding.sceneGameGroup.setVisibility(View.GONE);
    }

    @Override
    public void onEnterSceneResult(int errorCode) {
        runOnUiThread(() -> {
            if (errorCode != 0) {
                Toast.makeText(this, String.format(Locale.getDefault(), "EnterSceneFailed %d", errorCode), Toast.LENGTH_LONG).show();
                return;
            }
            isEnterScene.set(true);
            enableMic.set(true);
            enableSpeaker.set(true);
            isBroadcaster.set(true);

            if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
                initLocalSurfaceView();
                initRemoteSurfaceView();
            } else if (MetaChatConstants.SCENE_DRESS == MetaChatContext.getInstance().getCurrentScene()) {
                initDressTypeView();
            }
        });
    }

    @Override
    public void onLeaveSceneResult(int errorCode) {
    }

    @Override
    public void onReleasedScene(int status) {
        if (status == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MetaChatContext.getInstance().destroy();
                }
            });

            MetaChatContext.getInstance().setCurrentScene(MetaChatConstants.SCENE_NONE);
            if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT != getRequestedOrientation()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    @Override
    public void onSceneVideoFrame(TextureView view, VideoFrame videoFrame) {
        if (null == videoFrame) {
            return;
        }
        if (view == mLocalAvatarTextureView) {
            if ((mFrameWidth == -1 && mFrameHeight == -1) || (mFrameWidth != videoFrame.getBuffer().getWidth() || mFrameHeight != videoFrame.getBuffer().getHeight())) {
                mFrameWidth = videoFrame.getBuffer().getWidth();
                mFrameHeight = videoFrame.getBuffer().getHeight();
                // update set video configuration
                MetaChatContext.getInstance().getRtcEngine().setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                        new VideoEncoderConfiguration.VideoDimensions(mFrameWidth, mFrameHeight),
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                        STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE, VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED));
            }
            if (!MetaChatContext.getInstance().pushExternalVideoFrame(videoFrame)) {
                Log.e(TAG, "pushExternalVideoFrame fail");
            }
        }

    }

    @Override
    public void onRecvMessageFromScene(byte[] message) {
        String jsonStr = new String(message);
        Log.e(TAG, "onRecvMessageFromScene jsonStr:" + jsonStr);
        try {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if (!TextUtils.isEmpty(jsonObject.getString("key"))) {
                if (MetaChatConstants.SCENE_MESSAGE_ADD_SCENE_VIEW_SUCCESS.equals(jsonObject.getString("key"))) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //localAvatarViewReady();
                            MetaChatContext.getInstance().enableSceneVideo(mLocalAvatarTextureView, true);
                        }
                    });
                } else if (MetaChatConstants.SCENE_MESSAGE_REMOVE_SCENE_VIEW_SUCCESS.equals(jsonObject.getString("key"))) {
                    //maybe to do something
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            removeLocalSurfaceView();
                            removeRemoteSurfaceView();
                            MetaChatContext.getInstance().leaveScene();
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateSceneResult(IMetachatScene scene, int errorCode) {
        //异步线程回调需在主线程处理
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initUnityView();
                MetaChatContext.getInstance().enterScene();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        mIsFront = true;
        if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
            if (MetaChatContext.getInstance().isInScene()) {
                MetaChatContext.getInstance().resumeMedia();
            }
            if (null != mAvatarView) {
                mAvatarView.resume();
            }
        }
        initDressTypeView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        mIsFront = false;

        if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
            if (MetaChatContext.getInstance().isInScene()) {
                MetaChatContext.getInstance().pauseMedia();
            }
            if (null != mAvatarView) {
                mAvatarView.pause();
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (MetaChatConstants.SCENE_DRESS == MetaChatContext.getInstance().getCurrentScene()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        super.setRequestedOrientation(requestedOrientation);
    }


    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        if (MetaChatConstants.SCENE_GAME != MetaChatContext.getInstance().getCurrentScene()) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SurfaceView surfaceView = new SurfaceView(getApplicationContext());
                surfaceView.setZOrderMediaOverlay(true);
                int ret = -1;
                try {
                    mRemoteSurfaceViewList.add(new SurfaceViewInfo(surfaceView, uid));
                    remoteViewDataChanged();
                    ret = MetaChatContext.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "onUserJoined setupRemoteVideo ret=" + ret);

            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        if (MetaChatConstants.SCENE_GAME != MetaChatContext.getInstance().getCurrentScene()) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Iterator<SurfaceViewInfo> infoIterator = mRemoteSurfaceViewList.iterator();
                while (infoIterator.hasNext()) {
                    SurfaceViewInfo info = infoIterator.next();
                    if (info.getUid() == uid) {
                        infoIterator.remove();
                    }
                }
                remoteViewDataChanged();

                try {
                    MetaChatContext.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_FIT, uid));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void remoteViewDataChanged() {
        if (null != mRemoteViewAdapter) {
            mRemoteViewAdapter.notifyDataSetChanged();
        }

        binding.btnSwitchRemoteView.setEnabled(mRemoteSurfaceViewList.size() != 0);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void localViewDataChanged() {
        if (null != mRemoteViewAdapter) {
            mLocalViewAdapter.notifyDataSetChanged();
        }
    }

    private void initDressTypeView() {
        if (MetaChatConstants.SCENE_DRESS != MetaChatContext.getInstance().getCurrentScene()) {
            return;
        }

        if (mDressTypeAdapter == null) {
            mCurrentDressItemResource = mDressResourceDataList.get(0);

            mDressTypeAdapter = new DressTypeAdapter(getApplicationContext());
            mDressTypeAdapter.setDataList(mDressResourceDataList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            binding.rvDressType.setLayoutManager(linearLayoutManager);
            binding.rvDressType.setAdapter(mDressTypeAdapter);
            mDressTypeAdapter.setOnItemClickCallBack(new DressTypeAdapter.OnItemClickCallBack() {
                @Override
                public void onItemClick(DressItemResource dressItemResource) {
                    mCurrentDressItemResource = dressItemResource;
                    if (null != mDressTypeAssetAdapter) {
                        mDressTypeAssetAdapter.setDataList(Arrays.asList(Arrays.stream(mCurrentDressItemResource.getAssets()).boxed().toArray(Integer[]::new)),
                                MetaChatContext.getInstance().getRoleInfo().getDressResourceMap().get(mCurrentDressItemResource.getId()));
                        mDressTypeAssetAdapter.notifyDataSetChanged();
                    }
                }
            });
        } else {
            mDressTypeAdapter.setDataList(mDressResourceDataList);
            mDressTypeAdapter.notifyItemRangeChanged(0, mDressResourceDataList.size());
        }

        String iconFilePath = DressAndFaceDataUtils.getInstance().getIconFilePath(MetaChatContext.getInstance().getRoleInfo().getAvatarType());
        if (!TextUtils.isEmpty(iconFilePath)) {
            File file = new File(iconFilePath);
            Map<Integer, String> assetMap = null;
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (null != files) {
                    assetMap = new HashMap<>(files.length);
                    for (File tempFile : files) {
                        try {
                            int assetId = Integer.parseInt(tempFile.getName().substring(0, tempFile.getName().lastIndexOf(".")));
                            assetMap.put(assetId, tempFile.getPath());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (null != assetMap) {
                if (mDressTypeAssetAdapter == null) {
                    mDressTypeAssetAdapter = new DressTypeAssetAdapter(getApplicationContext(), assetMap);
                    mDressTypeAssetAdapter.setDataList(Arrays.asList(Arrays.stream(mCurrentDressItemResource.getAssets()).boxed().toArray(Integer[]::new)),
                            MetaChatContext.getInstance().getRoleInfo().getDressResourceMap().get(mCurrentDressItemResource.getId()));
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
                    binding.rvDressTypeAsset.setLayoutManager(gridLayoutManager);
                    binding.rvDressTypeAsset.setAdapter(mDressTypeAssetAdapter);

                    mDressTypeAssetAdapter.setOnItemClickCallBack(new DressTypeAssetAdapter.OnItemClickCallBack() {
                        @Override
                        public void onItemClick(int resId) {
                            MetaChatContext.getInstance().getRoleInfo().updateDressResource(mCurrentDressItemResource.getId(), resId);
                            MetaChatContext.getInstance().sendRoleDressInfo();
                        }
                    });
                } else {
                    mDressTypeAssetAdapter.setDataList(Arrays.asList(Arrays.stream(mCurrentDressItemResource.getAssets()).boxed().toArray(Integer[]::new)),
                            MetaChatContext.getInstance().getRoleInfo().getDressResourceMap().get(mCurrentDressItemResource.getId()));
                    mDressTypeAssetAdapter.notifyDataSetChanged();
                }
            }
        }

    }
}
