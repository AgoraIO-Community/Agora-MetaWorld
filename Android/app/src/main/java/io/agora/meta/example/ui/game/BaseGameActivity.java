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

    protected boolean mJoinChannelSuccess;

    protected int mViewMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        registerListener();
        initData();
        initMainUnityView();
        initView();
        initClickEvent();
    }

    protected void initLayout() {

    }

    protected void initView() {

    }

    protected void initData() {
        mViewMode = 0;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mReCreateScene = true;
        initData();
        initView();
        registerListener();
        maybeCreateScene();
    }

    protected void registerListener() {
        MetaContext.getInstance().registerMetaSceneEventHandler(this);
        MetaContext.getInstance().registerMetaServiceEventHandler(this);
        MetaContext.getInstance().setRtcEventCallback(this);
    }

    protected void unregisterListener() {
        MetaContext.getInstance().unregisterMetaSceneEventHandler(this);
        MetaContext.getInstance().unregisterMetaServiceEventHandler(this);
        MetaContext.getInstance().setRtcEventCallback(null);
    }

    protected void initClickEvent() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsFront = true;
        maybeCreateScene();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsFront = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterListener();
    }

    protected void initMainUnityView() {
        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                mReCreateScene = true;
                maybeCreateScene();
            }


            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                Log.i(TAG, "onSurfaceTextureSizeChanged");
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
        Log.i(TAG, "maybeCreateScene,mReCreateScene=" + mReCreateScene + ",mIsFront=" + mIsFront + ",mJoinChannelSuccess=" + mJoinChannelSuccess);
        if (mReCreateScene && mIsFront) {
            resetCreateSceneState();
            MetaContext.getInstance().createScene(this, mTextureView);
        }
    }

    protected void resetCreateSceneState() {

    }

    protected void addLocalAvatarView(TextureView textureView, int width, int height, int uid, String avatarName) {
        SceneDisplayConfig sceneDisplayConfig = new SceneDisplayConfig();
        sceneDisplayConfig.width = width;
        sceneDisplayConfig.height = height;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", String.valueOf(uid));
        if (!TextUtils.isEmpty(avatarName)) {
            jsonObject.put("avatarName", avatarName);
        }
        sceneDisplayConfig.extraInfo = jsonObject.toJSONString().getBytes();
        MetaContext.getInstance().addSceneView(textureView, sceneDisplayConfig);
    }

    protected void exit() {

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
        }
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
        mJoinChannelSuccess = false;
    }

    protected void updateViewMode() {
        UnityMessage message = new UnityMessage();
        message.setKey("setCamera");
        JSONObject valueJson = new JSONObject();
        valueJson.put("viewMode", ++mViewMode % 3);
        message.setValue(valueJson.toJSONString());
        MetaContext.getInstance().sendSceneMessage(JSONObject.toJSONString(message));
    }
}
