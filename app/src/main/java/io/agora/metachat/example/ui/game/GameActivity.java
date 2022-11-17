package io.agora.metachat.example.ui.game;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import java.util.ArrayList;
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
import io.agora.metachat.example.models.RoleInfo;
import io.agora.metachat.example.models.SkinInfo;
import io.agora.metachat.example.models.TabEntity;
import io.agora.metachat.example.ui.view.CirclePageIndicator;
import io.agora.metachat.example.utils.MetaChatConstants;
import io.agora.rtc2.Constants;

public class GameActivity extends Activity implements View.OnClickListener, IMetachatSceneEventHandler, IMetachatEventHandler, SkinGridViewAdapter.SkinItemClick {

    private final String TAG = GameActivity.class.getSimpleName();
    private GameActivityBinding binding;
    private TextureView mTextureView = null;

    private String nickname;
    private int gender;
    private final ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private static final int SKIN_TAB_MAX_PAGE_SIZE = 8;
    private int mCurrentTabIndex;
    private ViewPager mCurrentTabViewPager;
    private List<SkinGridViewAdapter> mTabItemAdapters;

    private int mScreenWidth;
    private int mScreenHeight;

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
                        }
                        if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
                            binding.back.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.card.getRoot().setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.users.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.mic.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                            binding.speaker.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);

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

        if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE != getRequestedOrientation()) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            mScreenHeight = dm.widthPixels;
            mScreenWidth = dm.heightPixels;
        } else {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            mScreenWidth = dm.widthPixels;
            mScreenHeight = dm.heightPixels;
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
                MetaChatContext.getInstance().setCurrentScene(MetaChatConstants.SCENE_NONE);
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
                MetaChatContext.getInstance().setCurrentScene(MetaChatConstants.SCENE_DRESS);
                MetaChatContext.getInstance().leaveScene();
                break;
            case R.id.mic:
                enableMic.set(!enableMic.get());
                break;
            case R.id.speaker:
                enableSpeaker.set(!enableSpeaker.get());
                break;
            case R.id.cancel_bt:
                MetaChatContext.getInstance().cancelRoleDressInfo(nickname, gender);
                MetaChatContext.getInstance().setCurrentScene(MetaChatConstants.SCENE_GAME);
                MetaChatContext.getInstance().leaveScene();
                break;
            case R.id.save_btn:
                MetaChatContext.getInstance().saveRoleDressInfo(nickname, gender);
                MetaChatContext.getInstance().setCurrentScene(MetaChatConstants.SCENE_GAME);
                MetaChatContext.getInstance().leaveScene();
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
        if (MetaChatConstants.SCENE_DRESS == MetaChatContext.getInstance().getCurrentScene()) {
            if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT != getRequestedOrientation()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else if (MetaChatConstants.SCENE_GAME == MetaChatContext.getInstance().getCurrentScene()) {
            if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE != getRequestedOrientation()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
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

        mTabEntities.clear();
        mCurrentTabIndex = 0;
        View view;
        if (MetaChatConstants.GENDER_WOMEN == gender) {
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
            }
            if (selectIndex < 0) {
                selectIndex = 0;
            }
        }


        mCurrentTabViewPager = (ViewPager) view.findViewById(R.id.viewpage_skin_item);
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
            final GridView gridView = (GridView) pagerView.findViewById(R.id.gridview);
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

            MetaChatContext.getInstance().sendRoleDressInfo();
        }
    }

/*    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }*/
}
