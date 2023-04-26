package io.agora.meta.example.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.List;

import io.agora.meta.example.inf.IMetaEventHandler;
import io.agora.metachat.AvatarModelInfo;
import io.agora.metachat.MetachatBundleInfo;
import io.agora.metachat.MetachatSceneInfo;
import io.agora.metachat.MetachatUserInfo;
import io.agora.meta.example.utils.KeyCenter;
import io.agora.meta.example.MainApplication;
import io.agora.meta.example.meta.MetaContext;
import io.agora.meta.example.utils.MetaConstants;
import io.agora.meta.example.utils.SingleLiveData;

public class MainViewModel extends ViewModel implements IMetaEventHandler {

    private final SingleLiveData<String> avatar = new SingleLiveData<>();
    private final SingleLiveData<String> nickname = new SingleLiveData<>();
    private final SingleLiveData<Integer> sex = new SingleLiveData<>();
    private final SingleLiveData<List<MetachatSceneInfo>> sceneList = new SingleLiveData<>();
    private final SingleLiveData<Long> selectScene = new SingleLiveData<>();
    private final SingleLiveData<Boolean> requestDownloading = new SingleLiveData<>();
    private final SingleLiveData<Integer> downloadingProgress = new SingleLiveData<>();

    @Override
    protected void onCleared() {
        MetaContext.getInstance().unregisterMetaChatEventHandler(this);
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

    public LiveData<Integer> getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex.postValue(sex);
    }

    public LiveData<List<MetachatSceneInfo>> getSceneList() {
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
        MetaContext metaChatContext = MetaContext.getInstance();
        metaChatContext.registerMetaChatEventHandler(this);
        boolean flag = metaChatContext.initialize(
                MainApplication.mGlobalApplication
        );
        if (flag) {
            if (MetaContext.getInstance().isEnableLocalSceneRes()) {
                prepareScene(null);
                selectScene.postValue(0L);
            } else {
                metaChatContext.getSceneInfos();
            }
        }
    }

    public void prepareScene(MetachatSceneInfo sceneInfo) {
        MetaContext metaChatContext = MetaContext.getInstance();
        metaChatContext.prepareScene(sceneInfo, new AvatarModelInfo() {{
            // TODO choose one
            if (null != sceneInfo) {
                MetachatBundleInfo[] bundles = sceneInfo.mBundles;
                for (MetachatBundleInfo bundleInfo : bundles) {
                    if (bundleInfo.mBundleType == MetachatBundleInfo.BundleType.BUNDLE_TYPE_AVATAR) {
                        mBundleCode = bundleInfo.mBundleCode;
                        break;
                    }
                }
            }
            mLocalVisible = true;
            if (MetaConstants.SCENE_GAME == MetaContext.getInstance().getCurrentScene()) {
                mRemoteVisible = true;
                mSyncPosition = true;
            } else if (MetaConstants.SCENE_DRESS == MetaContext.getInstance().getCurrentScene()) {
                mRemoteVisible = false;
                mSyncPosition = false;
            }
        }}, new MetachatUserInfo() {{
            mUserId = KeyCenter.RTM_UID;
            mUserName = MetaContext.getInstance().getRoleInfo().getName() == null ? mUserId : MetaContext.getInstance().getRoleInfo().getName();
            mUserIconUrl = MetaContext.getInstance().getRoleInfo().getAvatarUrl() == null ? "https://accpic.sd-rtn.com/pic/test/png/2.png" : MetaContext.getInstance().getRoleInfo().getAvatarUrl();
        }});
        if (!MetaContext.getInstance().isEnableLocalSceneRes()) {
            if (metaChatContext.isSceneDownloaded(sceneInfo)) {
                selectScene.postValue(sceneInfo.mSceneId);
            } else {
                requestDownloading.postValue(true);
            }
        }
    }

    public void downloadScene(MetachatSceneInfo sceneInfo) {
        MetaContext.getInstance().downloadScene(sceneInfo);
    }

    public void cancelDownloadScene(MetachatSceneInfo sceneInfo) {
        MetaContext.getInstance().cancelDownloadScene(sceneInfo);
    }

    @Override
    public void onGetSceneInfosResult(MetachatSceneInfo[] scenes, int errorCode) {
        sceneList.postValue(Arrays.asList(scenes));
    }

    @Override
    public void onDownloadSceneProgress(long mSceneId, int progress, int state) {
        Log.d("progress", String.valueOf(progress));
        if (state == SceneDownloadState.METACHAT_SCENE_DOWNLOAD_STATE_FAILED) {
            downloadingProgress.postValue(-1);
            return;
        }
        downloadingProgress.postValue(progress);
        if (state == SceneDownloadState.METACHAT_SCENE_DOWNLOAD_STATE_DOWNLOADED) {
            selectScene.postValue(mSceneId);
        }
    }
}
