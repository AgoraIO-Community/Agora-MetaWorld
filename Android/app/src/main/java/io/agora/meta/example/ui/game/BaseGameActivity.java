package io.agora.meta.example.ui.game;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;

import io.agora.meta.IMetaScene;
import io.agora.meta.MetaSceneOptions;
import io.agora.meta.SceneDisplayConfig;
import io.agora.meta.example.inf.IMetaEventHandler;
import io.agora.meta.example.inf.IRtcEventCallback;
import io.agora.meta.example.meta.MetaContext;
import io.agora.meta.example.models.UnityMessage;
import io.agora.meta.example.utils.MetaConstants;
import io.agora.rtc2.IRtcEngineEventHandler;

public class BaseGameActivity extends Activity implements IMetaEventHandler, IRtcEventCallback {
    private static final String TAG = BaseGameActivity.class.getSimpleName();
    protected TextureView mTextureView = null;
    protected boolean mReCreateScene;
    protected boolean mIsFront;
    protected boolean mMainViewAvailable = false;
    protected boolean mMainViewResized = false;

    protected boolean mJoinChannelSuccess;

    protected int mViewMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: " + this.getLocalClassName());
        mReCreateScene = true;
        super.onCreate(savedInstanceState);
        initLayout();
        registerListener();
        initData();
        initMainUnityView();
        initView();
        initClickEvent();
    }

    protected void initLayout() {
        Log.i(TAG, "initLayout: ");
    }

    protected void initView() {
        Log.i(TAG, "initView: ");
    }

    protected void initData() {
        Log.i(TAG, "initData: ");
        mViewMode = 0;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent: " + intent.getDataString());
        super.onNewIntent(intent);
        mReCreateScene = true;
        initData();
        initView();
        registerListener();
        maybeCreateScene();
    }

    protected void registerListener() {
        Log.i(TAG, "registerListener: ");
        MetaContext.getInstance().registerMetaSceneEventHandler(this);
        MetaContext.getInstance().registerMetaServiceEventHandler(this);
        MetaContext.getInstance().setRtcEventCallback(this);
    }

    protected void unregisterListener() {
        Log.i(TAG, "unregisterListener: ");
        MetaContext.getInstance().unregisterMetaSceneEventHandler(this);
        MetaContext.getInstance().unregisterMetaServiceEventHandler(this);
        MetaContext.getInstance().setRtcEventCallback(null);
    }

    protected void initClickEvent() {
        Log.i(TAG, "initClickEvent: ");
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume: " + this.getLocalClassName());
        super.onResume();
        mIsFront = true;
        maybeCreateScene();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause: " + this.getLocalClassName());
        super.onPause();
        mIsFront = false;
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop: " + this.getLocalClassName());
        super.onStop();
        unregisterListener();
    }

    protected void initMainUnityView() {
        Log.i(TAG, "initMainUnityView: ");
        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int w, int h) {
                Log.i(TAG, "onSurfaceTextureAvailable, width=" + w + ", height=" + h);
                mMainViewAvailable = true;
                maybeCreateScene();
            }


            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int w, int h) {
                Log.i(TAG, "onSurfaceTextureSizeChanged, width=" + w + ", height=" + h);
                mMainViewResized = true;
                maybeCreateScene();
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });
    }

    protected void maybeCreateScene() {
        Log.i(TAG, "maybeCreateScene,mReCreateScene=" + mReCreateScene + ",mIsFront=" + mIsFront + ",mMainViewAvailable=" + mMainViewAvailable + ",mMainViewResized=" + mMainViewResized + ",mJoinChannelSuccess=" + mJoinChannelSuccess);
        if (mReCreateScene && mIsFront && mMainViewAvailable/* && mMainViewResized*/) {
            resetCreateSceneState();
            MetaContext.getInstance().createScene(this, mTextureView);
        }
    }

    protected void resetCreateSceneState() {
        Log.i(TAG, "resetCreateSceneState: ");
        mReCreateScene = false;
    }

    protected void addLocalAvatarView(TextureView textureView, int width, int height, int uid, String avatarName) {
        Log.i(TAG, "addLocalAvatarView: uid: " + uid + ", avatar: " + avatarName);
        SceneDisplayConfig sceneDisplayConfig = new SceneDisplayConfig();
        sceneDisplayConfig.mWidth = width;
        sceneDisplayConfig.mHeight = height;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sceneIndex", 0);
        jsonObject.put("userId", String.valueOf(uid));
        if (!TextUtils.isEmpty(avatarName)) {
            jsonObject.put("avatar", avatarName);
        }
        sceneDisplayConfig.mExtraInfo = jsonObject.toJSONString().getBytes();
        MetaContext.getInstance().addSceneView(textureView, sceneDisplayConfig);
    }

    protected void exit() {
        Log.i(TAG, "exit: ");
    }

    @Override
    public void onReleasedScene(int status) {
        Log.i(TAG, "onReleasedScene: " + this.getLocalClassName());
        if (status == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MetaContext.getInstance().destroy();
                }
            });
        }
    }

    @Override
    public void onCreateSceneResult(IMetaScene scene, int errorCode) {
        Log.i(TAG, "onCreateSceneResult: ");
        //异步线程回调需在主线程处理
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MetaContext.getInstance().enterScene();
            }
        });
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (MetaConstants.SCENE_DRESS == MetaContext.getInstance().getCurrentScene()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (MetaConstants.SCENE_COFFEE == MetaContext.getInstance().getCurrentScene()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        super.setRequestedOrientation(requestedOrientation);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.i(TAG, "onJoinChannelSuccess");
        mJoinChannelSuccess = true;
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        Log.i(TAG, "onLeaveChannel: ");
        mJoinChannelSuccess = false;
    }

    protected void updateViewMode() {
        Log.i(TAG, "updateViewMode: " + mViewMode);
        UnityMessage message = new UnityMessage();
        message.setKey("setCamera");
        JSONObject valueJson = new JSONObject();
        valueJson.put("viewMode", ++mViewMode % 3);
        message.setValue(valueJson.toJSONString());
        MetaContext.getInstance().sendSceneMessage(JSONObject.toJSONString(message));
    }
}
