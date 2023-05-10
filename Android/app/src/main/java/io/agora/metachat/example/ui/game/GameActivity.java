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
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.agora.base.FaceCaptureInfo;
import io.agora.base.VideoFrame;
import io.agora.metachat.IMetachatEventHandler;
import io.agora.metachat.IMetachatScene;
import io.agora.metachat.IMetachatSceneEventHandler;
import io.agora.metachat.MetachatSceneInfo;
import io.agora.metachat.MetachatUserPositionInfo;
import io.agora.metachat.SceneDisplayConfig;
import io.agora.metachat.example.MainActivity;
import io.agora.metachat.example.adapter.SkinGridViewAdapter;
import io.agora.metachat.example.adapter.SurfaceViewAdapter;
import io.agora.metachat.example.adapter.ViewPagerAdapter;
import io.agora.metachat.example.data.SkinsData;
import io.agora.metachat.example.inf.IRtcEventCallback;
import io.agora.metachat.example.metachat.MetaChatContext;
import io.agora.metachat.example.R;
import io.agora.metachat.example.databinding.GameActivityBinding;
import io.agora.metachat.example.dialog.CustomDialog;
import io.agora.metachat.example.models.RoleInfo;
import io.agora.metachat.example.models.SkinInfo;
import io.agora.metachat.example.models.SurfaceViewInfo;
import io.agora.metachat.example.models.TabEntity;
import io.agora.metachat.example.models.UnityMessage;
import io.agora.metachat.example.ui.view.CirclePageIndicator;
import io.agora.metachat.example.utils.KeyCenter;
import io.agora.metachat.example.utils.MetaChatConstants;
import io.agora.rtc2.Constants;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.IVideoFrameObserver;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class GameActivity extends Activity implements IMetachatSceneEventHandler, IMetachatEventHandler, SkinGridViewAdapter.SkinItemClick, IRtcEventCallback {

    private final String TAG = GameActivity.class.getSimpleName();
    private GameActivityBinding binding;
    private TextureView mTextureView = null;

    private final ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private static final int SKIN_TAB_MAX_PAGE_SIZE = 4;
    private int mCurrentTabIndex;
    private List<SkinGridViewAdapter> mTabItemAdapters;
    private boolean mReCreateScene;
    private boolean mSurfaceSizeChange;
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
                            binding.cancelBt.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.saveBtn.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.dressTab.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.dressViewpage.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);

                            binding.back.setVisibility(View.GONE);
                            binding.card.getRoot().setVisibility(View.GONE);
                            binding.users.setVisibility(View.GONE);
                            binding.mic.setVisibility(View.GONE);
                            binding.speaker.setVisibility(View.GONE);
                            binding.dressSetting.setVisibility(View.GONE);

                            if (isEnterScene.get()) {
                                MetaChatContext.getInstance().sendRoleDressInfo();
                            }
                        } else if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
                            binding.back.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.card.getRoot().setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.card.nickname.setText(MetaChatContext.getInstance().getRoleInfo().getName());
                            binding.users.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.mic.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.speaker.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.dressSetting.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.btnSwitchLocalView.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.btnSwitchRemoteView.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);

                            binding.cancelBt.setVisibility(View.GONE);
                            binding.saveBtn.setVisibility(View.GONE);
                            binding.dressTab.setVisibility(View.GONE);
                            binding.dressViewpage.setVisibility(View.GONE);
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

        resetSceneState();

        isEnterScene.addOnPropertyChangedCallback(callback);
        enableMic.addOnPropertyChangedCallback(callback);
        enableSpeaker.addOnPropertyChangedCallback(callback);
        isBroadcaster.addOnPropertyChangedCallback(callback);
        MetaChatContext.getInstance().registerMetaChatSceneEventHandler(this);
        MetaChatContext.getInstance().registerMetaChatEventHandler(this);
        MetaChatContext.getInstance().setRtcEventCallback(this);
        initUnityView();

        initListener();
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
//                    if (null == mSaveLocalAvatarSurfaceTexture) {
//                        mSaveLocalAvatarSurfaceTexture = mLocalAvatarTextureView.getSurfaceTexture();
//                        addLocalAvatarView();
//                    } else {
//                        mLocalAvatarTextureView.setSurfaceTexture(mSaveLocalAvatarSurfaceTexture);
//                    }
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
            if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
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
        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                mReCreateScene = true;
                mSurfaceSizeChange = true;
                maybeCreateScene();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                Log.i(TAG, "onSurfaceTextureSizeChanged");
                mSurfaceSizeChange = true;
                if (MetaChatConstants.SCENE_NONE == MetaChatContext.getInstance().getNextScene()) {
                    maybeCreateScene();
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        binding.unity.addView(mTextureView, 0, layoutParams);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        mReCreateScene = true;
        //just for call setRequestedOrientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        super.onNewIntent(intent);

        maybeCreateScene();
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

    private void createScene(TextureView tv) {
        Log.i(TAG, "createScene");
        resetSceneState();
        resetViewVisibility();
        MetaChatContext.getInstance().createScene(this, KeyCenter.CHANNEL_ID, tv);
    }

    private void resetViewVisibility() {
        binding.back.setVisibility(View.GONE);
        binding.card.getRoot().setVisibility(View.GONE);
        binding.users.setVisibility(View.GONE);
        binding.mic.setVisibility(View.GONE);
        binding.speaker.setVisibility(View.GONE);
        binding.dressSetting.setVisibility(View.GONE);
        binding.btnSwitchLocalView.setVisibility(View.GONE);
        binding.btnSwitchRemoteView.setVisibility(View.GONE);

        binding.cancelBt.setVisibility(View.GONE);
        binding.saveBtn.setVisibility(View.GONE);
        binding.dressTab.setVisibility(View.GONE);
        binding.dressViewpage.setVisibility(View.GONE);
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
            }
        });
        resetSceneState();
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
    }

    @Override
    public void onUserPositionChanged(String uid, MetachatUserPositionInfo posInfo) {

    }

    @Override
    public void onEnumerateVideoDisplaysResult(String[] displayIds) {

    }

    @Override
    public void onCreateSceneResult(IMetachatScene scene, int errorCode) {
        //异步线程回调需在主线程处理
        runOnUiThread(() -> MetaChatContext.getInstance().enterScene());
    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {

    }

    @Override
    public void onRequestToken() {

    }

    @Override
    public void onGetSceneInfosResult(MetachatSceneInfo[] scenes, int errorCode) {

    }

    @Override
    public void onDownloadSceneProgress(long SceneId, int progress, int state) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        mIsFront = true;
        if (MetaChatContext.getInstance().isInScene()) {
            MetaChatContext.getInstance().resumeMedia();
        }
        if (MetaChatConstants.SCENE_DRESS == MetaChatContext.getInstance().getCurrentScene()) {
            initDressTab();
        }
        maybeCreateScene();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        mIsFront = false;
        //切换场景时候surface变更状态保留，重新进场景等待surface变更状态
        if (MetaChatContext.getInstance().getNextScene() == MetaChatConstants.SCENE_NONE) {
            mSurfaceSizeChange = false;
        }
        if (MetaChatContext.getInstance().isInScene()) {
            MetaChatContext.getInstance().pauseMedia();
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void initDressTab() {
        getLayoutInflater();
        LayoutInflater lf = LayoutInflater.from(this);
        Map<View, String> viewDressTypeMap = new LinkedHashMap<>();

        mTabEntities.clear();
        mCurrentTabIndex = 0;
        View view;
        if (MetaChatConstants.GENDER_WOMEN == MetaChatContext.getInstance().getRoleInfo().getGender()) {
            for (TabEntity tabEntity : SkinsData.TAB_ENTITY_WOMEN) {
                mTabEntities.add(tabEntity);

                view = lf.inflate(R.layout.viewpager_skin_layout, null);
                viewDressTypeMap.put(view, tabEntity.getDressType());
            }

        } else {
            for (TabEntity tabEntity : SkinsData.TAB_ENTITY_MAN) {
                mTabEntities.add(tabEntity);
                view = lf.inflate(R.layout.viewpager_skin_layout, null);
                viewDressTypeMap.put(view, tabEntity.getDressType());
            }
        }


        binding.dressTab.setTabData(mTabEntities);
        binding.dressTab.setCurrentTab(mCurrentTabIndex);
        binding.dressTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mCurrentTabIndex = position;
                binding.dressViewpage.setCurrentItem(mCurrentTabIndex);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        mTabItemAdapters = new ArrayList<>(mTabEntities.size());

        for (Map.Entry<View, String> entry : viewDressTypeMap.entrySet()) {
            initGridView(entry.getKey(), entry.getValue());
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this.getApplicationContext(), new ArrayList<>(viewDressTypeMap.keySet()));
        binding.dressViewpage.setAdapter(viewPagerAdapter);
        binding.dressViewpage.setScrollable(false);

        binding.dressViewpage.setCurrentItem(mCurrentTabIndex);

        binding.dressViewpage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected position=" + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * 加载tab下的gridview控件
     */
    private void initGridView(View view, String viewDressType) {
        List<SkinInfo> list = SkinsData.getDressInfo(viewDressType);
        if (null == list) {
            return;
        }

        List<SkinInfo> dataList = new ArrayList<>(list);

        int selectIndex = 0;
        RoleInfo roleInfo = MetaChatContext.getInstance().getRoleInfo();
        if (null != roleInfo) {
            if (SkinsData.KEY_WOMEN_CLOTHING.equalsIgnoreCase(viewDressType) ||
                    SkinsData.KEY_MAN_CLOTHING.equalsIgnoreCase(viewDressType)) {
                selectIndex = roleInfo.getTops() - 1;
            } else if (SkinsData.KEY_WOMEN_HAIRPIN.equalsIgnoreCase(viewDressType) ||
                    SkinsData.KEY_MAN_HAIRPIN.equalsIgnoreCase(viewDressType)) {
                selectIndex = roleInfo.getHair() - 1;
            } else if (SkinsData.KEY_WOMEN_SHOES.equalsIgnoreCase(viewDressType) ||
                    SkinsData.KEY_MAN_SHOES.equalsIgnoreCase(viewDressType)) {
                selectIndex = roleInfo.getShoes() - 1;
            } else if (SkinsData.KEY_WOMEN_TROUSERS.equalsIgnoreCase(viewDressType) ||
                    SkinsData.KEY_MAN_TROUSERS.equalsIgnoreCase(viewDressType)) {
                selectIndex = roleInfo.getLower() - 1;
            }
            if (selectIndex < 0) {
                selectIndex = 0;
            }
        }


        ViewPager mCurrentTabViewPager = view.findViewById(R.id.viewpage_skin_item);
        CirclePageIndicator indicator = (CirclePageIndicator) view.findViewById(R.id.indicator);

        //总的页数=总数/每页数量，并向上取整取整
        int totalPage = (int) Math.ceil(dataList.size() * 1.0 / SKIN_TAB_MAX_PAGE_SIZE);
        List<View> mViewPagerList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            dataList.get(i).setCheck(i == selectIndex);
        }
        SkinGridViewAdapter adapter;
        for (int i = 0; i < totalPage; i++) {
            View pagerView = getLayoutInflater().inflate(R.layout.skin_gridview, null);
            //初始化gridview的控件并绑定
            final GridView gridView = pagerView.findViewById(R.id.gridview);
            adapter = new SkinGridViewAdapter(this, dataList, i, SKIN_TAB_MAX_PAGE_SIZE, this);
            gridView.setAdapter(adapter);
            //每一个GridView作为一个View对象添加到ViewPager集合中
            mViewPagerList.add(gridView);
            mTabItemAdapters.add(adapter);
        }
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(this, mViewPagerList);
        mCurrentTabViewPager.setAdapter(mViewPagerAdapter);
        indicator.setViewPager(mCurrentTabViewPager);
    }

    @Override
    public void onSkinItemClick(int position) {
        for (SkinGridViewAdapter adapter : mTabItemAdapters) {
            adapter.notifyDataSetChanged();
        }

        TabEntity tabEntity = (TabEntity) mTabEntities.get(mCurrentTabIndex);
        RoleInfo roleInfo = MetaChatContext.getInstance().getRoleInfo();
        if (null != roleInfo) {
            if (SkinsData.KEY_WOMEN_CLOTHING.equalsIgnoreCase(tabEntity.getDressType()) ||
                    SkinsData.KEY_MAN_CLOTHING.equalsIgnoreCase(tabEntity.getDressType())) {
                roleInfo.setTops(position + 1);
            }
            if (SkinsData.KEY_WOMEN_HAIRPIN.equalsIgnoreCase(tabEntity.getDressType()) ||
                    SkinsData.KEY_MAN_HAIRPIN.equalsIgnoreCase(tabEntity.getDressType())) {
                roleInfo.setHair(position + 1);
            }

            if (SkinsData.KEY_WOMEN_TROUSERS.equalsIgnoreCase(tabEntity.getDressType()) ||
                    SkinsData.KEY_MAN_TROUSERS.equalsIgnoreCase(tabEntity.getDressType())) {
                roleInfo.setLower(position + 1);
            }

            if (SkinsData.KEY_WOMEN_SHOES.equalsIgnoreCase(tabEntity.getDressType()) ||
                    SkinsData.KEY_MAN_SHOES.equalsIgnoreCase(tabEntity.getDressType())) {
                roleInfo.setShoes(position + 1);
            }

            MetaChatContext.getInstance().sendRoleDressInfo();
        }
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

    private void maybeCreateScene() {
        Log.i(TAG, "maybeCreateScene,mReCreateScene=" + mReCreateScene + ",mSurfaceSizeChange=" + mSurfaceSizeChange + ",mIsFront=" + mIsFront);
        if (mReCreateScene && mSurfaceSizeChange && mIsFront) {
            createScene(mTextureView);
        }
    }

    private void resetSceneState() {
        mReCreateScene = false;
        mSurfaceSizeChange = false;
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
}
