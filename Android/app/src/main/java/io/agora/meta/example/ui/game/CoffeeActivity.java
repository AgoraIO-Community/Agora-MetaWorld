package io.agora.meta.example.ui.game;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.agora.base.VideoFrame;
import io.agora.meta.example.MainActivity;
import io.agora.meta.example.adapter.SurfaceViewAdapter;
import io.agora.meta.example.databinding.CoffeeActivityBinding;
import io.agora.meta.example.dialog.CustomDialog;
import io.agora.meta.example.meta.MetaContext;
import io.agora.meta.example.models.SurfaceViewInfo;
import io.agora.meta.example.utils.KeyCenter;
import io.agora.meta.example.R;
import io.agora.rtc2.Constants;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.IVideoFrameObserver;
import io.agora.rtc2.video.VideoCanvas;

public class CoffeeActivity extends BaseGameActivity {
    private final String TAG = CoffeeActivity.class.getSimpleName();
    private CoffeeActivityBinding binding;
    private final List<SurfaceViewInfo> mLocalSurfaceViewList = new ArrayList<>();
    private final List<SurfaceViewInfo> mRemoteSurfaceViewList = new ArrayList<>();

    private SurfaceViewAdapter mRemoteViewAdapter;
    private SurfaceViewAdapter mLocalViewAdapter;
    private SurfaceView mLocalPreviewSurfaceView;

    private TextureView mLocalAvatarTextureView;

    private boolean mEnableRemotePreviewAvatar;

    private final ObservableBoolean isEnterScene = new ObservableBoolean(false);
    private final ObservableBoolean enableMic = new ObservableBoolean(true);
    private final ObservableBoolean enableSpeaker = new ObservableBoolean(true);
    private final ObservableBoolean isBroadcaster = new ObservableBoolean(true);
    private final Observable.OnPropertyChangedCallback callback =
            new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (sender == isEnterScene) {
                        Log.i(TAG, "back click onEnterSceneResult2323");
                        binding.group.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                        binding.card.nickname.setText(MetaContext.getInstance().getRoleInfo().getName());
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initLayout() {
        super.initLayout();
        binding = CoffeeActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void initLocalSurfaceView() {
        mLocalSurfaceViewList.clear();

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


    private void initRemoteSurfaceView() {
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
        RtcEngine rtcEngine = MetaContext.getInstance().getRtcEngine();
        rtcEngine.stopPreview();
        mLocalSurfaceViewList.clear();
        binding.rvLocalView.setVisibility(View.GONE);
        mLocalAvatarTextureView = null;
    }

    private void removeRemoteSurfaceView() {
        mRemoteSurfaceViewList.clear();
        binding.rvRemoteView.setVisibility(View.GONE);
    }

    @Override
    protected void registerListener() {
        super.registerListener();
        isEnterScene.addOnPropertyChangedCallback(callback);
        enableMic.addOnPropertyChangedCallback(callback);
        enableSpeaker.addOnPropertyChangedCallback(callback);
        isBroadcaster.addOnPropertyChangedCallback(callback);
    }

    @Override
    protected void unregisterListener() {
        super.unregisterListener();

        isEnterScene.removeOnPropertyChangedCallback(callback);
        enableMic.removeOnPropertyChangedCallback(callback);
        enableSpeaker.removeOnPropertyChangedCallback(callback);
        isBroadcaster.removeOnPropertyChangedCallback(callback);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void initClickEvent() {
        super.initClickEvent();

        RxView.clicks(binding.back).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            Log.i(TAG, "back click");
            isEnterScene.set(false);
            exit();
            MetaContext.getInstance().resetRoleInfo();
            MetaContext.getInstance().removeSceneView(mLocalAvatarTextureView);
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

        RxView.clicks(binding.btnSwitchRemotePreview).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            mEnableRemotePreviewAvatar = !mEnableRemotePreviewAvatar;
            MetaContext.getInstance().enableSceneVideo(mLocalAvatarTextureView, mEnableRemotePreviewAvatar);
            binding.btnSwitchRemotePreview.setText(mEnableRemotePreviewAvatar ? getApplicationContext().getResources().getString(R.string.btn_switch_remote_preview_camera) :
                    getApplicationContext().getResources().getString(R.string.btn_switch_remote_preview_avatar));
        });
    }

    @Override
    public void initMainUnityView() {
        super.initMainUnityView();
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
    }


    @Override
    protected void resetCreateSceneState() {
        super.resetCreateSceneState();
        resetSceneState();
        resetViewVisibility();
    }

    private void resetSceneState() {
        mReCreateScene = false;
        mEnableRemotePreviewAvatar = true;
    }


    private void resetViewVisibility() {
        binding.group.setVisibility(View.GONE);
    }

    @Override
    public void onEnterSceneResult(int errorCode) {
        runOnUiThread(() -> {
            if (errorCode != 0) {
                Toast.makeText(this, String.format(Locale.getDefault(), "EnterSceneFailed %d", errorCode), Toast.LENGTH_LONG).show();
                return;
            }
            Log.i(TAG, "back click onEnterSceneResult");
            isEnterScene.set(true);
            enableMic.set(true);
            enableSpeaker.set(true);
            isBroadcaster.set(true);

            initLocalSurfaceView();
            initRemoteSurfaceView();

            MetaContext.getInstance().enableVideo();
            MetaContext.getInstance().joinChannel();
        });
        resetSceneState();
    }

    @Override
    public void onReleasedScene(int status) {
        super.onReleasedScene(status);
        if (status == 0) {
            if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT != getRequestedOrientation()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            Intent intent = new Intent(CoffeeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
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
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (MetaContext.getInstance().isInScene()) {
            MetaContext.getInstance().resumeMedia();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (MetaContext.getInstance().isInScene()) {
            MetaContext.getInstance().pauseMedia();
        }
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        Log.i(TAG, "onFirstRemoteVideoDecoded width:" + width + " height:" + height);
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

    private void addLocalTextureView() {
        mLocalAvatarTextureView = new TextureView(this);
        mLocalAvatarTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "localTextureView onSurfaceTextureAvailable surface=" + surface);
                addLocalAvatarView(mLocalAvatarTextureView,
                        mLocalPreviewSurfaceView.getMeasuredWidth(), mLocalPreviewSurfaceView.getMeasuredHeight(),
                        KeyCenter.RTC_UID,
                        MetaContext.getInstance().getRandomAvatarName(CoffeeActivity.this.getApplicationContext()));
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
    }
}
