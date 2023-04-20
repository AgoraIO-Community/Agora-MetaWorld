package io.agora.meta.example.inf;

import android.view.TextureView;

import io.agora.base.VideoFrame;
import io.agora.metachat.IMetachatEventHandler;
import io.agora.metachat.IMetachatScene;
import io.agora.metachat.IMetachatSceneEventHandler;
import io.agora.metachat.MetachatSceneInfo;
import io.agora.metachat.MetachatUserPositionInfo;

public interface IMetaEventHandler extends IMetachatEventHandler, IMetachatSceneEventHandler {
    @Override
    default void onCreateSceneResult(IMetachatScene scene, int errorCode) {

    }

    @Override
    default void onConnectionStateChanged(int state, int reason) {

    }

    @Override
    default void onRequestToken() {

    }

    @Override
    default void onGetSceneInfosResult(MetachatSceneInfo[] sceneInfos, int errorCode) {

    }

    @Override
    default void onDownloadSceneProgress(long sceneId, int progress, int state) {

    }

    @Override
    default void onEnterSceneResult(int errorCode) {

    }

    @Override
    default void onLeaveSceneResult(int errorCode) {

    }

    @Override
    default void onRecvMessageFromScene(byte[] message) {

    }

    @Override
    default void onUserPositionChanged(String uid, MetachatUserPositionInfo posInfo) {

    }

    @Override
    default void onEnumerateVideoDisplaysResult(String[] displayIds) {

    }

    @Override
    default void onReleasedScene(int status) {

    }

    @Override
    default void onSceneVideoFrame(TextureView view, VideoFrame videoFrame) {

    }
}
