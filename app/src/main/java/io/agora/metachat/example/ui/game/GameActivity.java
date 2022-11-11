package io.agora.metachat.example.ui.game;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.unity3d.splash.services.core.lifecycle.LifecycleListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import coil.ImageLoaders;
import coil.request.ImageRequest;
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
import io.agora.metachat.example.models.SkinInfo;
import io.agora.metachat.example.models.TabEntity;
import io.agora.metachat.example.ui.view.CirclePageIndicator;
import io.agora.metachat.example.utils.MetaChatConstants;
import io.agora.rtc2.Constants;

public class GameActivity extends Activity implements View.OnClickListener, IMetachatSceneEventHandler, IMetachatEventHandler {

    private GameActivityBinding binding;
    private TextureView mTextureView = null;

    private String nickname;
    private int gender;
    private final ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private static final int SKIN_PAGE_SIZE = 8;

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
                            binding.dressTl.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.viewpageTab.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);

                            binding.back.setVisibility(View.GONE);
                            binding.card.getRoot().setVisibility(View.GONE);
                            binding.users.setVisibility(View.GONE);
                            binding.mic.setVisibility(View.GONE);
                            binding.speaker.setVisibility(View.GONE);
                        }
                        if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
                            binding.back.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.card.getRoot().setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.users.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.mic.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.speaker.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);

                            binding.cancelBt.setVisibility(View.GONE);
                            binding.saveBtn.setVisibility(View.GONE);
                            binding.dressTl.setVisibility(View.GONE);
                            binding.viewpageTab.setVisibility(View.GONE);
                        }
                        if (isEnterScene.get()) {
                            SkinsData.initSkinsData(nickname, gender);
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
        super.onCreate(savedInstanceState);
        binding = GameActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isEnterScene.addOnPropertyChangedCallback(callback);
        enableMic.addOnPropertyChangedCallback(callback);
        enableSpeaker.addOnPropertyChangedCallback(callback);
        isBroadcaster.addOnPropertyChangedCallback(callback);
        MetaChatContext.getInstance().registerMetaChatSceneEventHandler(this);
        MetaChatContext.getInstance().registerMetaChatEventHandler(this);
        //initUnity();
        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                refreshByIntent(getIntent(), mTextureView);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });
        FrameLayout localView = (FrameLayout) findViewById(R.id.unity);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-1, -1);
        localView.addView(mTextureView, 0, layoutParams);


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE != getRequestedOrientation()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        refreshByIntent(intent, mTextureView);
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

    private void refreshByIntent(Intent intent, TextureView tv) {
        nickname = intent.getStringExtra("nickname");
        if (nickname != null) {
            binding.card.nickname.setText(nickname);
        }

        String avatar = intent.getStringExtra("avatar");
        if (avatar != null) {
            ImageRequest request = new ImageRequest.Builder(this)
                    .data(avatar)
                    .target(binding.card.avatar)
                    .build();
            ImageLoaders.create(this)
                    .enqueue(request);
        }

        gender = intent.getIntExtra("gender", MetaChatConstants.GENDER_MAN);

        String roomName = intent.getStringExtra("roomName");
        if (roomName != null) {
            MetaChatContext.getInstance().createScene(this, roomName, tv);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                MetaChatContext.getInstance().leaveScene();
                break;
            case R.id.mode:
            case R.id.tips:
                if (!isBroadcaster.get()) {
                    CustomDialog.showTips(this);
                }
                break;
            case R.id.role:
                isBroadcaster.set(!isBroadcaster.get());
                break;
            case R.id.users:
                Toast.makeText(this, "暂不支持", Toast.LENGTH_LONG)
                        .show();
                break;
            case R.id.mic:
                enableMic.set(!enableMic.get());
                break;
            case R.id.speaker:
                enableSpeaker.set(!enableSpeaker.get());
                break;
        }
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
    }

    @Override
    public void onLeaveSceneResult(int errorCode) {
    }

    @Override
    public void onReleasedScene(int status) {
        if (status == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MetaChatContext.getInstance().destroy();
                    isEnterScene.set(false);
                }
            });


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
    public void onCreateSceneResult(IMetachatScene scene, int errorCode) {
        //异步线程回调需在主线程处理
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MetaChatContext.getInstance().enterScene();
            }
        });
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
        if (MetaChatContext.getInstance().isInScene()) {
            MetaChatContext.getInstance().resumeMedia();
        }
        if (MetaChatConstants.SCENE_DRESS == MetaChatContext.getInstance().getCurrentScene()) {
            initDressTab();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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


        View view;
        if (MetaChatConstants.GENDER_WOMEN == gender) {
            for (Map.Entry<String, TabEntity> entityEntry : SkinsData.TAB_ENTITY_WOMEN.entrySet()) {
                mTabEntities.add(entityEntry.getValue());

                view = lf.inflate(R.layout.viewpager_skin_layout, null);
                viewDressTypeMap.put(view, entityEntry.getKey());
            }

        } else {
            for (Map.Entry<String, TabEntity> entityEntry : SkinsData.TAB_ENTITY_MAN.entrySet()) {
                mTabEntities.add(entityEntry.getValue());
                view = lf.inflate(R.layout.viewpager_skin_layout, null);
                viewDressTypeMap.put(view, entityEntry.getKey());
            }
        }
        binding.dressTl.setTabData(mTabEntities);

        binding.viewpageTab.setScrollable(false);


        SkinsData.initSkinsData(nickname, gender);

        for (Map.Entry<View, String> entry : viewDressTypeMap.entrySet()) {
            initGridView(entry.getKey(), entry.getValue());
        }

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this.getApplicationContext(), new ArrayList<>(viewDressTypeMap.keySet()));
        binding.viewpageTab.setAdapter(viewPagerAdapter);


        binding.dressTl.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                binding.viewpageTab.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
                if (position == 0) {
//                    Random mRandom = new Random();
//                    mTabLayout.showMsg(0, mRandom.nextInt(100) + 1);
//                    UnreadMsgUtils.show(mTabLayout_2.getMsgView(0), mRandom.nextInt(100) + 1);
                }
            }
        });

        binding.viewpageTab.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                binding.dressTl.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        binding.viewpageTab.setCurrentItem(0);
    }

    /**
     * 加载tab下的gridview控件
     */
    private void initGridView(View view, String viewDressType) {
        List<SkinInfo> list = SkinsData.getDressInfo(viewDressType);
        if (null == list) {
            return;
        }


        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.viewpage_skin_item);
        CirclePageIndicator indicator = (CirclePageIndicator) view.findViewById(R.id.indicator);

        //总的页数=总数/每页数量，并向上取整取整
        int mTotalPage = (int) Math.ceil(list.size() * 1.0 / SKIN_PAGE_SIZE);
        List<View> mViewPagerList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setCheck(false);
        }
        for (int i = 0; i < mTotalPage; i++) {
            View pagerView = getLayoutInflater().inflate(R.layout.skin_gridview, null);
            //初始化gridview的控件并绑定
            final GridView gridView = (GridView) pagerView.findViewById(R.id.gridview);
//            final GridView gridView = (GridView) View.inflate(this, R.layout.gridview, null);
            SkinGridViewAdapter adapter = new SkinGridViewAdapter(this, list, i, SKIN_PAGE_SIZE);
            gridView.setAdapter(adapter);
            //每一个GridView作为一个View对象添加到ViewPager集合中
            mViewPagerList.add(gridView);
        }
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(this, mViewPagerList);
        mViewPager.setAdapter(mViewPagerAdapter);
        indicator.setViewPager(mViewPager);
    }
}
