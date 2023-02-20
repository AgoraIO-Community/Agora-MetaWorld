package io.agora.metachat.game.internal

import io.agora.base.VideoFrame
import io.agora.rtc2.video.IVideoFrameObserver

/**
 * @author create by zhangwei03
 */
open class MChatBaseVideoFrameObserver : IVideoFrameObserver {
    override fun onCaptureVideoFrame(videoFrame: VideoFrame?): Boolean {
        return false
    }

    override fun onPreEncodeVideoFrame(videoFrame: VideoFrame?): Boolean {
        return false
    }

    override fun onScreenCaptureVideoFrame(videoFrame: VideoFrame?): Boolean {
        return false
    }

    override fun onPreEncodeScreenVideoFrame(videoFrame: VideoFrame?): Boolean {
        return false
    }

    override fun onMediaPlayerVideoFrame(videoFrame: VideoFrame, mediaPlayerId: Int): Boolean {
        return false
    }

    override fun onRenderVideoFrame(channelId: String?, uid: Int, videoFrame: VideoFrame): Boolean {
        return false
    }

    override fun getVideoFrameProcessMode(): Int {
        return 0
    }

    override fun getVideoFormatPreference(): Int {
        return 1
    }

    override fun getRotationApplied(): Boolean {
        return false
    }

    override fun getMirrorApplied(): Boolean {
        return false
    }

    override fun getObservedFramePosition(): Int {
        return 0
    }
}