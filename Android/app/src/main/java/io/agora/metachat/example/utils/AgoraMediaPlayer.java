package io.agora.metachat.example.utils;

import android.util.Log;

import io.agora.base.VideoFrame;
import io.agora.mediaplayer.Constants;
import io.agora.mediaplayer.IMediaPlayer;
import io.agora.mediaplayer.IMediaPlayerObserver;
import io.agora.mediaplayer.IMediaPlayerVideoFrameObserver;
import io.agora.mediaplayer.data.PlayerUpdatedInfo;
import io.agora.mediaplayer.data.SrcInfo;
import io.agora.rtc2.RtcEngine;

public class AgoraMediaPlayer implements IMediaPlayerObserver, IMediaPlayerVideoFrameObserver {
    private static final String TAG = AgoraMediaPlayer.class.getSimpleName();
    private volatile static AgoraMediaPlayer mAgoraMediaPlayer;
    private IMediaPlayer mMediaPlayer;
    private OnMediaVideoFramePushListener mOnMediaVideoFramePushListener;

    private AgoraMediaPlayer() {

    }

    public static AgoraMediaPlayer getInstance() {
        if (null == mAgoraMediaPlayer) {
            synchronized (AgoraMediaPlayer.class) {
                if (null == mAgoraMediaPlayer) {
                    mAgoraMediaPlayer = new AgoraMediaPlayer();
                }
            }
        }
        return mAgoraMediaPlayer;
    }

    public void initMediaPlayer(RtcEngine rtcEngine) {
        mMediaPlayer = rtcEngine.createMediaPlayer();
        mMediaPlayer.adjustPlayoutVolume(20);
        mMediaPlayer.registerPlayerObserver(this);
        mMediaPlayer.registerVideoFrameObserver(this);
    }

    public void setOnMediaVideoFramePushListener(OnMediaVideoFramePushListener onMediaVideoFramePushListener) {
        this.mOnMediaVideoFramePushListener = onMediaVideoFramePushListener;
    }

    public void play(String url, long startPos) {
        int ret = mMediaPlayer.open(url, startPos);
        if (ret == io.agora.rtc2.Constants.ERR_OK) {
            mMediaPlayer.setLoopCount(MetaChatConstants.PLAY_ADVERTISING_VIDEO_REPEAT);
        }
    }

    public void stop() {
        if (null != mMediaPlayer) {
            mMediaPlayer.registerVideoFrameObserver(null);
            mMediaPlayer.unRegisterPlayerObserver(this);
            mMediaPlayer.stop();
            mMediaPlayer.destroy();
            mMediaPlayer = null;
        }
    }

    public void pause() {
        if (null != mMediaPlayer) {
            mMediaPlayer.pause();
            mMediaPlayer.registerVideoFrameObserver(null);
        }
    }

    public void resume() {
        if (null != mMediaPlayer) {
            mMediaPlayer.resume();
            mMediaPlayer.registerVideoFrameObserver(this);
        }
    }


    @Override
    public void onPlayerStateChanged(Constants.MediaPlayerState state, Constants.MediaPlayerError error) {
        Log.i(TAG, "onPlayerStateChanged state=" + state);
        if (Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED == state) {
            if (mMediaPlayer.play() != io.agora.rtc2.Constants.ERR_OK) {
                Log.i(TAG, "onPlayerStateChanged play success");
            }
        }
    }

    @Override
    public void onPositionChanged(long position_ms) {

    }

    @Override
    public void onPlayerEvent(Constants.MediaPlayerEvent eventCode, long elapsedTime, String message) {

    }

    @Override
    public void onMetaData(Constants.MediaPlayerMetadataType type, byte[] data) {

    }

    @Override
    public void onPlayBufferUpdated(long playCachedBuffer) {

    }

    @Override
    public void onPreloadEvent(String src, Constants.MediaPlayerPreloadEvent event) {

    }

    @Override
    public void onAgoraCDNTokenWillExpire() {

    }

    @Override
    public void onPlayerSrcInfoChanged(SrcInfo from, SrcInfo to) {

    }

    @Override
    public void onPlayerInfoUpdated(PlayerUpdatedInfo info) {

    }

    @Override
    public void onAudioVolumeIndication(int volume) {

    }

    @Override
    public void onFrame(VideoFrame frame) {
        if (null != mOnMediaVideoFramePushListener) {
            mOnMediaVideoFramePushListener.onMediaVideoFramePushed(frame);
        }
    }


    public interface OnMediaVideoFramePushListener {
        void onMediaVideoFramePushed(VideoFrame frame);
    }
}
