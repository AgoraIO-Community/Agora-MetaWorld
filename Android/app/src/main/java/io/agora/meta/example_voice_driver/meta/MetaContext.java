package io.agora.meta.example_voice_driver.meta;

import static io.agora.rtc2.video.VideoEncoderConfiguration.STANDARD_BITRATE;

import android.content.Context;
import android.util.Log;
import android.view.TextureView;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.agora.base.VideoFrame;
import io.agora.meta.AvatarModelInfo;
import io.agora.meta.EnterSceneConfig;
import io.agora.meta.ILocalUserAvatar;
import io.agora.meta.IMetaScene;
import io.agora.meta.IMetaSceneEventHandler;
import io.agora.meta.IMetaService;
import io.agora.meta.IMetaServiceEventHandler;
import io.agora.meta.MetaSceneAssetsInfo;
import io.agora.meta.MetaSceneConfig;
import io.agora.meta.MetaServiceConfig;
import io.agora.meta.MetaUserInfo;
import io.agora.meta.MetaUserPositionInfo;
import io.agora.meta.SceneDisplayConfig;
import io.agora.meta.example_voice_driver.R;
import io.agora.meta.example_voice_driver.inf.IMetaEventHandler;
import io.agora.meta.example_voice_driver.inf.IRtcEventCallback;
import io.agora.meta.example_voice_driver.models.FaceParameterItem;
import io.agora.meta.example_voice_driver.models.UnityRoleInfo;
import io.agora.meta.example_voice_driver.models.manifest.DressItemResource;
import io.agora.meta.example_voice_driver.models.EnterSceneExtraInfo;
import io.agora.meta.example_voice_driver.models.RoleInfo;
import io.agora.meta.example_voice_driver.models.UnityMessage;
import io.agora.meta.example_voice_driver.models.manifest.FaceBlendShape;
import io.agora.meta.example_voice_driver.models.manifest.FaceBlendShapeItem;
import io.agora.meta.example_voice_driver.utils.AgoraMediaPlayer;
import io.agora.meta.example_voice_driver.utils.DressAndFaceDataUtils;
import io.agora.meta.example_voice_driver.utils.KeyCenter;
import io.agora.meta.example_voice_driver.utils.MMKVUtils;
import io.agora.meta.example_voice_driver.utils.MetaConstants;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import io.agora.spatialaudio.ILocalSpatialAudioEngine;
import io.agora.spatialaudio.LocalSpatialAudioConfig;
import io.agora.spatialaudio.RemoteVoicePositionInfo;

public class MetaContext implements IMetaEventHandler, AgoraMediaPlayer.OnMediaVideoFramePushListener {

    private final static String TAG = MetaContext.class.getName();
    private volatile static MetaContext instance = null;
    private final static boolean enableSpatialAudio = true;

    private RtcEngine rtcEngine;
    private ILocalSpatialAudioEngine spatialAudioEngine;
    private IMetaService metaService;
    private IMetaScene metaScene;
    private MetaSceneAssetsInfo sceneInfo;
    private AvatarModelInfo modelInfo;
    private MetaUserInfo userInfo;
    private TextureView sceneView;
    private final ConcurrentHashMap<IMetaServiceEventHandler, Integer> metaServiceEventHandlerMap;
    private final ConcurrentHashMap<IMetaSceneEventHandler, Integer> metaSceneEventHandlerMap;
    private boolean mJoinedRtc = false;
    private ILocalUserAvatar localUserAvatar;
    private boolean isInScene;
    private int currentScene;
    private int nextScene;
    private RoleInfo roleInfo;
    private List<RoleInfo> roleInfos;
    private boolean needSaveDressInfo;
    private boolean isInitMeta;

    private IRtcEventCallback iRtcEventCallback;
    private String scenePath;

    private boolean isEnableLocalSceneRes = false;

    private MetaContext() {
        metaServiceEventHandlerMap = new ConcurrentHashMap<>();
        metaSceneEventHandlerMap = new ConcurrentHashMap<>();
        isInScene = false;
        currentScene = MetaConstants.SCENE_NONE;
        nextScene = MetaConstants.SCENE_NONE;
        roleInfo = null;
        needSaveDressInfo = false;
        isInitMeta = false;
    }

    public static MetaContext getInstance() {
        if (instance == null) {
            synchronized (MetaContext.class) {
                if (instance == null) {
                    instance = new MetaContext();
                }
            }
        }
        return instance;
    }

    public void setRtcEventCallback(IRtcEventCallback iRtcEventCallback) {
        this.iRtcEventCallback = iRtcEventCallback;
    }

    public boolean initialize(Context context) {
        int ret = Constants.ERR_OK;
        if (rtcEngine == null) {
            try {
                RtcEngineConfig rtcConfig = new RtcEngineConfig();
                rtcConfig.mContext = context;
                rtcConfig.mAppId = KeyCenter.APP_ID;
                rtcConfig.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
                rtcConfig.mEventHandler = new IRtcEngineEventHandler() {
                    @Override
                    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                        Log.d(TAG, String.format("onJoinChannelSuccess %s %d", channel, uid));
                        mJoinedRtc = true;
                        if (null != iRtcEventCallback) {
                            iRtcEventCallback.onJoinChannelSuccess(channel, uid, elapsed);
                        }
                    }

                    @Override
                    public void onUserOffline(int uid, int reason) {
                        Log.d(TAG, String.format("onUserOffline %d %d ", uid, reason));
                        if (spatialAudioEngine != null) {
                            spatialAudioEngine.removeRemotePosition(uid);
                        }

                        if (null != iRtcEventCallback) {
                            iRtcEventCallback.onUserOffline(uid, reason);
                        }
                    }

                    @Override
                    public void onAudioRouteChanged(int routing) {
                        Log.d(TAG, String.format("onAudioRouteChanged %d", routing));
                    }

                    @Override
                    public void onUserJoined(int uid, int elapsed) {
                        Log.d(TAG, "onUserJoined uid=" + uid);
                        if (null != iRtcEventCallback) {
                            iRtcEventCallback.onUserJoined(uid, elapsed);
                        }
                    }

                    @Override
                    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
                        Log.d(TAG, "onFirstRemoteVideoDecoded uid=" + uid + ",width=" + width + ",heigh=" + height + ",elapsed=" + elapsed);
                        if (null != iRtcEventCallback) {
                            iRtcEventCallback.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
                        }
                    }
                };

                rtcConfig.mAudioScenario = Constants.AudioScenario.getValue(Constants.AudioScenario.DEFAULT);

                rtcEngine = RtcEngine.create(rtcConfig);
                rtcEngine.setParameters("{\"rtc.enable_debug_log\":true}");

                rtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                        new VideoEncoderConfiguration.VideoDimensions(240, 240),
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                        STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE, VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED));


                rtcEngine.enableAudio();
                //rtcEngine.enableVideo();

                rtcEngine.setAudioProfile(
                        Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_GAME_STREAMING
                );
                rtcEngine.setDefaultAudioRoutetoSpeakerphone(true);

                scenePath = context.getExternalFilesDir("").getPath();
                {
                    metaService = IMetaService.create();
                    MetaServiceConfig config = new MetaServiceConfig() {{
                        mRtcEngine = rtcEngine;
                        mAppId = KeyCenter.APP_ID;
                        mRtmToken = KeyCenter.RTM_TOKEN;
                        mLocalDownloadPath = scenePath;
                        mUserId = KeyCenter.RTM_UID;
                        mEventHandler = MetaContext.this;
                    }};
                    ret += metaService.initialize(config);
                    Log.i(TAG, "launcher version=" + metaService.getLauncherVersion(context));
                }

                AgoraMediaPlayer.getInstance().initMediaPlayer(rtcEngine);
                AgoraMediaPlayer.getInstance().setOnMediaVideoFramePushListener(this);

                isInitMeta = true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return ret == Constants.ERR_OK;
    }

    public void joinChannel() {
        if (null != rtcEngine) {
            rtcEngine.joinChannel(
                    KeyCenter.RTC_TOKEN, KeyCenter.CHANNEL_ID, KeyCenter.RTC_UID,
                    new ChannelMediaOptions() {{
                        publishMicrophoneTrack = true;
                        publishCameraTrack = true;
                        autoSubscribeAudio = true;
                        autoSubscribeVideo = true;
                        clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                    }});
        }
    }

    public void destroy() {
        IMetaService.destroy();
        metaService = null;
        RtcEngine.destroy();
        rtcEngine = null;
        isInitMeta = false;
    }

    public void registerMetaSceneEventHandler(IMetaSceneEventHandler eventHandler) {
        metaSceneEventHandlerMap.put(eventHandler, 0);
    }

    public void unregisterMetaSceneEventHandler(IMetaSceneEventHandler eventHandler) {
        metaSceneEventHandlerMap.remove(eventHandler);
    }

    public void registerMetaServiceEventHandler(IMetaServiceEventHandler eventHandler) {
        metaServiceEventHandlerMap.put(eventHandler, 0);
    }

    public void unregisterMetaServiceEventHandler(IMetaServiceEventHandler eventHandler) {
        metaServiceEventHandlerMap.remove(eventHandler);
    }

    public boolean getSceneInfos() {
        return metaService.getSceneAssetsInfo() == Constants.ERR_OK;
    }

    public boolean isSceneDownloaded(MetaSceneAssetsInfo sceneInfo) {
        return metaService.isSceneAssetsDownloaded(sceneInfo.mSceneId) > 0;
    }

    public boolean downloadScene(MetaSceneAssetsInfo sceneInfo) {
        return metaService.downloadSceneAssets(sceneInfo.mSceneId) == Constants.ERR_OK;
    }

    public boolean cancelDownloadScene(MetaSceneAssetsInfo sceneInfo) {
        return metaService.cancelDownloadSceneAssets(sceneInfo.mSceneId) == Constants.ERR_OK;
    }

    public void prepareScene(MetaSceneAssetsInfo sceneInfo, AvatarModelInfo modelInfo, MetaUserInfo userInfo) {
        this.sceneInfo = sceneInfo;
        this.modelInfo = modelInfo;
        this.userInfo = userInfo;
    }

    public boolean createScene(Context activityContext, TextureView tv) {
        Log.d(TAG, String.format("createAndEnterScene"));
        this.sceneView = tv;

        if (spatialAudioEngine == null && enableSpatialAudio) {
            spatialAudioEngine = ILocalSpatialAudioEngine.create();
            LocalSpatialAudioConfig config = new LocalSpatialAudioConfig() {{
                mRtcEngine = rtcEngine;
            }};
            spatialAudioEngine.initialize(config);
            spatialAudioEngine.muteLocalAudioStream(false);
            spatialAudioEngine.muteAllRemoteAudioStreams(false);
        }

        MetaSceneConfig sceneConfig = new MetaSceneConfig();
        sceneConfig.mActivityContext = activityContext;
        if (MetaConstants.SCENE_DRESS == currentScene) {
            sceneConfig.mEnableVoiceDriveAvatar = true;
            sceneConfig.mEnableFaceCapture = false;
        } else if (MetaConstants.SCENE_GAME == currentScene) {
            sceneConfig.mEnableVoiceDriveAvatar = false;
            sceneConfig.mEnableFaceCapture = true;
        }
        sceneConfig.mFaceCaptureAppId = KeyCenter.FACE_CAP_APP_ID;
        sceneConfig.mFaceCaptureCertificate = KeyCenter.FACE_CAP_APP_KEY;
        int ret = -1;
        if (metaScene == null) {
            ret = metaService.createScene(sceneConfig);
        }
        mJoinedRtc = false;
        return ret == Constants.ERR_OK;
    }

    public void enterScene() {
        checkRoleInfoRes();

        if (null != localUserAvatar) {
            localUserAvatar.setUserInfo(userInfo);
            //该model的mBundleType为MetaBundleInfo.BundleType.BUNDLE_TYPE_AVATAR类型
            localUserAvatar.setModelInfo(modelInfo);
            if (null != roleInfo) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("2dbg", "");
                jsonObject.put("avatar", roleInfo.getAvatarType());
                jsonObject.put("dress", roleInfo.getDressResourceMap().values().toArray((new Integer[0])));
                jsonObject.put("face", roleInfo.getFaceParameterResourceMap().values().toArray((new FaceParameterItem[0])));
                localUserAvatar.setExtraInfo(jsonObject.toJSONString().getBytes());
            }
        }
        if (null != metaScene) {
            //使能位置信息回调功能
            //metaScene.enableUserPositionNotification(MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene());
            //设置回调接口
            metaScene.addEventHandler(MetaContext.getInstance());
            EnterSceneConfig config = new EnterSceneConfig();
            //sceneView必须为Texture类型，为渲染unity显示的view
            config.mSceneView = this.sceneView;
            //rtc加入channel的ID
            config.mRoomName = KeyCenter.CHANNEL_ID;
            //内容中心对应的ID
            if (null != sceneInfo) {
                config.mSceneId = this.sceneInfo.mSceneId;
            }
            if (isEnableLocalSceneRes) {
                config.mSceneId = 0;
                config.mScenePath = scenePath + "/" + getSceneId();
            }
            /*
             *仅为示例格式，具体格式以项目实际为准
             *   "extraCustomInfo":{
             *     "sceneIndex":0  //0为默认场景，在这里指咖啡厅，1为换装设置场景
             *   }
             */
            EnterSceneExtraInfo extraInfo = new EnterSceneExtraInfo();
            if (MetaConstants.SCENE_DRESS == MetaContext.getInstance().getCurrentScene()) {
                extraInfo.setSceneIndex(MetaConstants.SCENE_DRESS);
            } else if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
                extraInfo.setSceneIndex(MetaConstants.SCENE_GAME);
            }
            //加载的场景index
            config.mExtraInfo = JSONObject.toJSONString(extraInfo).getBytes();
            metaScene.enterScene(config);
        }
    }

    @Override
    public void onCreateSceneResult(IMetaScene scene, int errorCode) {
        Log.i(TAG, "onCreateSceneResult errorCode: " + errorCode);
        metaScene = scene;
        localUserAvatar = metaScene.getLocalUserAvatar();
        for (IMetaServiceEventHandler handler : metaServiceEventHandlerMap.keySet()) {
            handler.onCreateSceneResult(scene, errorCode);
        }
    }

    public boolean updateRole(int role) {
        int ret = Constants.ERR_OK;
        //是否为broadcaster
        boolean isBroadcaster = role == Constants.CLIENT_ROLE_BROADCASTER;
        ret += rtcEngine.updateChannelMediaOptions(new ChannelMediaOptions() {{
            publishMicrophoneTrack = isBroadcaster;
            clientRoleType = role;
        }});
        modelInfo.mLocalVisible = true;

        if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
            modelInfo.mRemoteVisible = isBroadcaster;
            modelInfo.mSyncPosition = isBroadcaster;
        } else if (MetaConstants.SCENE_DRESS == MetaContext.getInstance().getCurrentScene()) {
            modelInfo.mRemoteVisible = false;
            modelInfo.mSyncPosition = false;
        }

        if (null != localUserAvatar && Constants.ERR_OK == localUserAvatar.setModelInfo(modelInfo)) {
            ret += localUserAvatar.applyInfo();
        }
        return ret == Constants.ERR_OK;
    }

    public boolean enableLocalAudio(boolean enabled) {
        return rtcEngine.enableLocalAudio(enabled) == Constants.ERR_OK;
    }

    public boolean muteAllRemoteAudioStreams(boolean mute) {
        if (spatialAudioEngine != null) {
            return spatialAudioEngine.muteAllRemoteAudioStreams(mute) == Constants.ERR_OK;
        }
        return rtcEngine.muteAllRemoteAudioStreams(mute) == Constants.ERR_OK;
    }

    public boolean leaveScene() {
        Log.d(TAG, "leaveScene");
        int ret = Constants.ERR_OK;
        if (metaScene != null) {
            ret += rtcEngine.leaveChannel();
            ret += metaScene.leaveScene();
        }
        if (spatialAudioEngine != null) {
            ILocalSpatialAudioEngine.destroy();
            spatialAudioEngine = null;
        }
        Log.d(TAG, "leaveScene success");
        return ret == Constants.ERR_OK;
    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {
        Log.d(TAG, "onConnectionStateChanged state=" + state + ",reason=" + reason);
        if (state == ConnectionState.META_CONNECTION_STATE_ABORTED) {
            setCurrentScene(MetaConstants.SCENE_NONE);
            resetRoleInfo();
            leaveScene();
        }

        for (IMetaServiceEventHandler handler : metaServiceEventHandlerMap.keySet()) {
            handler.onConnectionStateChanged(state, reason);
        }
    }

    @Override
    public void onTokenWillExpire() {
        for (IMetaServiceEventHandler handler : metaServiceEventHandlerMap.keySet()) {
            handler.onTokenWillExpire();
        }
    }

    @Override
    public void onGetSceneAssetsInfoResult(MetaSceneAssetsInfo[] metaSceneAssetsInfos, int errorCode) {
        for (IMetaServiceEventHandler handler : metaServiceEventHandlerMap.keySet()) {
            handler.onGetSceneAssetsInfoResult(metaSceneAssetsInfos, errorCode);
        }
    }

    @Override
    public void onDownloadSceneAssetsProgress(long sceneId, int progress, int state) {
        for (IMetaServiceEventHandler handler : metaServiceEventHandlerMap.keySet()) {
            handler.onDownloadSceneAssetsProgress(sceneId, progress, state);
        }
    }

    @Override
    public void onEnterSceneResult(int errorCode) {
        Log.d(TAG, String.format("onEnterSceneResult %d", errorCode));
        if (errorCode == 0) {
            isInScene = true;

            if (needSaveDressInfo) {
                needSaveDressInfo = false;
                MMKVUtils.getInstance().putValue(MetaConstants.MMKV_ROLE_INFO, JSONArray.toJSONString(roleInfos));
            }

            if (null != metaScene) {
                metaScene.setSceneParameters("{\"debugUnity\":true}");
            }

            if (spatialAudioEngine != null) {
                // audio的mute状态交给ILocalSpatialAudioEngine统一管理
                rtcEngine.muteAllRemoteAudioStreams(true);
            }
            if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
                pushVideoFrameToDisplay();
            }
        }
        for (IMetaSceneEventHandler handler : metaSceneEventHandlerMap.keySet()) {
            handler.onEnterSceneResult(errorCode);
        }
    }

    // Just for test
    private void pushVideoFrameToDisplay() {
        metaScene.enableVideoDisplay("1", true);
        AgoraMediaPlayer.getInstance().play(MetaConstants.VIDEO_URL, 0);
    }

    @Override
    public void onLeaveSceneResult(int errorCode) {
        Log.d(TAG, String.format("onLeaveSceneResult %d", errorCode));
        isInScene = false;
        AgoraMediaPlayer.getInstance().stop();
        if (errorCode == 0) {
            metaScene.release();
            metaScene = null;
        }

        for (IMetaSceneEventHandler handler : metaSceneEventHandlerMap.keySet()) {
            handler.onLeaveSceneResult(errorCode);
        }
    }

    @Override
    public void onReleasedScene(int status) {
        Log.d(TAG, String.format("[meta] onReleasedScene %d", status));
        for (IMetaSceneEventHandler handler : metaSceneEventHandlerMap.keySet()) {
            handler.onReleasedScene(status);
        }
    }

    @Override
    public void onSceneVideoFrameCaptured(TextureView view, VideoFrame videoFrame) {
        Log.d(TAG, "onSceneVideoFrameCaptured");
        for (IMetaSceneEventHandler handler : metaSceneEventHandlerMap.keySet()) {
            handler.onSceneVideoFrameCaptured(view, videoFrame);
        }
    }

    @Override
    public void onSceneMessageReceived(byte[] message) {
        Log.d(TAG, String.format("onSceneMessageReceived %s", new String(message)));
        for (IMetaSceneEventHandler handler : metaSceneEventHandlerMap.keySet()) {
            handler.onSceneMessageReceived(message);
        }
    }

    @Override
    public void onAddSceneViewResult(TextureView view, int errorCode) {
        Log.d(TAG, String.format("onAddSceneViewResult %d", errorCode));
        for (IMetaSceneEventHandler handler : metaSceneEventHandlerMap.keySet()) {
            handler.onAddSceneViewResult(view, errorCode);
        }
    }

    @Override
    public void onRemoveSceneViewResult(TextureView view, int errorCode) {
        Log.d(TAG, String.format("onAddSceneViewResult %d", errorCode));
        for (IMetaSceneEventHandler handler : metaSceneEventHandlerMap.keySet()) {
            handler.onRemoveSceneViewResult(view, errorCode);
        }
    }

    @Override
    public void onUserPositionChanged(String uid, MetaUserPositionInfo posInfo) {
        Log.d(TAG, String.format("onUserPositionChanged %s %s %s %s %s", uid,
                Arrays.toString(posInfo.mPosition),
                Arrays.toString(posInfo.mForward),
                Arrays.toString(posInfo.mRight),
                Arrays.toString(posInfo.mUp)
        ));

        if (spatialAudioEngine != null) {
            try {
                int userId = Integer.parseInt(uid);
                if (KeyCenter.RTC_UID == userId) {
                    spatialAudioEngine.updateSelfPosition(
                            posInfo.mPosition, posInfo.mForward, posInfo.mRight, posInfo.mUp
                    );
                } else if (mJoinedRtc) {
                    spatialAudioEngine.updateRemotePosition(userId, new RemoteVoicePositionInfo() {{
                        position = posInfo.mPosition;
                        forward = posInfo.mForward;
                    }});
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        for (IMetaSceneEventHandler handler : metaSceneEventHandlerMap.keySet()) {
            handler.onUserPositionChanged(uid, posInfo);
        }
    }

    public MetaSceneAssetsInfo getSceneInfo() {
        return sceneInfo;
    }

    @Override
    public void onMediaVideoFramePushed(VideoFrame frame) {
        Log.i(TAG, "onMediaVideoFramePushed");
        if (null != metaScene) {
            metaScene.pushVideoFrameToDisplay("1", frame);
        }
    }

    public void pauseMedia() {
        AgoraMediaPlayer.getInstance().pause();
    }

    public void resumeMedia() {
        AgoraMediaPlayer.getInstance().resume();
    }

    public boolean isInScene() {
        return isInScene;
    }

    public void initRoleInfo(Context context, String name, int gender) {
        roleInfo = null;
        initRoleInfoFromDb(name, gender);

        if (null == roleInfo) {
            currentScene = MetaConstants.SCENE_DRESS;

            if (null == roleInfos) {
                roleInfos = new ArrayList<>(1);
            }

            roleInfo = new RoleInfo();
            roleInfos.add(roleInfo);

            needSaveDressInfo = true;
        } else {
            currentScene = MetaConstants.SCENE_GAME;
            needSaveDressInfo = false;
        }
        roleInfo.setName(name);
        roleInfo.setGender(gender);
        roleInfo.setAvatarType(context.getResources().getStringArray(R.array.avatar_model_value)[gender]);

        //for test dress scene
        currentScene = MetaConstants.SCENE_DRESS;
    }

    private void checkRoleInfoRes() {
        DressItemResource[] dressResources = DressAndFaceDataUtils.getInstance().getDressResources(roleInfo.getAvatarType());
        if (null != dressResources) {
            for (DressItemResource resource : dressResources) {
                if (!roleInfo.getDressResourceMap().containsKey(resource.getId())) {
                    roleInfo.updateDressResource(resource.getId(), resource.getAssets()[0]);
                }
            }
        }

        FaceBlendShape[] faceBlendShapes = DressAndFaceDataUtils.getInstance().getFaceBlendShapes(roleInfo.getAvatarType());
        if (null != faceBlendShapes) {
            for (FaceBlendShape faceBlendShape : faceBlendShapes) {
                FaceBlendShapeItem[] shapes = faceBlendShape.getShapes();
                if (null != shapes) {
                    for (FaceBlendShapeItem faceBlendShapeItem : shapes) {
                        if (!roleInfo.getFaceParameterResourceMap().containsKey(faceBlendShapeItem.getKey())) {
                            roleInfo.updateFaceParameter(faceBlendShapeItem.getKey(), 50);
                        }
                    }
                }
            }
        }
    }

    public RoleInfo getRoleInfo() {
        return roleInfo;
    }

    public int getCurrentScene() {
        return currentScene;
    }

    public int getNextScene() {
        return nextScene;
    }

    public void setCurrentScene(int currentScene) {
        this.currentScene = currentScene;
    }

    public void setNextScene(int nextScene) {
        this.nextScene = nextScene;
    }

    public void sendRoleDressInfo(int[] resIdArray) {
        //注意该协议格式需要和unity协商一致
        UnityMessage message = new UnityMessage();
        message.setKey(MetaConstants.KEY_UNITY_MESSAGE_UPDATE_DRESS);
        JSONObject valueJson = new JSONObject();
        valueJson.put("id", resIdArray);
        message.setValue(valueJson.toJSONString());
        sendSceneMessage(JSONObject.toJSONString(message));
    }


    public void sendRoleFaceInfo(FaceParameterItem[] faceParameterItems) {
        //注意该协议格式需要和unity协商一致
        UnityMessage message = new UnityMessage();
        message.setKey(MetaConstants.KEY_UNITY_MESSAGE_UPDATE_FACE);
        JSONObject valueJson = new JSONObject();
        valueJson.put("value", faceParameterItems);
        message.setValue(valueJson.toJSONString());
        sendSceneMessage(JSONObject.toJSONString(message));
    }

    public void saveRoleDressInfo(String name, int gender) {
        Iterator<RoleInfo> it = roleInfos.iterator();
        RoleInfo tempRoleInfo;
        while (it.hasNext()) {
            tempRoleInfo = it.next();
            //just only gender
            if (/*tempRoleInfo.getName().equalsIgnoreCase(name) && */tempRoleInfo.getGender() == gender) {
                it.remove();
            }
        }
        roleInfos.add(0, roleInfo);
        MMKVUtils.getInstance().putValue(MetaConstants.MMKV_ROLE_INFO, JSONArray.toJSONString(roleInfos));
    }

    public void cancelRoleDressInfo(String name, int gender) {
        initRoleInfoFromDb(name, gender);
    }

    private void initRoleInfoFromDb(String name, int gender) {
        roleInfos = JSONArray.parseArray(MMKVUtils.getInstance().getValue(MetaConstants.MMKV_ROLE_INFO, ""), RoleInfo.class);

        if (roleInfos != null && roleInfos.size() != 0) {
            for (int i = 0; i < roleInfos.size(); i++) {
                if (null != roleInfos.get(i)) {
                    //only just gender
                    if (/*(null != roleInfos.get(i).getName() && roleInfos.get(i).getName().equalsIgnoreCase(name)) &&*/ roleInfos.get(i).getGender() == gender) {
                        roleInfo = roleInfos.get(i);
                        break;
                    }
                }
            }
        }
    }

    public void sendSceneMessage(String msg) {
        if (metaScene == null) {
            Log.e(TAG, "sendMessageToScene metaScene is null");
            return;
        }

        if (metaScene.sendSceneMessage(msg.getBytes()) == 0) {
            Log.i(TAG, "send " + msg + " successful");
        } else {
            Log.e(TAG, "send " + msg + " fail");
        }
    }

    public void resetRoleInfo() {
        roleInfos = null;
        roleInfo = null;
    }

    public boolean isInitMeta() {
        return isInitMeta;
    }

    public int getSceneId() {
        return MetaConstants.SCENE_ID_META_1_1_VOICE_DRIVER;
    }

    public RtcEngine getRtcEngine() {
        return rtcEngine;
    }

    public void addSceneView(TextureView view, SceneDisplayConfig config) {
        if (null != metaScene) {
            Log.i(TAG, "addRenderView view::" + view + ",config:" + config);
            metaScene.addSceneView(view, config);
        }
    }

    public void enableSceneVideo(TextureView view, boolean enable) {
        if (null != metaScene && null != rtcEngine) {
            rtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                    new VideoEncoderConfiguration.VideoDimensions(view.getWidth(), view.getHeight()),
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                    STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT, VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED));
            metaScene.enableSceneVideoCapture(view, enable);
        }
    }

    public void removeSceneView(TextureView view) {
        if (null != metaScene) {
            metaScene.removeSceneView(view);
        }
    }

    public boolean pushExternalVideoFrame(VideoFrame videoFrame) {
        if (null == rtcEngine) {
            return false;
        }

        return rtcEngine.pushExternalVideoFrame(videoFrame);
    }

    public String getScenePath() {
        return scenePath;
    }

    public boolean isEnableLocalSceneRes() {
        return isEnableLocalSceneRes;
    }

    public UnityRoleInfo getUnityRoleInfo() {
        UnityRoleInfo unityRoleInfo = new UnityRoleInfo();
        unityRoleInfo.setGender(roleInfo.getGender());
        return unityRoleInfo;
    }

    public void enableVoiceDriveAvatar(boolean enable) {
        if (null != metaScene) {
            metaScene.enableVoiceDriveAvatar(enable);
        }
    }

    public void pushAudioToDriveAvatar(byte[] data, long timestamp) {
        if (null != metaScene) {
            metaScene.pushAudioToDriveAvatar(data, timestamp, MetaConstants.AUDIO_SAMPLE_RATE, MetaConstants.AUDIO_SAMPLE_NUM_OF_CHANNEL);
        }
    }

    public int pushExternalAudioFrame(byte[] data, long timestamp) {
        if (null != rtcEngine) {
            return rtcEngine.pushExternalAudioFrame(data, timestamp);
        }
        return -1;
    }

    public void updatePublishCustomAudioTrackChannelOptions(boolean enable, int sampleRate, int channels, int sourceNumber, boolean localPlayback, boolean publish) {
        if (null != rtcEngine) {
            ChannelMediaOptions option = new ChannelMediaOptions();
            option.publishCustomAudioTrack = enable;
            rtcEngine.updateChannelMediaOptions(option);

            rtcEngine.setExternalAudioSource(enable, sampleRate, channels, sourceNumber, localPlayback, publish);

        }
    }
}
