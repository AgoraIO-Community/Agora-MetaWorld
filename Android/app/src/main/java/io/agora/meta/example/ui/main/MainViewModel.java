package io.agora.meta.example.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.List;

import io.agora.meta.AvatarModelInfo;
import io.agora.meta.MetaBundleInfo;
import io.agora.meta.MetaSceneAssetsInfo;
import io.agora.meta.MetaUserInfo;
import io.agora.meta.example.config.Config;
import io.agora.meta.example.inf.IMetaEventHandler;
import io.agora.meta.example.utils.KeyCenter;
import io.agora.meta.example.MainApplication;
import io.agora.meta.example.meta.MetaContext;
import io.agora.meta.example.utils.MetaConstants;
import io.agora.meta.example.utils.SingleLiveData;

public class MainViewModel extends ViewModel implements IMetaEventHandler {

    private final SingleLiveData<String> avatar = new SingleLiveData<>();
    private final SingleLiveData<String> nickname = new SingleLiveData<>();
    private final SingleLiveData<List<MetaSceneAssetsInfo>> sceneList = new SingleLiveData<>();
    private final SingleLiveData<Long> selectScene = new SingleLiveData<>();
    private final SingleLiveData<Boolean> requestDownloading = new SingleLiveData<>();
    private final SingleLiveData<Integer> downloadingProgress = new SingleLiveData<>();

    @Override
    protected void onCleared() {
        MetaContext.getInstance().unregisterMetaServiceEventHandler(this);
        super.onCleared();
    }

    public LiveData<String> getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar.postValue(avatar);
    }

    public LiveData<String> getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname.postValue(nickname);
    }


    public LiveData<List<MetaSceneAssetsInfo>> getSceneList() {
        return sceneList;
    }

    public LiveData<Long> getSelectScene() {
        return selectScene;
    }

    public LiveData<Boolean> getRequestDownloading() {
        return requestDownloading;
    }

    public LiveData<Integer> getDownloadingProgress() {
        return downloadingProgress;
    }

    public void getScenes() {
        MetaContext metaContext = MetaContext.getInstance();
        metaContext.registerMetaServiceEventHandler(this);
        boolean flag = metaContext.initialize(
                MainApplication.mGlobalApplication
        );
        if (flag) {
            if (Config.ENABLE_LOCAL_SCENE_RES) {
                prepareScene(null);
                selectScene.postValue(0L);
            } else {
                metaContext.getSceneInfos();
            }
        }
    }

    public void prepareScene(MetaSceneAssetsInfo sceneInfo) {
        MetaContext metaContext = MetaContext.getInstance();
        metaContext.prepareScene(sceneInfo, new AvatarModelInfo() {{
            // TODO choose one
            if (null != sceneInfo) {
                MetaBundleInfo[] bundles = sceneInfo.mBundles;
                for (MetaBundleInfo bundleInfo : bundles) {
                    if (bundleInfo.mBundleType == MetaBundleInfo.BundleType.BUNDLE_TYPE_AVATAR) {
                        mBundleCode = bundleInfo.mBundleCode;
                        break;
                    }
                }
            }
            mLocalVisible = true;
            if (MetaConstants.SCENE_COFFEE == MetaContext.getInstance().getCurrentScene()) {
                mRemoteVisible = true;
                mSyncPosition = true;
            } else if (MetaConstants.SCENE_DRESS == MetaContext.getInstance().getCurrentScene()) {
                mRemoteVisible = false;
                mSyncPosition = false;
            }
        }}, new MetaUserInfo() {{
            mUserId = KeyCenter.RTM_UID;
            mUserName = MetaContext.getInstance().getRoleInfo().getName() == null ? mUserId : MetaContext.getInstance().getRoleInfo().getName();
            mUserIconUrl = MetaContext.getInstance().getRoleInfo().getAvatarUrl() == null ? "https://accpic.sd-rtn.com/pic/test/png/2.png" : MetaContext.getInstance().getRoleInfo().getAvatarUrl();
        }});
        if (!Config.ENABLE_LOCAL_SCENE_RES) {
            if (metaContext.isSceneDownloaded(sceneInfo)) {
                selectScene.postValue(sceneInfo.mSceneId);
            } else {
                requestDownloading.postValue(true);
            }
        }
    }

    public void downloadScene(MetaSceneAssetsInfo sceneInfo) {
        MetaContext.getInstance().downloadScene(sceneInfo);
    }

    public void cancelDownloadScene(MetaSceneAssetsInfo sceneInfo) {
        MetaContext.getInstance().cancelDownloadScene(sceneInfo);
    }


    @Override
    public void onGetSceneAssetsInfoResult(MetaSceneAssetsInfo[] metaSceneAssetsInfos, int errorCode) {
        sceneList.postValue(Arrays.asList(metaSceneAssetsInfos));
    }

    @Override
    public void onDownloadSceneAssetsProgress(long sceneId, int progress, int state) {
        Log.d("progress", String.valueOf(progress));
        if (state == SceneDownloadState.META_SCENE_DOWNLOAD_STATE_FAILED) {
            downloadingProgress.postValue(-1);
            return;
        }
        downloadingProgress.postValue(progress);
        if (state == SceneDownloadState.META_SCENE_DOWNLOAD_STATE_DOWNLOADED) {
            selectScene.postValue(sceneId);
        }
    }
}
