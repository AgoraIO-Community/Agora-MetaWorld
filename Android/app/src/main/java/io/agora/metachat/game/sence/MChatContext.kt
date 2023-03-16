package io.agora.metachat.game.sence

import android.content.Context
import android.view.TextureView
import io.agora.base.VideoFrame
import io.agora.metachat.*
import io.agora.metachat.game.internal.MChatBaseEventHandler
import io.agora.metachat.game.internal.MChatBaseSceneEventHandler
import io.agora.metachat.game.internal.MChatBaseVideoFrameObserver
import io.agora.metachat.game.sence.npc.MChatLocalSourceMediaPlayer
import io.agora.metachat.game.sence.npc.MChatNpcManager
import io.agora.metachat.game.sence.npc.NpcListener
import io.agora.metachat.global.*
import io.agora.metachat.tools.LogTools
import io.agora.rtc2.*
import io.agora.spatialaudio.ILocalSpatialAudioEngine
import io.agora.spatialaudio.LocalSpatialAudioConfig

/**
 * @author create by zhangwei03
 */
class MChatContext private constructor() {

    companion object {
        private const val TAG = "MChatContext"

        private val chatContext by lazy {
            MChatContext()
        }

        @JvmStatic
        fun instance(): MChatContext = chatContext
    }

    private var rtcEngine: RtcEngine? = null
    private var metaChatService: IMetachatService? = null
    private var metaChatScene: IMetachatScene? = null
    private var sceneInfo: MetachatSceneInfo? = null
    private var modelInfo: AvatarModelInfo? = null
    private var userInfo: MetachatUserInfo? = null
    private var rtcRoomId: String = ""
    private var sceneTextureView: TextureView? = null
    private val mchatEventHandlerSet = mutableSetOf<IMetachatEventHandler>()
    private val mchatSceneEventHandlerSet = mutableSetOf<IMetachatSceneEventHandler>()
    private var localUserAvatar: ILocalUserAvatar? = null
    private var isInScene = false
    private var myStreamId: Int = -1

    // unity 交互协议
    private var unityCmd: MChatUnityCmd? = null

    // 电视播放器
    private var chatMediaPlayer: MChatAgoraMediaPlayer? = null

    // 空间音频
    private var chatSpatialAudio: MChatSpatialAudio? = null

    // npc 管理器
    private var npcManager: MChatNpcManager? = null

    fun getLocalUserAvatar(): ILocalUserAvatar? = localUserAvatar

    fun getUnityCmd(): MChatUnityCmd? = unityCmd

    fun rtcEngine(): RtcEngine? = rtcEngine

    fun chatMediaPlayer(): MChatAgoraMediaPlayer? = chatMediaPlayer

    fun chatSpatialAudio(): MChatSpatialAudio? = chatSpatialAudio

    fun chatNpcManager(): MChatNpcManager? = npcManager

    private val mChatEventHandler = object : MChatBaseEventHandler() {
        override fun onCreateSceneResult(scene: IMetachatScene?, errorCode: Int) {
            LogTools.d(TAG, "onCreateSceneResult errorCode:$errorCode")
            metaChatScene = scene
            metaChatScene?.let {
                unityCmd = MChatUnityCmd(it)
                unityCmd?.changeLanguage()
            }
            localUserAvatar = metaChatScene?.localUserAvatar
            mchatEventHandlerSet.forEach {
                it.onCreateSceneResult(scene, errorCode)
            }
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            LogTools.d(TAG, "onConnectionStateChanged state:$state reason:$reason")
            mchatEventHandlerSet.forEach {
                it.onConnectionStateChanged(state, reason)
            }
        }

        override fun onRequestToken() {
            LogTools.d(TAG, "onRequestToken")
            mchatEventHandlerSet.forEach {
                it.onRequestToken()
            }
        }

        override fun onGetSceneInfosResult(scenes: Array<out MetachatSceneInfo>, errorCode: Int) {
            LogTools.d(TAG, "onGetSceneInfosResult errorCode:$errorCode")
            mchatEventHandlerSet.forEach {
                it.onGetSceneInfosResult(scenes, errorCode)
            }
        }

        override fun onDownloadSceneProgress(sceneId: Long, progress: Int, state: Int) {
            mchatEventHandlerSet.forEach {
                it.onDownloadSceneProgress(sceneId, progress, state)
            }
        }

    }

    private val mChatSceneEventHandler = object : MChatBaseSceneEventHandler() {
        override fun onEnterSceneResult(errorCode: Int) {
            LogTools.d(TAG, "onEnterSceneResult errorCode:$errorCode")
            if (errorCode == 0) {
                isInScene = true
                rtcEngine?.joinChannel(
                    MChatKeyCenter.getRtcToken(rtcRoomId), rtcRoomId, MChatKeyCenter.curUid,
                    ChannelMediaOptions().apply {
                        autoSubscribeAudio = true
                        autoSubscribeVideo = true
                        clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                    })
                // audio的mute状态交给ILocalSpatialAudioEngine统一管理
                chatSpatialAudio()?.muteAllRemoteAudioStreams(true)
                metaChatScene?.enableVideoDisplay(MChatConstant.DefaultValue.VIDEO_DISPLAY_ID, true)
                chatMediaPlayer()?.switchPlayAdvertise()
            }
            npcManager = MChatNpcManager()
            npcManager?.initNpcMediaPlayer(MChatApp.instance(), this@MChatContext, object : NpcListener {
                override fun onNpcReady(id: Int, sourceName: String) {

                }

                override fun onNpcFail() {
                }

            })
            mchatSceneEventHandlerSet.forEach {
                it.onEnterSceneResult(errorCode)
            }
        }

        override fun onLeaveSceneResult(errorCode: Int) {
            LogTools.d(TAG, "onLeaveSceneResult errorCode:$errorCode")
            isInScene = false
            chatMediaPlayer()?.stop()
            if (errorCode == 0) {
                metaChatScene?.release()
                metaChatScene?.removeEventHandler(this)
                metaChatScene = null
            }
            unityCmd = null
            mchatSceneEventHandlerSet.forEach {
                it.onLeaveSceneResult(errorCode)
            }
        }

        override fun onRecvMessageFromScene(message: ByteArray?) {
            val msg = if (message != null) String(message) else ""
            LogTools.d(TAG, "onRecvMessageFromScene message:$msg")
            mchatSceneEventHandlerSet.forEach {
                it.onRecvMessageFromScene(message)
            }
            getUnityCmd()?.handleSceneMessage(msg)
        }

        override fun onUserPositionChanged(uid: String, posInfo: MetachatUserPositionInfo) {
//        LogTools.d(
//            TAG,
//            "onUserPositionChanged uid:$uid,${Arrays.toString(posInfo.mPosition)},${Arrays.toString(posInfo.mForward)},${
//                Arrays.toString(posInfo.mRight)
//            },${Arrays.toString(posInfo.mUp)}"
//        )
            mchatSceneEventHandlerSet.forEach {
                it.onUserPositionChanged(uid, posInfo)
            }
        }

        override fun onReleasedScene(status: Int) {
            LogTools.d(TAG, "onReleasedScene status:$status")
            mchatSceneEventHandlerSet.forEach {
                it.onReleasedScene(status)
            }
        }
    }

    fun initialize(context: Context): Boolean {
        var ret = Constants.ERR_OK
        if (rtcEngine == null) {
            try {
                rtcEngine = RtcEngine.create(context, MChatKeyCenter.RTC_APP_ID, object : IRtcEngineEventHandler() {
                    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                        LogTools.d(TAG, "onJoinChannelSuccess channel:$channel,uid:$uid")
                    }

                    override fun onUserOffline(uid: Int, reason: Int) {
                        LogTools.d(TAG, "onUserOffline uid:$uid,reason:$reason")
                        chatSpatialAudio()?.removeRemotePosition(uid)
                    }

                    override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray) {
                        LogTools.d(TAG, "onStreamMessage uid:$uid,streamId:$streamId")
                        MChatStreamParser.parse(uid, streamId, data)
                    }

                    override fun onStreamMessageError(uid: Int, streamId: Int, error: Int, missed: Int, cached: Int) {
                        LogTools.d(
                            TAG,
                            "onStreamMessageError uid:$uid,streamId:$streamId,error:$error,missed:$missed,cached:$cached"
                        )
                    }
                })
                rtcEngine?.apply {
                    setParameters("{\"rtc.enable_debug_log\":true}")
                    enableAudio()
                    enableVideo()
                    setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                    setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_GAME_STREAMING)
                }
                metaChatService = IMetachatService.create()
                metaChatService?.addEventHandler(mChatEventHandler)
                val config: MetachatConfig = MetachatConfig().apply {
                    mRtcEngine = rtcEngine
                    mAppId = MChatKeyCenter.RTC_APP_ID
                    mRtmToken = MChatKeyCenter.RTM_TOKEN
                    mLocalDownloadPath = context.filesDir?.path ?: ""
                    mUserId = MChatKeyCenter.curUid.toString()
                    mEventHandler = mChatEventHandler
                }
                metaChatService?.let {
                    ret += it.initialize(config)
                }
                LogTools.d(TAG, "launcher version:${metaChatService?.getLauncherVersion(context)}")
                rtcEngine?.let { rtc ->
                    // 创建电视播放器
                    rtc.createMediaPlayer()?.let { mediaPLayer ->
                        chatMediaPlayer = MChatAgoraMediaPlayer(rtc, mediaPLayer)
                        chatMediaPlayer?.initMediaPlayer()
                        chatMediaPlayer?.setOnMediaVideoFramePushListener { frame ->
                            metaChatScene?.pushVideoFrameToDisplay(MChatConstant.DefaultValue.VIDEO_DISPLAY_ID, frame)
                        }
                    }
                    // 注册视频帧观测器
                    rtc.registerVideoFrameObserver(object : MChatBaseVideoFrameObserver() {

                        override fun onRenderVideoFrame(channelId: String?, uid: Int, videoFrame: VideoFrame): Boolean {
                            if (chatMediaPlayer()?.curUserInKaraoke() == false) {
                                // 在k歌中不需要往场景内推送原始视频帧
                                metaChatScene?.pushVideoFrameToDisplay(
                                    MChatConstant.DefaultValue.VIDEO_DISPLAY_ID, videoFrame
                                )
                                return true
                            }
                            return super.onRenderVideoFrame(channelId, uid, videoFrame)
                        }
                    })
                    // 创建空间音频引擎
                    ILocalSpatialAudioEngine.create()?.let { localSpatialAudio ->
                        val localSpatialAudioConfig = LocalSpatialAudioConfig().apply {
                            mRtcEngine = rtc
                        }
                        localSpatialAudio.initialize(localSpatialAudioConfig)
                        chatSpatialAudio = MChatSpatialAudio(MChatKeyCenter.curUid, localSpatialAudio)
                        chatSpatialAudio?.initSpatialAudio()
                    }
                    // 创建数据流
                    val dataStreamConfig = DataStreamConfig().apply {
                        ordered = true
                    }
                    myStreamId = rtc.createDataStream(dataStreamConfig)
                }
            } catch (e: Exception) {
                LogTools.e(TAG, "rtcEngine initialize error:${e.message}")
                return false
            }
        }
        return ret == Constants.ERR_OK
    }

    // npc media player
    fun createLocalSourcePlayer(id: Int, sourcePath: String): MChatLocalSourceMediaPlayer? {
        return rtcEngine?.let { it ->
            val player = it.createMediaPlayer()
            return MChatLocalSourceMediaPlayer(id, player, sourcePath)
        }
    }

    fun destroy() {
        IMetachatService.destroy()
        metaChatService?.removeEventHandler(mChatEventHandler)
        metaChatService = null
        npcManager?.stopAll()
        npcManager?.destroy()
        npcManager = null
        chatMediaPlayer?.destroy()
        chatMediaPlayer = null
        chatSpatialAudio?.destroy()
        chatSpatialAudio = null
        RtcEngine.destroy()
        rtcEngine = null
    }

    fun registerMetaChatEventHandler(eventHandler: IMetachatEventHandler) {
        mchatEventHandlerSet.add(eventHandler)
    }

    fun unregisterMetaChatEventHandler(eventHandler: IMetachatEventHandler) {
        mchatEventHandlerSet.remove(eventHandler)
    }

    fun registerMetaChatSceneEventHandler(eventHandler: IMetachatSceneEventHandler) {
        mchatSceneEventHandlerSet.add(eventHandler)
    }

    fun unregisterMetaChatSceneEventHandler(eventHandler: IMetachatSceneEventHandler) {
        mchatSceneEventHandlerSet.remove(eventHandler)
    }

    fun getSceneInfo(): MetachatSceneInfo {
        return sceneInfo ?: MetachatSceneInfo()
    }

    fun getSceneInfos(): Boolean {
        return metaChatService?.sceneInfos == Constants.ERR_OK
    }

    fun isSceneDownloaded(sceneInfo: MetachatSceneInfo): Boolean {
        return (metaChatService?.isSceneDownloaded(sceneInfo.mSceneId) ?: 0) > 0
    }

    fun downloadScene(sceneInfo: MetachatSceneInfo): Boolean {
        return metaChatService?.downloadScene(sceneInfo.mSceneId) == Constants.ERR_OK
    }

    fun cancelDownloadScene(sceneInfo: MetachatSceneInfo): Boolean {
        return metaChatService?.cancelDownloadScene(sceneInfo.mSceneId) == Constants.ERR_OK
    }

    fun prepareScene(sceneInfo: MetachatSceneInfo?, modelInfo: AvatarModelInfo?, userInfo: MetachatUserInfo?) {
        this.sceneInfo = sceneInfo
        this.modelInfo = modelInfo
        this.userInfo = userInfo
    }

    /**
     * create scene
     */
    fun createScene(activityContext: Context, roomId: String, tv: TextureView): Boolean {
        LogTools.d(TAG, "createScene $roomId")
        this.rtcRoomId = roomId
        sceneTextureView = tv
        val sceneConfig = MetachatSceneConfig()
        sceneConfig.mActivityContext = activityContext
        val ret = metaChatService?.createScene(sceneConfig) ?: -1
        return ret == Constants.ERR_OK
    }

    /**
     * enter scene
     */
    fun enterScene() {
        LogTools.d(TAG, "enterScene $rtcRoomId")
        localUserAvatar?.let {
            it.userInfo = userInfo
            //该model的mBundleType为MetachatBundleInfo.BundleType.BUNDLE_TYPE_AVATAR类型
            it.modelInfo = modelInfo
        }
        metaChatScene?.let {
            //使能位置信息回调功能
            it.enableUserPositionNotification(true)
            //设置回调接口
            it.addEventHandler(mChatSceneEventHandler)
            val config = EnterSceneConfig()
            config.mSceneView = sceneTextureView  //sceneView必须为Texture类型，为渲染unity显示的view
            config.mRoomName = rtcRoomId  //rtc加入channel的ID
            config.mSceneId = sceneInfo?.mSceneId ?: 0   //内容中心对应的ID
            it.enterScene(config)
        }
    }

    fun updateRole(role: Int): Boolean {
        var ret = Constants.ERR_OK
        //是否为broadcaster
        val isBroadcaster = role == Constants.CLIENT_ROLE_BROADCASTER
        rtcEngine?.let {
            val options = ChannelMediaOptions().apply {
                publishMicrophoneTrack = isBroadcaster
                clientRoleType = role
            }
            ret += it.updateChannelMediaOptions(options)
        }
        modelInfo?.let {
            it.mLocalVisible = true
            it.mRemoteVisible = isBroadcaster
            it.mSyncPosition = isBroadcaster
        }
        localUserAvatar?.let {
            it.modelInfo = modelInfo
            ret += it.applyInfo()
        }
        return ret == Constants.ERR_OK
    }

    fun enableLocalAudio(enabled: Boolean): Boolean {
        return rtcEngine?.enableLocalAudio(enabled) == Constants.ERR_OK
    }

    fun muteAllRemoteAudioStreams(mute: Boolean): Boolean {
        return if (chatSpatialAudio() != null) {
            chatSpatialAudio()?.muteAllRemoteAudioStreams(mute) == Constants.ERR_OK
        } else {
            rtcEngine?.muteAllRemoteAudioStreams(mute) == Constants.ERR_OK
        }
    }

    fun leaveScene(): Boolean {
        var ret = Constants.ERR_OK
        metaChatScene?.let {
            ret += rtcEngine?.leaveChannel() ?: -1
            ret += it.leaveScene()
        }
        LogTools.d(TAG, "leaveScene success $rtcRoomId")
        return ret == Constants.ERR_OK
    }

    fun isInScene(): Boolean = isInScene

    // 发送数据流
    fun sendStreamMessage(data: String): Int {
        return rtcEngine?.sendStreamMessage(myStreamId, data.toByteArray()) ?: Constants.ERR_FAILED
    }
}