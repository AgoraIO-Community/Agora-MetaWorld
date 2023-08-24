package io.agora.meta.example.ui.game;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jakewharton.rxbinding2.view.RxView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.agora.meta.MetaSceneOptions;
import io.agora.meta.example.MainActivity;
import io.agora.meta.example.R;
import io.agora.meta.example.adapter.SurfaceViewAdapter;
import io.agora.meta.example.databinding.VoiceChatActivityBinding;
import io.agora.meta.example.meta.MetaContext;
import io.agora.meta.example.models.SurfaceViewInfo;
import io.agora.meta.example.utils.AudioFileReader;
import io.agora.meta.example.utils.KeyCenter;
import io.agora.meta.example.utils.MetaConstants;

public class VoiceChatActivity extends BaseGameActivity {
    private final String TAG = VoiceChatActivity.class.getSimpleName();
    private VoiceChatActivityBinding binding;

    private final List<SurfaceViewInfo> mAllSurfaceViewList = new ArrayList<>();
    private SurfaceViewAdapter mAllViewAdapter;

    private AudioFileReader mAudioFileReader;

    private boolean mIsExit;

    private TextureView mAddAvatarTextureView;

    private boolean mVoiceDriverByChannel;

    private int mCurFakeUid = 1000;

    private final ObservableBoolean isEnterScene = new ObservableBoolean(false);
    private final Observable.OnPropertyChangedCallback callback =
            new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (sender == isEnterScene) {
                        if (MetaContext.getInstance().getCurrentScene() == MetaConstants.SCENE_FACE_CAPTURE_CHAT) {
                            binding.faceCaptureGroup.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                        } else {
                            binding.group.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                        }
                    }
                }
            };

    @Override
    protected void initLayout() {
        binding = VoiceChatActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initData() {
        super.initData();
        mAllSurfaceViewList.clear();
        mIsExit = false;
        mVoiceDriverByChannel = true;
    }

    @Override
    protected void initView() {
        super.initView();
        binding.roomNameTv.setText(String.format("房间号：%s", MetaContext.getInstance().getRoomName()));

        if (mAllViewAdapter == null) {
            mAllViewAdapter = new SurfaceViewAdapter(getApplicationContext());
            mAllViewAdapter.setSurfaceViewData(mAllSurfaceViewList);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
            binding.rvAllView.setLayoutManager(gridLayoutManager);
            binding.rvAllView.setAdapter(mAllViewAdapter);
        } else {
            localViewDataChanged();
        }

        binding.playLocalAudioBt.setEnabled(false);
        binding.stopLocalAudioBt.setEnabled(false);
    }

    private void removeTextureViewByResult(TextureView textureView) {
        ViewGroup viewGroup = (ViewGroup) textureView.getParent();
        if (null != viewGroup && viewGroup.getChildCount() >= 1) {
            viewGroup.removeView(textureView);
        }
        int index = 0;
        Iterator<SurfaceViewInfo> iterator = mAllSurfaceViewList.iterator();
        while (iterator.hasNext()) {
            SurfaceViewInfo surfaceViewInfo = iterator.next();
            if (textureView == surfaceViewInfo.getView()) {
                iterator.remove();
                break;
            }
            index++;
        }
        if (null != mAllViewAdapter) {
            mAllViewAdapter.notifyItemRemoved(index);
        }
    }

    @Override
    protected void registerListener() {
        super.registerListener();
        isEnterScene.addOnPropertyChangedCallback(callback);
    }

    @Override
    protected void unregisterListener() {
        super.unregisterListener();

        isEnterScene.removeOnPropertyChangedCallback(callback);
    }

    @Override
    @SuppressLint("CheckResult")
    protected void initClickEvent() {
        super.initClickEvent();


        RxView.clicks(binding.exitBtn).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isEnterScene.set(false);
            mIsExit = true;
            exit();
            MetaContext.getInstance().resetRoleInfo();
            removeAllLocalTextureView();
        });


        RxView.clicks(binding.zoomAvatarBt).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            updateViewMode();
        });

        RxView.clicks(binding.addViewBt).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            int curUid = mCurFakeUid++;
            String[] avatars = new String[] {
              "mina", "kda", "huamulan", "boy", "girl"
            };
            String extraInfo = "{\"avatar\":\"" + avatars[curUid % avatars.length] + "\"}";
            addLocalTextureView(curUid, extraInfo.getBytes());
        });

        RxView.clicks(binding.removeViewBt).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            if (mAllSurfaceViewList.size() <= 1) {
                return;
            }
            try {
                int delIndex = new Random().nextInt(mAllSurfaceViewList.size() - 1) + 1;
                MetaContext.getInstance().removeSceneView((TextureView) mAllSurfaceViewList.get(delIndex).getView());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        RxView.clicks(binding.playLocalAudioBt).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            if (null != mAudioFileReader) {
                mAudioFileReader.start();
            }
        });

        RxView.clicks(binding.stopLocalAudioBt).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            if (null != mAudioFileReader) {
                mAudioFileReader.stop();
            }
        });

        RxView.clicks(binding.voiceDriverSwitchBt).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            MetaSceneOptions options = new MetaSceneOptions();
            if (mVoiceDriverByChannel) {
                mVoiceDriverByChannel = false;
                //本地音频驱动
                binding.voiceDriverSwitchBt.setText(R.string.voice_driver_channel);
                binding.playLocalAudioBt.setEnabled(true);
                binding.stopLocalAudioBt.setEnabled(true);

                options.mLipSyncMode = MetaSceneOptions.LipSyncMode.LIP_SYNC_MODE_PUSH_AUDIO;

                MetaContext.getInstance().updatePublishCustomAudioTrackChannelOptions(true, MetaConstants.AUDIO_SAMPLE_RATE, MetaConstants.AUDIO_SAMPLE_NUM_OF_CHANNEL, MetaConstants.AUDIO_SAMPLE_NUM_OF_CHANNEL, true, true);

            } else {
                mVoiceDriverByChannel = true;
                //频道音频驱动
                binding.voiceDriverSwitchBt.setText(R.string.voice_driver_local);
                binding.playLocalAudioBt.setEnabled(false);
                binding.stopLocalAudioBt.setEnabled(false);

                options.mLipSyncMode = MetaSceneOptions.LipSyncMode.LIP_SYNC_MODE_NORMAL;

                MetaContext.getInstance().updatePublishCustomAudioTrackChannelOptions(false, MetaConstants.AUDIO_SAMPLE_RATE, MetaConstants.AUDIO_SAMPLE_NUM_OF_CHANNEL, MetaConstants.AUDIO_SAMPLE_NUM_OF_CHANNEL, true, true);
            }

            options.mPublishBlendShape = true;
            options.mAutoSubscribeBlendShape = true;
            MetaContext.getInstance().updateSceneOptions(options);
        });
    }


    @Override
    public void initMainUnityView() {
        super.initMainUnityView();
        addMainAvatarView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        addMainAvatarView();
    }

    private void addMainAvatarView() {
        mAllSurfaceViewList.add(new SurfaceViewInfo(mTextureView, KeyCenter.RTC_UID));
        viewListDataChanged();
    }


    @Override
    public void exit() {
        super.exit();
        if (null != mAudioFileReader) {
            mAudioFileReader.stop();
        }
    }

    @Override
    protected void resetCreateSceneState() {
        super.resetCreateSceneState();
        resetSceneState();
        resetViewVisibility();
    }

    private void resetSceneState() {
        mViewMode = 1;
    }


    private void resetViewVisibility() {
        binding.group.setVisibility(View.GONE);
    }

    @Override
    public void onEnterSceneResult(int errorCode) {
        super.onEnterSceneResult(errorCode);
        runOnUiThread(() -> {
            if (errorCode != 0) {
                Toast.makeText(this, String.format(Locale.getDefault(), "EnterSceneFailed %d", errorCode), Toast.LENGTH_LONG).show();
                return;
            }
            isEnterScene.set(true);

            updateViewMode();
            MetaContext.getInstance().updateVoiceChatRole();

            if (MetaConstants.SCENE_FACE_CAPTURE_CHAT == MetaContext.getInstance().getCurrentScene()) {
                MetaContext.getInstance().enableVideo();
            }
            MetaContext.getInstance().joinChannel();
        });
        resetSceneState();
    }

    @Override
    public void onReleasedScene(int status) {
        super.onReleasedScene(status);
        if (status == 0) {
            Intent intent = new Intent(VoiceChatActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    private void removeAllLocalTextureView() {
        Log.i(TAG, "removeAllLocalTextureView");
        if (mAllSurfaceViewList.size() == 1) {
            MetaContext.getInstance().leaveScene();
            return;
        }
        //0 is local camera view
        for (int i = 1; i < mAllSurfaceViewList.size(); i++) {
            MetaContext.getInstance().removeSceneView((TextureView) mAllSurfaceViewList.get(i).getView());
        }
    }

    @Override
    public void onAddSceneViewResult(TextureView view, int errorCode) {

    }

    @Override
    public void onRemoveSceneViewResult(TextureView view, int errorCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeTextureViewByResult(view);
                //0 is local camera view
                if (mIsExit && mAllSurfaceViewList.size() == 1) {
                    isEnterScene.set(false);
                    binding.rvAllView.setVisibility(View.GONE);
                    MetaContext.getInstance().leaveScene();
                }
            }
        });
    }


    @Override
    public void onRemoteUserStateChanged(String uid, int state, byte[] extraInfo) {
        Log.i(TAG, "onRemoteUserStateChanged uid:" + uid + " state:" + state);
        super.onRemoteUserStateChanged(uid, state, extraInfo);
        if (MetaSceneUserStateType.META_SCENE_USER_STATE_ONLINE == state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        addLocalTextureView(Integer.parseInt(uid), extraInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (MetaSceneUserStateType.META_SCENE_USER_STATE_OFFLINE == state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // int index = 0;
                        Iterator<SurfaceViewInfo> iterator = mAllSurfaceViewList.iterator();
                        while (iterator.hasNext()) {
                            SurfaceViewInfo surfaceViewInfo = iterator.next();
                            if (Integer.parseInt(uid) == surfaceViewInfo.getUid()) {
                                MetaContext.getInstance().removeSceneView((TextureView) surfaceViewInfo.getView());
                                // iterator.remove();
                                break;
                            }
                            // index++;
                        }
//                        if (null != mAllViewAdapter) {
//                            mAllViewAdapter.notifyItemRemoved(index);
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private void viewListDataChanged() {
        if (null != mAllViewAdapter) {
            mAllViewAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void localViewDataChanged() {
        if (null != mAllViewAdapter) {
            mAllViewAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        super.onJoinChannelSuccess(channel, uid, elapsed);
        Log.i(TAG, "onJoinChannelSuccess");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MetaSceneOptions options = new MetaSceneOptions();
                if (MetaContext.getInstance().getCurrentScene() == MetaConstants.SCENE_FACE_CAPTURE_CHAT) {
                    options.mMotionCaptureType = MetaSceneOptions.MotionCaptureType.MOTION_CAPTURE_TYPE_FACE_CAPTURE;
                } else {
                    options.mLipSyncMode = MetaSceneOptions.LipSyncMode.LIP_SYNC_MODE_NORMAL;
                }
                options.mPublishBlendShape = true;
                options.mAutoSubscribeBlendShape = true;
                MetaContext.getInstance().updateSceneOptions(options);

                mAudioFileReader = new AudioFileReader(VoiceChatActivity.this, new AudioFileReader.OnAudioReadListener() {
                    @Override
                    public void onAudioRead(byte[] buffer, long timestamp) {
                        MetaContext.getInstance().pushAudioToLipSync(buffer, timestamp);
                        MetaContext.getInstance().pushExternalAudioFrame(buffer, timestamp);
                    }
                });
            }
        });
    }


    private void addLocalTextureView(int uid, byte[] extraInfo) {
        if (mAllSurfaceViewList.size() - 1 >= MetaConstants.MAX_COUNT_ADD_SCENE_VIEW) {
            Log.e(TAG, "add scene view to reach the maximum number");
            return;
        }

        String strExtraInfo = new String(extraInfo, StandardCharsets.UTF_8);
        JSONObject jsonObject = JSONObject.parseObject(strExtraInfo);
        String avatarName = jsonObject.getString("avatar");


        mAddAvatarTextureView = new TextureView(this);
        mAddAvatarTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "localTextureView onSurfaceTextureAvailable surface=" + surface);
                addLocalAvatarView(mAddAvatarTextureView,
                        mTextureView.getMeasuredWidth(), mTextureView.getMeasuredHeight(),
                        uid,
                        avatarName);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                Log.i(TAG, "localTextureView onSurfaceTextureDestroyed surface：" + surface);
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });
        mAllSurfaceViewList.add(new SurfaceViewInfo(mAddAvatarTextureView, uid));
        if (null != mAllViewAdapter) {
            mAllViewAdapter.notifyItemInserted(mAllSurfaceViewList.size() - 1);
            binding.rvAllView.scrollToPosition(mAllSurfaceViewList.size() - 1);
        }
    }
}
