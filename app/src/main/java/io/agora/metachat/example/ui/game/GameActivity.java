package io.agora.metachat.example.ui.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.agora.metachat.IMetachatEventHandler;
import io.agora.metachat.IMetachatScene;
import io.agora.metachat.IMetachatSceneEventHandler;
import io.agora.metachat.MetachatSceneInfo;
import io.agora.metachat.MetachatUserPositionInfo;
import io.agora.metachat.example.MainActivity;
import io.agora.metachat.example.adapter.SkinGridViewAdapter;
import io.agora.metachat.example.adapter.ViewPagerAdapter;
import io.agora.metachat.example.data.SkinsData;
import io.agora.metachat.example.metachat.MetaChatContext;
import io.agora.metachat.example.R;
import io.agora.metachat.example.databinding.GameActivityBinding;
import io.agora.metachat.example.dialog.CustomDialog;
import io.agora.metachat.example.models.RoleInfo;
import io.agora.metachat.example.models.SkinInfo;
import io.agora.metachat.example.models.TabEntity;
import io.agora.metachat.example.ui.view.CirclePageIndicator;
import io.agora.metachat.example.utils.KeyCenter;
import io.agora.metachat.example.utils.MetaChatConstants;
import io.agora.rtc2.Constants;

public class GameActivity extends Activity implements IMetachatSceneEventHandler, IMetachatEventHandler, SkinGridViewAdapter.SkinItemClick {

    private final String TAG = GameActivity.class.getSimpleName();
    private GameActivityBinding binding;
    private TextureView mTextureView = null;

    private final ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private static final int SKIN_TAB_MAX_PAGE_SIZE = 4;
    private int mCurrentTabIndex;
    private List<SkinGridViewAdapter> mTabItemAdapters;
    private boolean mReCreateScene;
    private boolean mSurfaceSizeChange;
    private boolean mIsFront;

    private final ObservableBoolean isEnterScene = new ObservableBoolean(false);
    private final ObservableBoolean enableMic = new ObservableBoolean(true);
    private final ObservableBoolean enableSpeaker = new ObservableBoolean(true);
    private final ObservableBoolean isBroadcaster = new ObservableBoolean(true);
    private final Observable.OnPropertyChangedCallback callback =
            new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (sender == isEnterScene) {
                        if (MetaChatConstants.SCENE_DRESS == MetaChatContext.getInstance().getCurrentScene()) {
                            binding.cancelBt.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.saveBtn.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.dressTab.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.dressViewpage.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);

                            binding.back.setVisibility(View.GONE);
                            binding.card.getRoot().setVisibility(View.GONE);
                            binding.users.setVisibility(View.GONE);
                            binding.mic.setVisibility(View.GONE);
                            binding.speaker.setVisibility(View.GONE);
                            binding.dressSetting.setVisibility(View.GONE);
                        }
                        if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
                            binding.back.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.card.getRoot().setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.card.nickname.setText(MetaChatContext.getInstance().getRoleInfo().getName());
                            binding.users.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.mic.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.speaker.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.dressSetting.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);

                            binding.cancelBt.setVisibility(View.GONE);
                            binding.saveBtn.setVisibility(View.GONE);
                            binding.dressTab.setVisibility(View.GONE);
                            binding.dressViewpage.setVisibility(View.GONE);
                        }
                        if (isEnterScene.get()) {
                            MetaChatContext.getInstance().sendRoleDressInfo();
                        }
                    } else if (sender == enableMic) {
                        if (!MetaChatContext.getInstance().enableLocalAudio(enableMic.get())) {
                            return;
                        }
                        binding.mic.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        enableMic.get() ? R.mipmap.microphone_on : R.mipmap.microphone_off,
                                        getTheme()
                                )
                        );
                    } else if (sender == enableSpeaker) {
                        if (!MetaChatContext.getInstance().muteAllRemoteAudioStreams(!enableSpeaker.get())) {
                            return;
                        }
                        binding.speaker.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        enableSpeaker.get() ? R.mipmap.voice_on : R.mipmap.voice_off,
                                        getTheme()
                                )
                        );
                    } else if (sender == isBroadcaster) {
                        if (!MetaChatContext.getInstance().updateRole(isBroadcaster.get() ?
                                Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE)) {
                            return;
                        }
                        binding.card.nickname.setText(MetaChatContext.getInstance().getRoleInfo().getName());
                        binding.card.mode.setText(isBroadcaster.get() ? "语聊模式" : "游客模式");
                        binding.card.tips.setVisibility(isBroadcaster.get() ? View.GONE : View.VISIBLE);
                        binding.card.role.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        isBroadcaster.get() ? R.mipmap.offbtn : R.mipmap.onbtn,
                                        getTheme()
                                )
                        );
                        binding.mic.setVisibility(isBroadcaster.get() ? View.VISIBLE : View.GONE);
                        if (isBroadcaster.get()) enableMic.set(true);
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //just for call setRequestedOrientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        binding = GameActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        resetSceneState();

        isEnterScene.addOnPropertyChangedCallback(callback);
        enableMic.addOnPropertyChangedCallback(callback);
        enableSpeaker.addOnPropertyChangedCallback(callback);
        isBroadcaster.addOnPropertyChangedCallback(callback);
        MetaChatContext.getInstance().registerMetaChatSceneEventHandler(this);
        MetaChatContext.getInstance().registerMetaChatEventHandler(this);
        initUnityView();

        initListener();
    }

    @SuppressLint("CheckResult")
    private void initListener() {
        RxView.clicks(binding.back).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            MetaChatContext.getInstance().setCurrentScene(MetaChatConstants.SCENE_NONE);
            MetaChatContext.getInstance().resetRoleInfo();
            MetaChatContext.getInstance().leaveScene();
        });

        RxView.clicks(binding.cancelBt).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            MetaChatContext.getInstance().cancelRoleDressInfo(MetaChatContext.getInstance().getRoleInfo().getName()
                    , MetaChatContext.getInstance().getRoleInfo().getGender());
            MetaChatContext.getInstance().setNextScene(MetaChatConstants.SCENE_GAME);
            MetaChatContext.getInstance().leaveScene();
        });


        RxView.clicks(binding.saveBtn).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            MetaChatContext.getInstance().saveRoleDressInfo(MetaChatContext.getInstance().getRoleInfo().getName()
                    , MetaChatContext.getInstance().getRoleInfo().getGender());
            MetaChatContext.getInstance().setNextScene(MetaChatConstants.SCENE_GAME);
            MetaChatContext.getInstance().leaveScene();
        });

        RxView.clicks(binding.dressSetting).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            MetaChatContext.getInstance().setNextScene(MetaChatConstants.SCENE_DRESS);
            MetaChatContext.getInstance().leaveScene();
        });

        RxView.clicks(binding.speaker).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            enableSpeaker.set(!enableSpeaker.get());
        });


        RxView.clicks(binding.mic).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            enableMic.set(!enableMic.get());
        });

        RxView.clicks(binding.users).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            Toast.makeText(this, "暂不支持", Toast.LENGTH_LONG)
                    .show();
        });

        RxView.clicks(binding.card.tips).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            if (!isBroadcaster.get()) {
                CustomDialog.showTips(this);
            }
        });

        RxView.clicks(binding.card.mode).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            if (!isBroadcaster.get()) {
                CustomDialog.showTips(this);
            }
        });

        RxView.clicks(binding.card.role).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isBroadcaster.set(!isBroadcaster.get());
        });
    }

    private void initUnityView() {
        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                mReCreateScene = true;
                mSurfaceSizeChange = true;
                maybeCreateScene();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                Log.i(TAG, "onSurfaceTextureSizeChanged");
                mSurfaceSizeChange = true;
                if (MetaChatConstants.SCENE_NONE == MetaChatContext.getInstance().getNextScene()) {
                    maybeCreateScene();
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        binding.unity.addView(mTextureView, 0, layoutParams);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        mReCreateScene = true;
        //just for call setRequestedOrientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        super.onNewIntent(intent);

        maybeCreateScene();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        isEnterScene.removeOnPropertyChangedCallback(callback);
        enableMic.removeOnPropertyChangedCallback(callback);
        enableSpeaker.removeOnPropertyChangedCallback(callback);
        isBroadcaster.removeOnPropertyChangedCallback(callback);
        MetaChatContext.getInstance().registerMetaChatSceneEventHandler(this);
    }

    private void createScene(TextureView tv) {
        Log.i(TAG, "createScene");
        resetSceneState();
        resetViewVisibility();
        MetaChatContext.getInstance().createScene(this, KeyCenter.CHANNEL_ID, tv);
    }

    private void resetViewVisibility() {
        binding.back.setVisibility(View.GONE);
        binding.card.getRoot().setVisibility(View.GONE);
        binding.users.setVisibility(View.GONE);
        binding.mic.setVisibility(View.GONE);
        binding.speaker.setVisibility(View.GONE);
        binding.dressSetting.setVisibility(View.GONE);

        binding.cancelBt.setVisibility(View.GONE);
        binding.saveBtn.setVisibility(View.GONE);
        binding.dressTab.setVisibility(View.GONE);
        binding.dressViewpage.setVisibility(View.GONE);
    }

    @Override
    public void onEnterSceneResult(int errorCode) {
        runOnUiThread(() -> {
            if (errorCode != 0) {
                Toast.makeText(this, String.format(Locale.getDefault(), "EnterSceneFailed %d", errorCode), Toast.LENGTH_LONG).show();
                return;
            }
            isEnterScene.set(true);
            enableMic.set(true);
            enableSpeaker.set(true);
            isBroadcaster.set(true);
        });
        resetSceneState();
    }

    @Override
    public void onLeaveSceneResult(int errorCode) {
        runOnUiThread(() -> {
            isEnterScene.set(false);
        });
    }

    @Override
    public void onReleasedScene(int status) {
        if (status == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MetaChatContext.getInstance().destroy();
                }
            });

            MetaChatContext.getInstance().setCurrentScene(MetaChatConstants.SCENE_NONE);
            if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT != getRequestedOrientation()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    @Override
    public void onRecvMessageFromScene(byte[] message) {

    }

    @Override
    public void onUserPositionChanged(String uid, MetachatUserPositionInfo posInfo) {

    }

    @Override
    public void onEnumerateVideoDisplaysResult(String[] displayIds) {

    }

    @Override
    public void onCreateSceneResult(IMetachatScene scene, int errorCode) {
        //异步线程回调需在主线程处理
        runOnUiThread(() -> MetaChatContext.getInstance().enterScene());
    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {

    }

    @Override
    public void onRequestToken() {

    }

    @Override
    public void onGetSceneInfosResult(MetachatSceneInfo[] scenes, int errorCode) {

    }

    @Override
    public void onDownloadSceneProgress(long SceneId, int progress, int state) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        mIsFront = true;
        if (MetaChatContext.getInstance().isInScene()) {
            MetaChatContext.getInstance().resumeMedia();
        }
        if (MetaChatConstants.SCENE_DRESS == MetaChatContext.getInstance().getCurrentScene()) {
            initDressTab();
        }
        maybeCreateScene();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        mIsFront = false;
        //切换场景时候surface变更状态保留，重新进场景等待surface变更状态
        if (MetaChatContext.getInstance().getNextScene() == MetaChatConstants.SCENE_NONE) {
            mSurfaceSizeChange = false;
        }
        if (MetaChatContext.getInstance().isInScene()) {
            MetaChatContext.getInstance().pauseMedia();
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void initDressTab() {
        getLayoutInflater();
        LayoutInflater lf = LayoutInflater.from(this);
        Map<View, String> viewDressTypeMap = new LinkedHashMap<>();

        mTabEntities.clear();
        mCurrentTabIndex = 0;
        View view;
        if (MetaChatConstants.GENDER_WOMEN == MetaChatContext.getInstance().getRoleInfo().getGender()) {
            for (TabEntity tabEntity : SkinsData.TAB_ENTITY_WOMEN) {
                mTabEntities.add(tabEntity);

                view = lf.inflate(R.layout.viewpager_skin_layout, null);
                viewDressTypeMap.put(view, tabEntity.getDressType());
            }

        } else {
            for (TabEntity tabEntity : SkinsData.TAB_ENTITY_MAN) {
                mTabEntities.add(tabEntity);
                view = lf.inflate(R.layout.viewpager_skin_layout, null);
                viewDressTypeMap.put(view, tabEntity.getDressType());
            }
        }


        binding.dressTab.setTabData(mTabEntities);
        binding.dressTab.setCurrentTab(mCurrentTabIndex);
        binding.dressTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mCurrentTabIndex = position;
                binding.dressViewpage.setCurrentItem(mCurrentTabIndex);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        mTabItemAdapters = new ArrayList<>(mTabEntities.size());

        for (Map.Entry<View, String> entry : viewDressTypeMap.entrySet()) {
            initGridView(entry.getKey(), entry.getValue());
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this.getApplicationContext(), new ArrayList<>(viewDressTypeMap.keySet()));
        binding.dressViewpage.setAdapter(viewPagerAdapter);
        binding.dressViewpage.setScrollable(false);

        binding.dressViewpage.setCurrentItem(mCurrentTabIndex);

        binding.dressViewpage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected position=" + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * 加载tab下的gridview控件
     */
    private void initGridView(View view, String viewDressType) {
        List<SkinInfo> list = SkinsData.getDressInfo(viewDressType);
        if (null == list) {
            return;
        }

        List<SkinInfo> dataList = new ArrayList<>(list);

        int selectIndex = 0;
        RoleInfo roleInfo = MetaChatContext.getInstance().getRoleInfo();
        if (null != roleInfo) {
            if (SkinsData.KEY_WOMEN_CLOTHING.equalsIgnoreCase(viewDressType) ||
                    SkinsData.KEY_MAN_CLOTHING.equalsIgnoreCase(viewDressType)) {
                selectIndex = roleInfo.getTops() - 1;
            } else if (SkinsData.KEY_WOMEN_HAIRPIN.equalsIgnoreCase(viewDressType) ||
                    SkinsData.KEY_MAN_HAIRPIN.equalsIgnoreCase(viewDressType)) {
                selectIndex = roleInfo.getHair() - 1;
            } else if (SkinsData.KEY_WOMEN_SHOES.equalsIgnoreCase(viewDressType) ||
                    SkinsData.KEY_MAN_SHOES.equalsIgnoreCase(viewDressType)) {
                selectIndex = roleInfo.getShoes() - 1;
            } else if (SkinsData.KEY_WOMEN_TROUSERS.equalsIgnoreCase(viewDressType) ||
                    SkinsData.KEY_MAN_TROUSERS.equalsIgnoreCase(viewDressType)) {
                selectIndex = roleInfo.getLower() - 1;
            }
            if (selectIndex < 0) {
                selectIndex = 0;
            }
        }


        ViewPager mCurrentTabViewPager = view.findViewById(R.id.viewpage_skin_item);
        CirclePageIndicator indicator = (CirclePageIndicator) view.findViewById(R.id.indicator);

        //总的页数=总数/每页数量，并向上取整取整
        int totalPage = (int) Math.ceil(dataList.size() * 1.0 / SKIN_TAB_MAX_PAGE_SIZE);
        List<View> mViewPagerList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            dataList.get(i).setCheck(i == selectIndex);
        }
        SkinGridViewAdapter adapter;
        for (int i = 0; i < totalPage; i++) {
            View pagerView = getLayoutInflater().inflate(R.layout.skin_gridview, null);
            //初始化gridview的控件并绑定
            final GridView gridView = pagerView.findViewById(R.id.gridview);
            adapter = new SkinGridViewAdapter(this, dataList, i, SKIN_TAB_MAX_PAGE_SIZE, this);
            gridView.setAdapter(adapter);
            //每一个GridView作为一个View对象添加到ViewPager集合中
            mViewPagerList.add(gridView);
            mTabItemAdapters.add(adapter);
        }
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(this, mViewPagerList);
        mCurrentTabViewPager.setAdapter(mViewPagerAdapter);
        indicator.setViewPager(mCurrentTabViewPager);
    }

    @Override
    public void onSkinItemClick(int position) {
        for (SkinGridViewAdapter adapter : mTabItemAdapters) {
            adapter.notifyDataSetChanged();
        }

        TabEntity tabEntity = (TabEntity) mTabEntities.get(mCurrentTabIndex);
        RoleInfo roleInfo = MetaChatContext.getInstance().getRoleInfo();
        if (null != roleInfo) {
            if (SkinsData.KEY_WOMEN_CLOTHING.equalsIgnoreCase(tabEntity.getDressType()) ||
                    SkinsData.KEY_MAN_CLOTHING.equalsIgnoreCase(tabEntity.getDressType())) {
                roleInfo.setTops(position + 1);
            }
            if (SkinsData.KEY_WOMEN_HAIRPIN.equalsIgnoreCase(tabEntity.getDressType()) ||
                    SkinsData.KEY_MAN_HAIRPIN.equalsIgnoreCase(tabEntity.getDressType())) {
                roleInfo.setHair(position + 1);
            }

            if (SkinsData.KEY_WOMEN_TROUSERS.equalsIgnoreCase(tabEntity.getDressType()) ||
                    SkinsData.KEY_MAN_TROUSERS.equalsIgnoreCase(tabEntity.getDressType())) {
                roleInfo.setLower(position + 1);
            }

            if (SkinsData.KEY_WOMEN_SHOES.equalsIgnoreCase(tabEntity.getDressType()) ||
                    SkinsData.KEY_MAN_SHOES.equalsIgnoreCase(tabEntity.getDressType())) {
                roleInfo.setShoes(position + 1);
            }

            MetaChatContext.getInstance().sendRoleDressInfo();
        }
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (MetaChatConstants.SCENE_DRESS == MetaChatContext.getInstance().getCurrentScene()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        super.setRequestedOrientation(requestedOrientation);
    }

    private void maybeCreateScene() {
        Log.i(TAG, "maybeCreateScene,mReCreateScene=" + mReCreateScene + ",mSurfaceSizeChange=" + mSurfaceSizeChange + ",mIsFront=" + mIsFront);
        if (mReCreateScene && mSurfaceSizeChange && mIsFront) {
            createScene(mTextureView);
        }
    }

    private void resetSceneState() {
        mReCreateScene = false;
        mSurfaceSizeChange = false;
    }
}
