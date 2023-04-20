package io.agora.metachat.game.sence

import io.agora.base.VideoFrame
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerVideoFrameObserver
import io.agora.metachat.game.internal.MChatBaseMediaPlayerObserver
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.tools.LogTools
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.RtcEngine

/**
 * @author create by zhangwei03
 */
class MChatAgoraMediaPlayer constructor(val rtcEngine: RtcEngine, val mediaPlayer: IMediaPlayer) {

    companion object {

        private const val TAG = "MChatAgoraMediaPlayer"
    }

    private var playingUrl: String = ""

    private val mediaPlayerListeners = mutableSetOf<MChatMediaPlayerListener>()

    fun registerListener(listener: MChatMediaPlayerListener) {
        mediaPlayerListeners.add(listener)
    }

    fun unregisterListener(listener: MChatMediaPlayerListener) {
        mediaPlayerListeners.remove(listener)
    }

    // 媒体播放器的视频数据观测器。
    private var mediaVideoFramePushListener: IMediaPlayerVideoFrameObserver? = null

    // 电视音量
    var tvVolume: Int = MChatConstant.DefaultValue.DEFAULT_TV_VOLUME

    // 当前用户是否在k歌中
    private var curUserInKaraoke = false

    // 其他用户是否在k歌中
    private var otherUserInKaraoke = false

    // 当前媒体流数量
    private var streamCount = 0

    private val mediaPlayerObserver = object : MChatBaseMediaPlayerObserver() {
        override fun onPlayerStateChanged(state: Constants.MediaPlayerState, error: Constants.MediaPlayerError) {
            LogTools.d(TAG, "onPlayerStateChanged state:$state,error:$error")
            if (Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED == state) {
                streamCount = mediaPlayer.streamCount.also {
                    if (it <= 0) return@also
                    for (i in 0 until streamCount) {
                        // 通过媒体流的索引值获取媒体流信息。 需要在 getStreamCount 后调用该方法。
                        val info = mediaPlayer.getStreamInfo(0)
                        LogTools.d(TAG, "streamIndex:${info.streamIndex},streamType:${info.mediaStreamType}")
                    }
                }
                mediaPlayer.play()
            } else if (Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED == state) {
                // 播放完成，切歌
                mediaPlayerListeners.forEach {
                    it.onPlayCompleted(playingUrl)
                }
            }
        }
    }

    // 媒体播放器的视频数据观测器
    private val mediaPlayerVideoFrameObserver = object : IMediaPlayerVideoFrameObserver {
        override fun onFrame(frame: VideoFrame?) {
            mediaVideoFramePushListener?.onFrame(frame)
        }
    }

    fun initMediaPlayer() {
        mediaPlayer.registerPlayerObserver(mediaPlayerObserver)
        mediaPlayer.registerVideoFrameObserver(mediaPlayerVideoFrameObserver)
        setPlayerVolume(tvVolume, true)
    }

    fun setOnMediaVideoFramePushListener(mediaVideoFramePushListener: IMediaPlayerVideoFrameObserver) = apply {
        this.mediaVideoFramePushListener = mediaVideoFramePushListener
    }

    fun mediaPlayerId(): Int = mediaPlayer.mediaPlayerId

    // 切换播放宣传片
    fun switchPlayAdvertise() {
        play(MChatConstant.VIDEO_URL)
    }

    // 播放k歌
    fun switchPlayKaraoke(url: String) {
        play(url, repeatCount = 0)
        // k歌推送视频流
        val channelOptions = ChannelMediaOptions().apply {
            autoSubscribeAudio = true
            autoSubscribeVideo = true
            publishMediaPlayerId = mediaPlayer.mediaPlayerId
            publishMediaPlayerAudioTrack = true
            publishMediaPlayerVideoTrack = true
        }
        rtcEngine.updateChannelMediaOptions(channelOptions)
    }

    fun play(url: String, startPos: Long = 0, repeatCount: Int = -1) {
        mediaPlayer.stop()
        val result = mediaPlayer.open(url, startPos)
        if (result == io.agora.rtc2.Constants.ERR_OK) {
            playingUrl = url
            mediaPlayer.setLoopCount(repeatCount)
        }
    }

    fun stop() {
        mediaPlayer.registerVideoFrameObserver(null)
        mediaPlayer.stop()
        mediaPlayer.destroy()
    }

    fun pause() {
        mediaPlayer.pause()
        mediaPlayer.registerVideoFrameObserver(null)
    }

    fun resume() {
        mediaPlayer.resume()
        mediaPlayer.registerVideoFrameObserver(mediaPlayerVideoFrameObserver)
    }

    @Synchronized
    fun setPlayerVolume(volume: Int, forced: Boolean = false): Boolean {
        var result = false
        if (forced || this.tvVolume != volume) {
            mediaPlayer.adjustPlayoutVolume(volume).also {
                if (io.agora.rtc2.Constants.ERR_OK == it) {
                    this.tvVolume = volume
                    result = true
                }
            }
        }
        return result
    }

    /**
     * 打开原唱
     */
    @Synchronized
    fun userOriginalTrack(original: Boolean): Int {
        return if (original) mediaPlayer.selectAudioTrack(0) else mediaPlayer.selectAudioTrack(1)
    }

    /**
     * 调整当前播放的媒体资源的音调
     * 按半音音阶调整本地播放的音乐文件的音调，默认值为 0，即不调整音调。取值范围为 [-12,12]，每相邻两个值的音高距离相差半音。
     * 取值的绝对值越大，音调升高或降低得越多。
     */
    @Synchronized
    fun setPlayerAudioPitch(pitch: Int): Int {
        val playerAudioPitch = if (pitch < -12) {
            -12
        } else if (pitch > 12) {
            12
        } else {
            pitch
        }
        return mediaPlayer.setAudioPitch(playerAudioPitch)
    }

    @Synchronized
    fun destroy(): Int {
        mediaPlayer.unRegisterPlayerObserver(mediaPlayerObserver)
        return mediaPlayer.destroy()
    }

    @Synchronized
    fun startKaraoke() {
        curUserInKaraoke = true
    }

    @Synchronized
    fun stopKaraoke() {
        curUserInKaraoke = false
        // k歌推送视频流
        val channelOptions = ChannelMediaOptions().apply {
            autoSubscribeAudio = true
            autoSubscribeVideo = true
            publishMediaPlayerId = 0
            publishMediaPlayerAudioTrack = false
            publishMediaPlayerVideoTrack = false
        }
        rtcEngine.updateChannelMediaOptions(channelOptions)
        switchPlayAdvertise()
    }

    @Synchronized
    fun setOtherInKaraoke(otherUserInKaraoke: Boolean) {
        this.otherUserInKaraoke = otherUserInKaraoke
        if (!otherUserInKaraoke) {
            mediaPlayer.registerVideoFrameObserver(mediaPlayerVideoFrameObserver)
            // 其他用户停止k歌，继续播放宣传片
            switchPlayAdvertise()
        } else {
            // 停止播放宣传片
            pause()
        }
    }

    @Synchronized
    fun curUserInKaraoke(): Boolean = curUserInKaraoke

    @Synchronized
    fun otherUserInKaraoke(): Boolean = otherUserInKaraoke
}

interface MChatMediaPlayerListener {

    fun onPlayCompleted(url: String)
}