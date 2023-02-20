package io.agora.metachat.game.internal

import io.agora.metachat.IMetachatSceneEventHandler
import io.agora.metachat.MetachatUserPositionInfo

/**
 * @author create by zhangwei03
 */
open class MChatBaseSceneEventHandler : IMetachatSceneEventHandler {
    override fun onEnterSceneResult(errorCode: Int) {
    }

    override fun onLeaveSceneResult(errorCode: Int) {
    }

    override fun onRecvMessageFromScene(message: ByteArray?) {
    }

    override fun onUserPositionChanged(uid: String, posInfo: MetachatUserPositionInfo) {
    }

    override fun onEnumerateVideoDisplaysResult(displayIds: Array<out String>?) {
    }

    override fun onReleasedScene(status: Int) {
    }
}