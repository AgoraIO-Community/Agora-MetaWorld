package io.agora.metachat.game

import android.app.Activity
import android.view.TextureView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.metachat.*
import io.agora.metachat.game.internal.MChatBaseEventHandler
import io.agora.metachat.game.internal.MChatBaseSceneEventHandler
import io.agora.metachat.game.sence.MChatContext
import io.agora.metachat.imkit.MChatGroupIMManager
import io.agora.metachat.service.MChatServiceProtocol
import io.agora.metachat.service.MChatSubscribeDelegate
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.SingleLiveData
import io.agora.metachat.tools.ThreadTools
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler.ErrorCode

/**
 * @author create by zhangwei03
 */
class MChatGameViewModel : ViewModel() {

    private val chatServiceProtocol: MChatServiceProtocol = MChatServiceProtocol.getImplInstance()

    private val _isEnterScene = SingleLiveData<Boolean>()
    private val _onlineMic = SingleLiveData<Boolean>()
    private val _muteRemote = SingleLiveData<Boolean>()
    private val _muteLocal = SingleLiveData<Boolean>()
    private val _exitGame = SingleLiveData<Boolean>()
    private val _leaveRoom = SingleLiveData<Boolean>()
    private val _groupDestroyRoom = SingleLiveData<Boolean>()
    private val _onConnectError = SingleLiveData<Pair<Int, Int>>()

    fun isEnterSceneObservable(): LiveData<Boolean> = _isEnterScene
    fun onlineMicObservable(): LiveData<Boolean> = _onlineMic
    fun muteRemoteObservable(): LiveData<Boolean> = _muteRemote
    fun muteLocalObservable(): LiveData<Boolean> = _muteLocal
    fun exitGameObservable(): LiveData<Boolean> = _exitGame
    fun leaveRoomObservable(): LiveData<Boolean> = _leaveRoom
    fun groupDestroyRoomObservable(): LiveData<Boolean> = _groupDestroyRoom
    fun sceneConnectErrorObservable(): LiveData<Pair<Int, Int>> = _onConnectError

    var mReCreateScene = false
//    var mSurfaceSizeChange = false

    private val mchatContext by lazy {
        MChatContext.instance()
    }

    fun cleared() {
        mchatContext.unregisterMetaChatEventHandler(mChatEventHandler)
        mchatContext.unregisterMetaChatSceneEventHandler(mChatSceneEventHandler)
        chatServiceProtocol.unsubscribeEvent(chatDelegate)
    }

    private val mChatEventHandler = object : MChatBaseEventHandler() {
        override fun onCreateSceneResult(scene: IMetachatScene?, errorCode: Int) {
            ThreadTools.get().runOnMainThread {
                mchatContext.enterScene()
            }
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            super.onConnectionStateChanged(state, reason)
            if (state == 4) {
                _onConnectError.postValue(Pair(state, reason))
            }
        }
    }

    private val mChatSceneEventHandler = object : MChatBaseSceneEventHandler() {
        override fun onEnterSceneResult(errorCode: Int) {
            if (errorCode != ErrorCode.ERR_OK) {
                LogTools.e("enter scene failed:$errorCode")
                return
            }
            _isEnterScene.postValue(true)
            _onlineMic.postValue(true)
            sendMuteRemoteEvent(false)
            _muteLocal.postValue(true)
            resetSceneState()
        }

        override fun onLeaveSceneResult(errorCode: Int) {
            _isEnterScene.postValue(false)
        }

        override fun onReleasedScene(status: Int) {
            if (status == ErrorCode.ERR_OK) {
                ThreadTools.get().runOnMainThread {
                    cleared()
                    mchatContext.destroy()
                }
                _exitGame.postValue(true)
            }
        }
    }

    private val chatDelegate = object: MChatSubscribeDelegate {
        override fun onGroupDestroyed(groupId: String) {
            super.onGroupDestroyed(groupId)
            _groupDestroyRoom.postValue(true)
        }
    }

    fun initMChatScene() {
        mchatContext.registerMetaChatEventHandler(mChatEventHandler)
        mchatContext.registerMetaChatSceneEventHandler(mChatSceneEventHandler)
        chatServiceProtocol.subscribeEvent(chatDelegate)
    }

    fun createScene(activity: Activity, roomId: String, tv: TextureView) {
        resetSceneState()
        mchatContext.createScene(activity, roomId, tv)
    }

    fun resetSceneState() {
        mReCreateScene = false
    }

    fun maybeCreateScene(activity: Activity, roomId: String, tv: TextureView): Boolean {
        if (mReCreateScene) {
            createScene(activity, roomId, tv)
            return true
        }
        return false
    }

    // 发送上下麦
    fun sendOnlineEvent() {
        if (_onlineMic.value == true) {
            val result = mchatContext.updateRole(Constants.CLIENT_ROLE_AUDIENCE)
            if (result) {
                _onlineMic.postValue(false)
            } else {
                LogTools.e("offline Mic error")
            }
        } else {
            val result = mchatContext.updateRole(Constants.CLIENT_ROLE_BROADCASTER)
            if (result) {
                _onlineMic.postValue(true)
            } else {
                LogTools.e("online Mic error")
            }
        }
    }

    // 远端静音
    fun sendMuteRemoteEvent() {
        sendMuteRemoteEvent(_muteRemote.value != true)
    }

    fun sendMuteRemoteEvent(mute: Boolean) {
        val result = mchatContext.muteAllRemoteAudioStreams(mute)
        LogTools.e("远端静音    sendMuteRemoteEvent    result = $result")
        if (!result) {
            return
        }
        _muteRemote.postValue(mute)
    }

    // 本地静音
    fun sendMuteLocalEvent() {
        if (_muteLocal.value == true) {
            val result = mchatContext.enableLocalAudio(false)
            if (result) {
                _muteLocal.postValue(false)
            } else {
                LogTools.e("unMute local error")
            }
        } else {
            val result = mchatContext.enableLocalAudio(true)
            if (result) {
                _muteLocal.postValue(true)
            } else {
                LogTools.e("mute local error")
            }
        }
    }

    // 离开房间
    fun leaveRoom() {
        // 退出环信im 房间，不管是否成功
        MChatGroupIMManager.instance().leaveGroupTask {
            chatServiceProtocol.leaveRoom {
                _leaveRoom.postValue(true)
            }
        }
    }
}