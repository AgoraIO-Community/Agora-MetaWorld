package io.agora.metachat.game.sence

import io.agora.metachat.MetachatUserPositionInfo
import io.agora.metachat.game.internal.MChatBaseSceneEventHandler
import io.agora.metachat.global.MChatConstant
import io.agora.rtc2.Constants
import io.agora.spatialaudio.ILocalSpatialAudioEngine
import io.agora.spatialaudio.RemoteVoicePositionInfo

/**
 * @author create by zhangwei03
 */
class MChatSpatialAudio constructor(
    private val localUid: Int,
    private val spatialAudioEngine: ILocalSpatialAudioEngine
) {

    private val chatContext by lazy {
        MChatContext.instance()
    }

    // 音效距离
    var recvRange: Float = MChatConstant.DefaultValue.DEFAULT_RECV_RANGE

    // 衰减系数
    var distanceUnit: Float = MChatConstant.DefaultValue.DEFAULT_DISTANCE_UNIT

    private val mChatSceneEventHandler = object : MChatBaseSceneEventHandler() {
        override fun onUserPositionChanged(uid: String, posInfo: MetachatUserPositionInfo) {
            val userId = uid.toIntOrNull() ?: -1
            if (localUid == userId) {
                spatialAudioEngine.updateSelfPosition(posInfo.mPosition, posInfo.mForward, posInfo.mRight, posInfo.mUp)
            } else {
                spatialAudioEngine.updateRemotePosition(userId, RemoteVoicePositionInfo().apply {
                    position = posInfo.mPosition
                    forward = posInfo.mForward
                })
            }
        }
    }

    fun initSpatialAudio() {
        chatContext.registerMetaChatSceneEventHandler(mChatSceneEventHandler)
        spatialAudioEngine.muteLocalAudioStream(false)
        spatialAudioEngine.muteAllRemoteAudioStreams(false)
        setAudioRecvRange(recvRange,true)
        setDistanceUnit(distanceUnit,true)
    }

    fun destroy() {
        chatContext.unregisterMetaChatSceneEventHandler(mChatSceneEventHandler)
        ILocalSpatialAudioEngine.destroy()
    }

    fun updateLocalMediaPlayerPosition(id: Int, position: FloatArray, forward: FloatArray) {
        val info = RemoteVoicePositionInfo()
        info.position = position
        info.forward = forward
        spatialAudioEngine.updatePlayerPositionInfo(id, info)
    }

    fun removeRemotePosition(uid: Int): Int {
        return spatialAudioEngine.removeRemotePosition(uid)
    }

    fun muteAllRemoteAudioStreams(mute: Boolean): Int {
        return spatialAudioEngine.muteAllRemoteAudioStreams(mute)
    }

    // 音效距离
    fun setAudioRecvRange(value:Float,forced: Boolean = false):Boolean{
        var result = false
        if (forced || this.recvRange != value) {
            spatialAudioEngine.setAudioRecvRange(value).also {
                if (Constants.ERR_OK == it) {
                    this.recvRange = value
                    result = true
                }
            }
        }
        return result

    }

    // 衰减系数
    fun setDistanceUnit(value:Float,forced: Boolean = false):Boolean{
        var result = false
        if (forced || this.distanceUnit != value) {
            spatialAudioEngine.setDistanceUnit(value).also {
                if (Constants.ERR_OK == it) {
                    this.distanceUnit = value
                    result = true
                }
            }
        }
        return result
    }
}