package io.agora.metachat.game.internal

import io.agora.metachat.IMetachatEventHandler
import io.agora.metachat.IMetachatScene
import io.agora.metachat.MetachatSceneInfo

/**
 * @author create by zhangwei03
 */
open class MChatBaseEventHandler: IMetachatEventHandler {
    override fun onCreateSceneResult(scene: IMetachatScene?, errorCode: Int) {
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
    }

    override fun onRequestToken() {
    }

    override fun onGetSceneInfosResult(sceneInfos: Array<out MetachatSceneInfo>, errorCode: Int) {
    }

    override fun onDownloadSceneProgress(sceneId: Long, progress: Int, state: Int) {
    }
}