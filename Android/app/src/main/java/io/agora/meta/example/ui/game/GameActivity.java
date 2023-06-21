package io.agora.meta.example.ui.game;

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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
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

import io.agora.base.JavaI420Buffer;
import io.agora.base.VideoFrame;
import io.agora.meta.IMetaScene;
import io.agora.meta.IMetaSceneEventHandler;
import io.agora.meta.SceneDisplayConfig;
import io.agora.meta.example.MainActivity;
import io.agora.meta.example.adapter.DressTypeAdapter;
import io.agora.meta.example.adapter.DressTypeAssetAdapter;
import io.agora.meta.example.adapter.FaceTypeAdapter;
import io.agora.meta.example.adapter.FaceTypeShapesAdapter;
import io.agora.meta.example.adapter.SurfaceViewAdapter;
import io.agora.meta.example.databinding.GameActivityBinding;
import io.agora.meta.example.dialog.CustomDialog;
import io.agora.meta.example.inf.IMetaEventHandler;
import io.agora.meta.example.inf.IRtcEventCallback;
import io.agora.meta.example.meta.MetaContext;
import io.agora.meta.example.models.FaceParameterItem;
import io.agora.meta.example.models.UnityMessage;
import io.agora.meta.example.models.manifest.DressItemResource;
import io.agora.meta.example.models.SurfaceViewInfo;
import io.agora.meta.example.models.manifest.FaceBlendShape;
import io.agora.meta.example.models.manifest.FaceBlendShapeItem;
import io.agora.meta.example.utils.DressAndFaceDataUtils;
import io.agora.meta.example.utils.KeyCenter;
import io.agora.meta.example.utils.MetaConstants;
import io.agora.meta.example.R;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.IVideoFrameObserver;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class GameActivity extends Activity implements IMetaEventHandler, IRtcEventCallback {

    private final String TAG = GameActivity.class.getSimpleName();
    private GameActivityBinding binding;

    private TextureView mTextureView = null;
    private boolean mReCreateScene;
    private boolean mSurfaceSizeChange;
    private boolean mIsFront;

    private final List<SurfaceViewInfo> mLocalSurfaceViewList = new ArrayList<>();
    private final List<SurfaceViewInfo> mRemoteSurfaceViewList = new ArrayList<>();

    private SurfaceViewAdapter mRemoteViewAdapter;
    private SurfaceViewAdapter mLocalViewAdapter;
    private SurfaceView mLocalPreviewSurfaceView;

    private TextureView mLocalAvatarTextureView;
    private TextureView mLocalAvatarTextureView1;
    private TextureView mLocalAvatarTextureView2;
    private TextureView mLocalAvatarTextureView3;
    private TextureView mLocalAvatarTextureView4;

    private SurfaceTexture mSaveLocalAvatarSurfaceTexture;

    private int mFrameWidth = -1;
    private int mFrameHeight = -1;

    private DressTypeAdapter mDressTypeAdapter;
    private DressTypeAssetAdapter mDressTypeAssetAdapter;
    private List<DressItemResource> mDressResourceDataList;

    private DressItemResource mCurrentDressItemResource;

    private FaceTypeAdapter mFaceTypeAdapter;
    private FaceTypeShapesAdapter mFaceTypeShapesAdapter;
    private List<FaceBlendShape> mFaceBlendShapeDataList;
    private FaceBlendShape mCurrentFaceBlendShape;

    private boolean mJoinChannelSuccess;

    private boolean mEnableRemotePreviewAvatar;

    private int mViewMode = 0;

    private final ObservableBoolean isEnterScene = new ObservableBoolean(false);
    private final ObservableBoolean enableMic = new ObservableBoolean(true);
    private final ObservableBoolean enableSpeaker = new ObservableBoolean(true);
    private final ObservableBoolean isBroadcaster = new ObservableBoolean(true);
    private final Observable.OnPropertyChangedCallback callback =
            new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (sender == isEnterScene) {
                        if (MetaConstants.SCENE_DRESS == MetaContext.getInstance().getCurrentScene()) {
                            binding.sceneDressAndFaceGroup.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.sceneGameGroup.setVisibility(View.GONE);

                            if (isEnterScene.get()) {
                                binding.dressSettingLayout.setVisibility(View.VISIBLE);
                                binding.faceSettingLayout.setVisibility(View.GONE);
                            }
                        } else if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
                            binding.card.nickname.setText(MetaContext.getInstance().getRoleInfo().getName());

                            binding.sceneGameGroup.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.sceneDressAndFaceGroup.setVisibility(View.GONE);
                        }
                        if (isEnterScene.get()) {
                            MetaContext.getInstance().sendRoleDressInfo(Arrays.stream(MetaContext.getInstance().getRoleInfo().getDressResourceMap().values().toArray(new Integer[0])).mapToInt(Integer::valueOf).toArray());
//                            MetaContext.getInstance().sendRoleFaceInfo();
                        }
                    } else if (sender == enableMic) {
                        if (!MetaContext.getInstance().enableLocalAudio(enableMic.get())) {
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
                        if (!MetaContext.getInstance().muteAllRemoteAudioStreams(!enableSpeaker.get())) {
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
                        if (!MetaContext.getInstance().updateRole(isBroadcaster.get() ?
                                Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE)) {
                            return;
                        }
                        binding.card.nickname.setText(MetaContext.getInstance().getRoleInfo().getName());
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

        MetaContext.getInstance().registerMetaSceneEventHandler(this);
        MetaContext.getInstance().registerMetaServiceEventHandler(this);
        MetaContext.getInstance().setRtcEventCallback(this);

        initMainUnityView();

        initView();

        initListener();
        // MetaContext.getInstance().joinChannel();
    }

    private void initView() {
        binding.uidTv.setText("UID:" + KeyCenter.RTC_UID);
    }

    private void initLocalSurfaceView() {
        if (MetaConstants.SCENE_GAME != MetaContext.getInstance().getCurrentScene()) {
            return;
        }
        if (null == mLocalPreviewSurfaceView) {
            mLocalPreviewSurfaceView = new SurfaceView(getApplicationContext());
        }

        RtcEngine rtcEngine = MetaContext.getInstance().getRtcEngine();
        VideoCanvas videoCanvas = new VideoCanvas(mLocalPreviewSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        videoCanvas.position = Constants.VideoModulePosition.VIDEO_MODULE_POSITION_POST_CAPTURER_ORIGIN;
        rtcEngine.setupLocalVideo(videoCanvas);
        rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);

        rtcEngine.registerVideoFrameObserver(new IVideoFrameObserver() {
            @Override
            public boolean onCaptureVideoFrame(VideoFrame videoFrame) {
//                if (null != videoFrame.getMetaInfo() && videoFrame.getMetaInfo().getCustomMetaInfo(MetaConstants.KEY_FACE_CAPTURE_INFO).size() > 0) {
//                    FaceCaptureInfo faceCaptureInfo = (FaceCaptureInfo) videoFrame.getMetaInfo().getCustomMetaInfo(MetaConstants.KEY_FACE_CAPTURE_INFO).get(0);
//                    UnityMessage unityMessage = new UnityMessage();
//                    unityMessage.setKey(MetaConstants.KEY_UNITY_MESSAGE_FACE_CAPTURE);
//                    unityMessage.setValue(faceCaptureInfo.toString());
//                    MetaContext.getInstance().sendSceneMessage(JSONObject.toJSONString(unityMessage));
//                }
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


        addLocalTextureView();

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

    private void addVideoFrameObserver() {
        RtcEngine rtcEngine = MetaContext.getInstance().getRtcEngine();

        rtcEngine.registerVideoFrameObserver(new IVideoFrameObserver() {
            @Override
            public boolean onCaptureVideoFrame(VideoFrame videoFrame) {
                MetaContext.getInstance().pushExternalVideoFrame(videoFrame);
//                if (null != videoFrame.getMetaInfo() && videoFrame.getMetaInfo().getCustomMetaInfo(MetaConstants.KEY_FACE_CAPTURE_INFO).size() > 0) {
//                    FaceCaptureInfo faceCaptureInfo = (FaceCaptureInfo) videoFrame.getMetaInfo().getCustomMetaInfo(MetaConstants.KEY_FACE_CAPTURE_INFO).get(0);
//                    UnityMessage unityMessage = new UnityMessage();
//                    unityMessage.setKey(MetaConstants.KEY_UNITY_MESSAGE_FACE_CAPTURE);
//                    unityMessage.setValue(faceCaptureInfo.toString());
//                    MetaContext.getInstance().sendSceneMessage(JSONObject.toJSONString(unityMessage));
//                }
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


    }

    private void addLocalAvatarView(TextureView textureView) {
        SceneDisplayConfig sceneDisplayConfig = new SceneDisplayConfig();
        sceneDisplayConfig.width = mLocalPreviewSurfaceView.getMeasuredWidth();
        sceneDisplayConfig.height = mLocalPreviewSurfaceView.getMeasuredHeight();
        MetaContext.getInstance().addSceneView(textureView, sceneDisplayConfig);
    }

    private void localAvatarViewReady() {
        binding.unity.removeView(mLocalAvatarTextureView);
        mLocalSurfaceViewList.add(new SurfaceViewInfo(mLocalAvatarTextureView, 0));
        localViewDataChanged();
    }

    private void initRemoteSurfaceView() {
        if (MetaConstants.SCENE_GAME != MetaContext.getInstance().getCurrentScene()) {
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
            viewGroup.removeView(mLocalAvatarTextureView1);
            viewGroup.removeView(mLocalAvatarTextureView2);
            viewGroup.removeView(mLocalAvatarTextureView3);
            viewGroup.removeView(mLocalAvatarTextureView4);
        }
        RtcEngine rtcEngine = MetaContext.getInstance().getRtcEngine();
        rtcEngine.stopPreview();
        mLocalSurfaceViewList.clear();
        binding.rvLocalView.setVisibility(View.GONE);
        mLocalAvatarTextureView = null;
        mLocalAvatarTextureView1 = null;
        mLocalAvatarTextureView2 = null;
        mLocalAvatarTextureView3 = null;
        mLocalAvatarTextureView4 = null;
        mFrameWidth = -1;
        mFrameHeight = -1;
    }

    private void removeRemoteSurfaceView() {
        mRemoteSurfaceViewList.clear();
        binding.rvRemoteView.setVisibility(View.GONE);
    }

    @SuppressLint("CheckResult")
    private void initListener() {
        isEnterScene.addOnPropertyChangedCallback(callback);
        enableMic.addOnPropertyChangedCallback(callback);
        enableSpeaker.addOnPropertyChangedCallback(callback);
        isBroadcaster.addOnPropertyChangedCallback(callback);

        RxView.clicks(binding.back).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isEnterScene.set(false);
            MetaContext.getInstance().resetRoleInfo();
            if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
                MetaContext.getInstance().removeSceneView(mLocalAvatarTextureView);
            } else {
                MetaContext.getInstance().leaveScene();
            }
            MetaContext.getInstance().setCurrentScene(MetaConstants.SCENE_NONE);
        });

        RxView.clicks(binding.cancelBt).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isEnterScene.set(false);
            MetaContext.getInstance().cancelRoleDressInfo(MetaContext.getInstance().getRoleInfo().getName()
                    , MetaContext.getInstance().getRoleInfo().getGender());
            //MetaContext.getInstance().setNextScene(MetaConstants.SCENE_GAME);
            MetaContext.getInstance().leaveScene();
        });


        RxView.clicks(binding.saveBtn).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isEnterScene.set(false);
            MetaContext.getInstance().saveRoleDressInfo(MetaContext.getInstance().getRoleInfo().getName()
                    , MetaContext.getInstance().getRoleInfo().getGender());
            //MetaContext.getInstance().setNextScene(MetaConstants.SCENE_GAME);
            MetaContext.getInstance().leaveScene();
        });

        RxView.clicks(binding.dressSetting).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isEnterScene.set(false);
            if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
                MetaContext.getInstance().removeSceneView(mLocalAvatarTextureView);
            } else {
                MetaContext.getInstance().leaveScene();
            }
            MetaContext.getInstance().setNextScene(MetaConstants.SCENE_DRESS);
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

        RxView.clicks(binding.dressSettingBt).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            binding.dressSettingLayout.setVisibility(View.VISIBLE);
            binding.faceSettingLayout.setVisibility(View.GONE);
        });

        RxView.clicks(binding.faceSettingBt).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            binding.dressSettingLayout.setVisibility(View.GONE);
            binding.faceSettingLayout.setVisibility(View.VISIBLE);
        });

        RxView.clicks(binding.zoomAvatarBt).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            updateViewMode();
        });


        RxView.clicks(binding.btnSwitchRemotePreview).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            mEnableRemotePreviewAvatar = !mEnableRemotePreviewAvatar;
            MetaContext.getInstance().enableSceneVideo(mLocalAvatarTextureView, mEnableRemotePreviewAvatar);
            binding.btnSwitchRemotePreview.setText(mEnableRemotePreviewAvatar ? getApplicationContext().getResources().getString(R.string.btn_switch_remote_preview_camera) :
                    getApplicationContext().getResources().getString(R.string.btn_switch_remote_preview_avatar));
        });
    }

    private void initMainUnityView() {
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
                if (MetaConstants.SCENE_NONE == MetaContext.getInstance().getNextScene()) {
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

        ConstraintLayout.LayoutParams unityLayoutParams = (ConstraintLayout.LayoutParams) binding.unity.getLayoutParams();
        unityLayoutParams.bottomMargin = 0;
        binding.unity.setLayoutParams(unityLayoutParams);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mReCreateScene = true;
        //just for call setRequestedOrientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        super.onNewIntent(intent);
        // MetaContext.getInstance().joinChannel();

        maybeCreateScene();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        isEnterScene.removeOnPropertyChangedCallback(callback);
        enableMic.removeOnPropertyChangedCallback(callback);
        enableSpeaker.removeOnPropertyChangedCallback(callback);
        isBroadcaster.removeOnPropertyChangedCallback(callback);
        MetaContext.getInstance().unregisterMetaServiceEventHandler(this);
    }

    private void maybeCreateScene() {
        Log.i(TAG, "maybeCreateScene,mReCreateScene=" + mReCreateScene + ",mSurfaceSizeChange=" + mSurfaceSizeChange + ",mIsFront=" + mIsFront + ",mJoinChannelSuccess=" + mJoinChannelSuccess);
        if (MetaConstants.SCENE_DRESS == MetaContext.getInstance().getCurrentScene()) {
            mSurfaceSizeChange = true;
        }
        if (mReCreateScene && mSurfaceSizeChange && mIsFront /*&& mJoinChannelSuccess*/) {
            resetSceneState();
            resetViewVisibility();
            initDressAndFaceData();
            MetaContext.getInstance().createScene(this, mTextureView);
        }
    }

    private void resetSceneState() {
        mReCreateScene = false;
        mSurfaceSizeChange = false;
        mEnableRemotePreviewAvatar = true;
        mViewMode = 0;
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

            if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
                initLocalSurfaceView();
                initRemoteSurfaceView();
            } else if (MetaConstants.SCENE_DRESS == MetaContext.getInstance().getCurrentScene()) {
                addVideoFrameObserver();
                initDressTypeView();
                initFaceTypeView();
                updateViewMode();
                pushEmptyFrame();
            }

            if (MetaConstants.SCENE_DRESS == MetaContext.getInstance().getCurrentScene()) {
                MetaContext.getInstance().enableSceneVideo(mTextureView, true);
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
                    MetaContext.getInstance().destroy();
                }
            });

            MetaContext.getInstance().setCurrentScene(MetaConstants.SCENE_NONE);
            if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT != getRequestedOrientation()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    @Override
    public void onSceneVideoFrameCaptured(TextureView view, VideoFrame videoFrame) {
        if (null == videoFrame) {
            return;
        }
        if (view == mLocalAvatarTextureView) {
            if ((mFrameWidth == -1 && mFrameHeight == -1) || (mFrameWidth != videoFrame.getBuffer().getWidth() || mFrameHeight != videoFrame.getBuffer().getHeight())) {
                mFrameWidth = videoFrame.getBuffer().getWidth();
                mFrameHeight = videoFrame.getBuffer().getHeight();
                // update set video configuration
                MetaContext.getInstance().getRtcEngine().setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                        new VideoEncoderConfiguration.VideoDimensions(mFrameWidth, mFrameHeight),
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                        STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE, VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED));
            }
            if (!MetaContext.getInstance().pushExternalVideoFrame(videoFrame)) {
                Log.e(TAG, "pushExternalVideoFrame fail");
            }
        }

    }

    @Override
    public void onAddSceneViewResult(TextureView view, int errorCode) {
        if (view.equals(mLocalAvatarTextureView)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MetaContext.getInstance().enableSceneVideo(mLocalAvatarTextureView, mEnableRemotePreviewAvatar);
                }
            });
        }
    }

    @Override
    public void onRemoveSceneViewResult(TextureView view, int errorCode) {
        if (view.equals(mLocalAvatarTextureView)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeLocalSurfaceView();
                    removeRemoteSurfaceView();
                    MetaContext.getInstance().leaveScene();
                }
            });
        }
    }

    @Override
    public void onSceneMessageReceived(byte[] message) {
        String jsonStr = new String(message);
        Log.e(TAG, "onRecvMessageFromScene jsonStr:" + jsonStr);
//        try {
//            JSONObject jsonObject = JSON.parseObject(jsonStr);
//            if (!TextUtils.isEmpty(jsonObject.getString("key"))) {
//                if (MetaConstants.SCENE_MESSAGE_ADD_SCENE_VIEW_SUCCESS.equals(jsonObject.getString("key"))) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            MetaContext.getInstance().enableSceneVideo(mLocalAvatarTextureView, mEnableRemotePreviewAvatar);
//                        }
//                    });
//                } else if (MetaConstants.SCENE_MESSAGE_REMOVE_SCENE_VIEW_SUCCESS.equals(jsonObject.getString("key"))) {
//                    //maybe to do something
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            removeLocalSurfaceView();
//                            removeRemoteSurfaceView();
//                            MetaContext.getInstance().leaveScene();
//                        }
//                    });
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onCreateSceneResult(IMetaScene scene, int errorCode) {
        //异步线程回调需在主线程处理
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MetaContext.getInstance().enterScene();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        mIsFront = true;
        if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
            if (MetaContext.getInstance().isInScene()) {
                MetaContext.getInstance().resumeMedia();
            }
        }
        maybeCreateScene();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        mIsFront = false;
        if (MetaContext.getInstance().getNextScene() == MetaConstants.SCENE_NONE) {
            mSurfaceSizeChange = false;
        }
        if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
            if (MetaContext.getInstance().isInScene()) {
                MetaContext.getInstance().pauseMedia();
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (MetaConstants.SCENE_DRESS == MetaContext.getInstance().getCurrentScene()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        super.setRequestedOrientation(requestedOrientation);
    }


    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        Log.i(TAG, "onFirstRemoteVideoDecoded width:" + width + " height:" + height);
        if (MetaConstants.SCENE_GAME != MetaContext.getInstance().getCurrentScene()) {
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
                    ret = MetaContext.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "onUserJoined setupRemoteVideo ret=" + ret);

            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        if (MetaConstants.SCENE_GAME != MetaContext.getInstance().getCurrentScene()) {
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
                    MetaContext.getInstance().getRtcEngine().setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_FIT, uid));
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

    private void initDressAndFaceData() {
        if (MetaContext.getInstance().getCurrentScene() == MetaConstants.SCENE_DRESS) {
            DressItemResource[] dressItemResources = DressAndFaceDataUtils.getInstance().getDressResources(MetaContext.getInstance().getRoleInfo().getAvatarType());
            if (null != dressItemResources) {
                mDressResourceDataList = Arrays.asList(dressItemResources);
            }

            FaceBlendShape[] faceBlendShapes = DressAndFaceDataUtils.getInstance().getFaceBlendShapes(MetaContext.getInstance().getRoleInfo().getAvatarType());
            if (null != faceBlendShapes) {
                mFaceBlendShapeDataList = Arrays.asList(faceBlendShapes);
            }
        }
    }

    private void initDressTypeView() {
        if (MetaConstants.SCENE_DRESS != MetaContext.getInstance().getCurrentScene()) {
            return;
        }

        if (null == mDressResourceDataList) {
            Log.i(TAG, "dress resource list is null");
            return;
        }

        mCurrentDressItemResource = mDressResourceDataList.get(0);
        if (mDressTypeAdapter == null) {
            mDressTypeAdapter = new DressTypeAdapter(getApplicationContext());

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            binding.rvDressType.setLayoutManager(linearLayoutManager);
            binding.rvDressType.setAdapter(mDressTypeAdapter);
            mDressTypeAdapter.setOnItemClickCallBack(new DressTypeAdapter.OnItemClickCallBack() {
                @Override
                public void onItemClick(DressItemResource dressItemResource) {
                    mCurrentDressItemResource = dressItemResource;
                    if (null != mDressTypeAssetAdapter) {
                        setDressTypeAssetData(Arrays.asList(Arrays.stream(mCurrentDressItemResource.getAssets()).boxed().toArray(Integer[]::new)));
                    }
                }
            });
        }
        mDressTypeAdapter.setDataList(mDressResourceDataList);


        String iconFilePath = DressAndFaceDataUtils.getInstance().getIconFilePath(MetaContext.getInstance().getRoleInfo().getAvatarType());
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
                    mDressTypeAssetAdapter = new DressTypeAssetAdapter(getApplicationContext());
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
                    binding.rvDressTypeAsset.setLayoutManager(gridLayoutManager);
                    binding.rvDressTypeAsset.setAdapter(mDressTypeAssetAdapter);

                    mDressTypeAssetAdapter.setOnItemClickCallBack(new DressTypeAssetAdapter.OnItemClickCallBack() {
                        @Override
                        public void onItemClick(int resId) {
                            MetaContext.getInstance().getRoleInfo().updateDressResource(mCurrentDressItemResource.getId(), resId);
                            MetaContext.getInstance().sendRoleDressInfo(new int[]{resId});
                        }
                    });

                }
                mDressTypeAssetAdapter.setAssetMap(assetMap);
                setDressTypeAssetData(Arrays.asList(Arrays.stream(mCurrentDressItemResource.getAssets()).boxed().toArray(Integer[]::new)));
            }
        }
    }

    private void setDressTypeAssetData(List<Integer> list) {
        Integer resId = MetaContext.getInstance().getRoleInfo().getDressResourceMap().get(mCurrentDressItemResource.getId());
        if (null != mDressTypeAssetAdapter) {
            if (null == resId) {
                resId = -1;
            } else {
                if (!list.contains(resId)) {
                    resId = -1;
                }
            }
            mDressTypeAssetAdapter.setDataList(list, resId);
            binding.rvDressTypeAsset.scrollToPosition(0);

        }
    }

    private void initFaceTypeView() {
        if (MetaConstants.SCENE_DRESS != MetaContext.getInstance().getCurrentScene()) {
            return;
        }

        if (null == mFaceBlendShapeDataList) {
            Log.i(TAG, "face data list is null");
            return;
        }

        mCurrentFaceBlendShape = mFaceBlendShapeDataList.get(0);
        if (mFaceTypeAdapter == null) {
            mFaceTypeAdapter = new FaceTypeAdapter(getApplicationContext());
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            binding.rvFaceType.setLayoutManager(linearLayoutManager);
            binding.rvFaceType.setAdapter(mFaceTypeAdapter);
            mFaceTypeAdapter.setOnItemClickCallBack(new FaceTypeAdapter.OnItemClickCallBack() {
                @Override
                public void onItemClick(FaceBlendShape faceBlendShape) {
                    mCurrentFaceBlendShape = faceBlendShape;
                    if (null != mFaceTypeShapesAdapter) {
                        setFaceTypeShapesData(Arrays.asList(mCurrentFaceBlendShape.getShapes()));
                    }
                }
            });
        }
        mFaceTypeAdapter.setDataList(mFaceBlendShapeDataList);


        if (mFaceTypeShapesAdapter == null) {
            mFaceTypeShapesAdapter = new FaceTypeShapesAdapter(getApplicationContext());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
            binding.rvFaceTypeShapes.setLayoutManager(gridLayoutManager);
            binding.rvFaceTypeShapes.setAdapter(mFaceTypeShapesAdapter);

            mFaceTypeShapesAdapter.setOnShapeChangeCallBack(new FaceTypeShapesAdapter.OnShapeChangeCallBack() {
                @Override
                public void onShapeChange(FaceBlendShapeItem faceBlendShapeItem, int value) {
                    MetaContext.getInstance().getRoleInfo().updateFaceParameter(faceBlendShapeItem.getKey(), value);
                    MetaContext.getInstance().sendRoleFaceInfo(new FaceParameterItem[]{new FaceParameterItem(faceBlendShapeItem.getKey(), value)});
                }
            });
        }
        setFaceTypeShapesData(Arrays.asList(mCurrentFaceBlendShape.getShapes()));
    }

    private void setFaceTypeShapesData(List<FaceBlendShapeItem> list) {
        if (null != mFaceTypeShapesAdapter) {
            mFaceTypeShapesAdapter.setDataList(list, MetaContext.getInstance().getRoleInfo().getFaceParameterResourceMap());
            binding.rvFaceTypeShapes.scrollToPosition(0);
        }
    }

    private void updateUnityViewHeight() {
        binding.dressSettingLayout.post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.unity.getLayoutParams();
                if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
                    layoutParams.bottomMargin = 0;
                } else if (MetaConstants.SCENE_DRESS == MetaContext.getInstance().getCurrentScene()) {
                    layoutParams.bottomMargin = binding.dressSettingLayout.getMeasuredHeight();
                }
                binding.unity.setLayoutParams(layoutParams);
            }
        });
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.i(TAG, "onJoinChannelSuccess");
        mJoinChannelSuccess = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // maybeCreateScene();
                MetaContext.getInstance().enableVoiceDriveAvatar(true);
            }
        });
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        mJoinChannelSuccess = false;
    }

    private void addLocalTextureView() {
        mLocalAvatarTextureView = new TextureView(this);
        mLocalAvatarTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "localTextureView onSurfaceTextureAvailable surface=" + surface);
                addLocalAvatarView(mLocalAvatarTextureView);
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
        mLocalSurfaceViewList.add(new SurfaceViewInfo(mLocalAvatarTextureView, KeyCenter.RTC_UID));

        mLocalAvatarTextureView1 = new TextureView(this);
        mLocalAvatarTextureView1.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "localTextureView onSurfaceTextureAvailable surface=" + surface);
                addLocalAvatarView(mLocalAvatarTextureView1);
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
        mLocalSurfaceViewList.add(new SurfaceViewInfo(mLocalAvatarTextureView1, KeyCenter.RTC_UID));

        mLocalAvatarTextureView2 = new TextureView(this);
        mLocalAvatarTextureView2.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "localTextureView onSurfaceTextureAvailable surface=" + surface);
                addLocalAvatarView(mLocalAvatarTextureView2);
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
        mLocalSurfaceViewList.add(new SurfaceViewInfo(mLocalAvatarTextureView2, KeyCenter.RTC_UID));

        mLocalAvatarTextureView3 = new TextureView(this);
        mLocalAvatarTextureView3.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "localTextureView onSurfaceTextureAvailable surface=" + surface);
                addLocalAvatarView(mLocalAvatarTextureView3);
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
        mLocalSurfaceViewList.add(new SurfaceViewInfo(mLocalAvatarTextureView3, KeyCenter.RTC_UID));

        mLocalAvatarTextureView4 = new TextureView(this);
        mLocalAvatarTextureView4.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "localTextureView onSurfaceTextureAvailable surface=" + surface);
                addLocalAvatarView(mLocalAvatarTextureView4);
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
        mLocalSurfaceViewList.add(new SurfaceViewInfo(mLocalAvatarTextureView4, KeyCenter.RTC_UID));
    }

    private void updateViewMode() {
        UnityMessage message = new UnityMessage();
        message.setKey("setCamera");
        JSONObject valueJson = new JSONObject();
        valueJson.put("viewMode", ++mViewMode % 3);
        message.setValue(valueJson.toJSONString());
        MetaContext.getInstance().sendSceneMessage(JSONObject.toJSONString(message));
    }

    private void pushEmptyFrame() {
        JavaI420Buffer i420Buffer = JavaI420Buffer.allocate(240, 240);
        i420Buffer.getDataY().put(new byte[0], 0, 0);
        i420Buffer.getDataU().put(new byte[0], 0, 0);
        i420Buffer.getDataV().put(new byte[0], 0, 0);
        VideoFrame emptyFrame = new VideoFrame(i420Buffer, 0, System.currentTimeMillis());
        MetaContext.getInstance().pushExternalVideoFrame(emptyFrame);
    }
}
