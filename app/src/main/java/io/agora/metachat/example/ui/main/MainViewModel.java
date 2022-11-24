package io.agora.metachat.example.ui.main;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.List;

import io.agora.metachat.AvatarModelInfo;
import io.agora.metachat.IMetachatEventHandler;
import io.agora.metachat.IMetachatScene;
import io.agora.metachat.MetachatBundleInfo;
import io.agora.metachat.MetachatSceneInfo;
import io.agora.metachat.MetachatUserInfo;
import io.agora.metachat.example.utils.KeyCenter;
import io.agora.metachat.example.MainApplication;
import io.agora.metachat.example.metachat.MetaChatContext;
import io.agora.metachat.example.utils.MetaChatConstants;
import io.agora.metachat.example.utils.SingleLiveData;

public class MainViewModel extends ViewModel implements IMetachatEventHandler {

    private final SingleLiveData<String> avatar = new SingleLiveData<>();
    private final SingleLiveData<String> nickname = new SingleLiveData<>();
    private final SingleLiveData<Integer> sex = new SingleLiveData<>();
    private final SingleLiveData<List<MetachatSceneInfo>> sceneList = new SingleLiveData<>();
    private final SingleLiveData<Long> selectScene = new SingleLiveData<>();
    private final SingleLiveData<Boolean> requestDownloading = new SingleLiveData<>();
    private final SingleLiveData<Integer> downloadingProgress = new SingleLiveData<>();

    @Override
    protected void onCleared() {
        MetaChatContext.getInstance().unregisterMetaChatEventHandler(this);
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
        MetaChatContext metaChatContext = MetaChatContext.getInstance();
        metaChatContext.registerMetaChatEventHandler(this);
        boolean flag = metaChatContext.initialize(
                MainApplication.mGlobalApplication
        );
        if (flag) {
            metaChatContext.getSceneInfos();
        }
    }

    public void prepareScene(MetachatSceneInfo sceneInfo) {
        MetaChatContext metaChatContext = MetaChatContext.getInstance();
        metaChatContext.prepareScene(sceneInfo, new AvatarModelInfo() {{
            // TODO choose one
            MetachatBundleInfo[] bundles = sceneInfo.mBundles;
            for (MetachatBundleInfo bundleInfo : bundles) {
                if (bundleInfo.mBundleType == MetachatBundleInfo.BundleType.BUNDLE_TYPE_AVATAR) {
                    mBundleCode = bundleInfo.mBundleCode;
                    break;
                }
            }
            mLocalVisible = true;
            if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
                mRemoteVisible = true;
                mSyncPosition = true;
            } else if (MetaChatConstants.SCENE_DRESS == MetaChatContext.getInstance().getCurrentScene()) {
                mRemoteVisible = false;
                mSyncPosition = false;
            }
        }}, new MetachatUserInfo() {{
            mUserId = KeyCenter.RTM_UID;
            mUserName = MetaChatContext.getInstance().getRoleInfo().getName() == null ? mUserId : MetaChatContext.getInstance().getRoleInfo().getName();
            mUserIconUrl = MetaChatContext.getInstance().getRoleInfo().getAvatar() == null ? "https://accpic.sd-rtn.com/pic/test/png/2.png" : MetaChatContext.getInstance().getRoleInfo().getAvatar();
        }});
        if (metaChatContext.isSceneDownloaded(sceneInfo)) {
            selectScene.postValue(sceneInfo.mSceneId);
        } else {
            requestDownloading.postValue(true);
        }
    }

    public void downloadScene(MetachatSceneInfo sceneInfo) {
        MetaChatContext.getInstance().downloadScene(sceneInfo);
    }

    public void cancelDownloadScene(MetachatSceneInfo sceneInfo) {
        MetaChatContext.getInstance().cancelDownloadScene(sceneInfo);
    }

    @Override
    public void onCreateSceneResult(IMetachatScene scene, int errorCode) {
    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {

    }

    @Override
    public void onRequestToken() {

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
