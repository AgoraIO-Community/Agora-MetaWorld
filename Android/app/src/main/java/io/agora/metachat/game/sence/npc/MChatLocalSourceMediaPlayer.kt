package io.agora.metachat.game.sence.npc

import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.metachat.game.internal.MChatBaseMediaPlayerObserver
import io.agora.metachat.global.MChatConstant

/**
 * @author create by zhangwei03
 */
class MChatLocalSourceMediaPlayer constructor(
    private val sourceId: Int,
    private val mediaPlayer: IMediaPlayer,
    private val sourceUrl: String
) {

    private val playerObserver = object : MChatBaseMediaPlayerObserver() {
        override fun onPlayerStateChanged(
            state: Constants.MediaPlayerState,
            error: Constants.MediaPlayerError
        ) {
            if (state == Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                mediaPlayer.mute(true)
                mediaPlayer.play()
            }
        }
    }

    init {
        mediaPlayer.registerPlayerObserver(playerObserver)
    }

    fun sourceId(): Int {
        return sourceId
    }

    fun playerId(): Int {
        return mediaPlayer.mediaPlayerId
    }

    fun play(loop: Boolean) {
        mediaPlayer.setLoopCount(if (loop) -1 else 0)
        mediaPlayer.open(sourceUrl, 0)
    }

    fun stop() {
        mediaPlayer.stop()
    }

    fun destroy() {
        mediaPlayer.unRegisterPlayerObserver(playerObserver)
        mediaPlayer.destroy()
    }

    fun setPlayerVolume(volume: Int): Int {
        return mediaPlayer.adjustPlayoutVolume(volume)
    }
}