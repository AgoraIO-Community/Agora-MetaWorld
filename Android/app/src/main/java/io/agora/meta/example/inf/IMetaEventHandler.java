package io.agora.meta.example.inf;

import android.view.TextureView;

import io.agora.base.VideoFrame;
import io.agora.meta.IMetaScene;
import io.agora.meta.IMetaSceneEventHandler;
import io.agora.meta.IMetaServiceEventHandler;
import io.agora.meta.MetaSceneAssetsInfo;
import io.agora.meta.MetaUserPositionInfo;

public interface IMetaEventHandler extends IMetaServiceEventHandler, IMetaSceneEventHandler {

    @Override
    default void onCreateSceneResult(IMetaScene scene, int errorCode) {

    }

    @Override
    default void onConnectionStateChanged(int state, int reason) {

    }


    @Override
    default void onTokenWillExpire() {

    }

    @Override
    default void onGetSceneAssetsInfoResult(MetaSceneAssetsInfo[] metaSceneAssetsInfos, int errorCode) {

    }

    @Override
    default void onDownloadSceneAssetsProgress(long sceneId, int progress, int state) {

    }

    @Override
    default void onEnterSceneResult(int errorCode) {

    }

    @Override
    default void onLeaveSceneResult(int errorCode) {

    }

    @Override
    default void onSceneMessageReceived(byte[] message) {

    }

    @Override
    default void onUserPositionChanged(String uid, MetaUserPositionInfo posInfo) {

    }


    @Override
    default void onReleasedScene(int status) {

    }

    @Override
    default void onSceneVideoFrameCaptured(TextureView view, VideoFrame videoFrame) {

    }

    @Override
    default void onAddSceneViewResult(TextureView view, int errorCode) {

    }

    @Override
    default void onRemoveSceneViewResult(TextureView view, int errorCode) {

    }
}
