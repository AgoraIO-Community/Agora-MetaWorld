package io.agora.metachat.example.inf;


import io.agora.rtc2.IRtcEngineEventHandler;

public interface IRtcEventCallback {
    default void onJoinChannelSuccess(String channel, int uid, int elapsed) {
    }

    default void onUserOffline(int uid, int reason) {
    }

    default void onUserJoined(int uid, int elapsed) {
    }

    default void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
    }

    default void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {

    }
}
