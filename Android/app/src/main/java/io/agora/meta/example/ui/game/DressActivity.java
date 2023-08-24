package io.agora.meta.example.ui.game;


import android.annotation.SuppressLint;
import android.content.Intent;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jakewharton.rxbinding2.view.RxView;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import io.agora.meta.example.MainActivity;
import io.agora.meta.example.adapter.DressTypeAdapter;
import io.agora.meta.example.adapter.DressTypeAssetAdapter;
import io.agora.meta.example.adapter.FaceTypeAdapter;
import io.agora.meta.example.adapter.FaceTypeShapesAdapter;
import io.agora.meta.example.databinding.DressActivityBinding;
import io.agora.meta.example.meta.MetaContext;
import io.agora.meta.example.models.FaceParameterItem;
import io.agora.meta.example.models.manifest.DressItemResource;
import io.agora.meta.example.models.manifest.FaceBlendShape;
import io.agora.meta.example.models.manifest.FaceBlendShapeItem;
import io.agora.meta.example.utils.DressAndFaceDataUtils;
import io.agora.meta.example.utils.KeyCenter;
import io.agora.meta.example.utils.MetaConstants;


public class DressActivity extends BaseGameActivity {

    private final String TAG = DressActivity.class.getSimpleName();
    private DressActivityBinding binding;

    private DressTypeAdapter mDressTypeAdapter;
    private DressTypeAssetAdapter mDressTypeAssetAdapter;
    private List<DressItemResource> mDressResourceDataList;

    private DressItemResource mCurrentDressItemResource;

    private FaceTypeAdapter mFaceTypeAdapter;
    private FaceTypeShapesAdapter mFaceTypeShapesAdapter;
    private List<FaceBlendShape> mFaceBlendShapeDataList;
    private FaceBlendShape mCurrentFaceBlendShape;

    private int mViewMode = 0;

    private final ObservableBoolean isEnterScene = new ObservableBoolean(false);
    private final Observable.OnPropertyChangedCallback callback =
            new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (sender == isEnterScene) {
                        binding.group.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                        if (isEnterScene.get()) {
                            MetaContext.getInstance().sendRoleDressInfo(Arrays.stream(MetaContext.getInstance().getRoleInfo().getDressResourceMap().values().toArray(new Integer[0])).mapToInt(Integer::valueOf).toArray());
                        }
                    }
                }
            };

    @Override
    protected void initLayout() {
        super.initLayout();

        binding = DressActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }


    @Override
    public void initView() {
        super.initView();
        binding.uidTv.setText("UID:" + KeyCenter.RTC_UID);
    }

    @Override
    protected void registerListener() {
        super.registerListener();
        isEnterScene.addOnPropertyChangedCallback(callback);
    }

    @Override
    protected void unregisterListener() {
        super.unregisterListener();
        isEnterScene.removeOnPropertyChangedCallback(callback);
    }

    @Override
    @SuppressLint("CheckResult")
    protected void initClickEvent() {
        super.initClickEvent();

        RxView.clicks(binding.cancelBt).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isEnterScene.set(false);
            exit();
            MetaContext.getInstance().cancelRoleDressInfo(MetaContext.getInstance().getRoleInfo().getName());
            MetaContext.getInstance().leaveScene();
        });


        RxView.clicks(binding.saveBtn).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            isEnterScene.set(false);
            exit();
            MetaContext.getInstance().saveRoleDressInfo(MetaContext.getInstance().getRoleInfo().getName());
            MetaContext.getInstance().leaveScene();
        });


        RxView.clicks(binding.dressSettingBt).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            binding.dressSettingLayout.setVisibility(View.VISIBLE);
            binding.faceSettingLayout.setVisibility(View.GONE);
        });

        RxView.clicks(binding.faceSettingBt).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(o -> {
            binding.dressSettingLayout.setVisibility(View.GONE);
            binding.faceSettingLayout.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void initMainUnityView() {
        super.initMainUnityView();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        binding.unity.addView(mTextureView, 0, layoutParams);

        ConstraintLayout.LayoutParams unityLayoutParams = (ConstraintLayout.LayoutParams) binding.unity.getLayoutParams();
        unityLayoutParams.bottomMargin = 0;
        binding.unity.setLayoutParams(unityLayoutParams);
    }

    @Override
    protected void resetCreateSceneState() {
        super.resetCreateSceneState();
        resetSceneState();
        resetSceneView();
        initDressAndFaceData();
    }

    private void resetSceneView() {
        binding.group.setVisibility(View.GONE);
    }

    private void resetSceneState() {
        mViewMode = 0;
    }

    @Override
    public void onEnterSceneResult(int errorCode) {
        runOnUiThread(() -> {
            if (errorCode != 0) {
                Toast.makeText(this, String.format(Locale.getDefault(), "EnterSceneFailed %d", errorCode), Toast.LENGTH_LONG).show();
                return;
            }
            isEnterScene.set(true);

            initDressTypeView();
            initFaceTypeView();
            updateViewMode();

            MetaContext.getInstance().enableSceneVideo(mTextureView, true);
        });
        resetSceneState();
    }


    @Override
    public void onReleasedScene(int status) {
        super.onReleasedScene(status);
        if (status == 0) {
            Intent intent = new Intent(DressActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    private void initDressAndFaceData() {
        if (MetaContext.getInstance().getCurrentScene() == MetaConstants.SCENE_DRESS) {
            DressItemResource[] dressItemResources = DressAndFaceDataUtils.getInstance().getDressResources(MetaContext.getInstance().getRoleInfo().getAvatarModelName());
            if (null != dressItemResources) {
                mDressResourceDataList = Arrays.asList(dressItemResources);
            }

            FaceBlendShape[] faceBlendShapes = DressAndFaceDataUtils.getInstance().getFaceBlendShapes(MetaContext.getInstance().getRoleInfo().getAvatarModelName());
            if (null != faceBlendShapes) {
                mFaceBlendShapeDataList = Arrays.asList(faceBlendShapes);
            }
        }
    }

    private void initDressTypeView() {
        if (MetaConstants.SCENE_DRESS != MetaContext.getInstance().getCurrentScene()) {
            return;
        }

        if (null == mDressResourceDataList || mDressResourceDataList.size() == 0) {
            Log.i(TAG, "dress resource list is null");
            return;
        }

        mCurrentDressItemResource = mDressResourceDataList.get(0);
        if (mDressTypeAdapter == null) {
            mDressTypeAdapter = new DressTypeAdapter(getApplicationContext());

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            binding.rvDressType.setLayoutManager(linearLayoutManager);
            binding.rvDressType.setAdapter(mDressTypeAdapter);
            mDressTypeAdapter.setOnItemClickCallBack(new DressTypeAdapter.OnItemClickCallBack() {
                @Override
                public void onItemClick(DressItemResource dressItemResource) {
                    mCurrentDressItemResource = dressItemResource;
                    if (null != mDressTypeAssetAdapter) {
                        setDressTypeAssetData(Arrays.asList(Arrays.stream(mCurrentDressItemResource.getAssets()).boxed().toArray(Integer[]::new)));
                    }
                }
            });
        }
        mDressTypeAdapter.setDataList(mDressResourceDataList);


        String iconFilePath = DressAndFaceDataUtils.getInstance().getIconFilePath(MetaContext.getInstance().getRoleInfo().getAvatarModelName());
        if (!TextUtils.isEmpty(iconFilePath)) {
            File file = new File(iconFilePath);
            Map<Integer, String> assetMap = null;
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (null != files) {
                    assetMap = new HashMap<>(files.length);
                    for (File tempFile : files) {
                        try {
                            int assetId = Integer.parseInt(tempFile.getName().substring(0, tempFile.getName().lastIndexOf(".")));
                            assetMap.put(assetId, tempFile.getPath());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (null != assetMap) {
                if (mDressTypeAssetAdapter == null) {
                    mDressTypeAssetAdapter = new DressTypeAssetAdapter(getApplicationContext());
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
                    binding.rvDressTypeAsset.setLayoutManager(gridLayoutManager);
                    binding.rvDressTypeAsset.setAdapter(mDressTypeAssetAdapter);

                    mDressTypeAssetAdapter.setOnItemClickCallBack(new DressTypeAssetAdapter.OnItemClickCallBack() {
                        @Override
                        public void onItemClick(int resId) {
                            MetaContext.getInstance().getRoleInfo().updateDressResource(mCurrentDressItemResource.getId(), resId);
                            MetaContext.getInstance().sendRoleDressInfo(new int[]{resId});
                        }
                    });

                }
                mDressTypeAssetAdapter.setAssetMap(assetMap);
                setDressTypeAssetData(Arrays.asList(Arrays.stream(mCurrentDressItemResource.getAssets()).boxed().toArray(Integer[]::new)));
            }
        }
    }

    private void setDressTypeAssetData(List<Integer> list) {
        Integer resId = MetaContext.getInstance().getRoleInfo().getDressResourceMap().get(mCurrentDressItemResource.getId());
        if (null != mDressTypeAssetAdapter) {
            if (null == resId) {
                resId = -1;
            } else {
                if (!list.contains(resId)) {
                    resId = -1;
                }
            }
            mDressTypeAssetAdapter.setDataList(list, resId);
            binding.rvDressTypeAsset.scrollToPosition(0);

        }
    }

    private void initFaceTypeView() {
        if (MetaConstants.SCENE_DRESS != MetaContext.getInstance().getCurrentScene()) {
            return;
        }

        if (null == mFaceBlendShapeDataList || mFaceBlendShapeDataList.size() == 0) {
            Log.i(TAG, "face data list is null");
            return;
        }

        mCurrentFaceBlendShape = mFaceBlendShapeDataList.get(0);
        if (mFaceTypeAdapter == null) {
            mFaceTypeAdapter = new FaceTypeAdapter(getApplicationContext());
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            binding.rvFaceType.setLayoutManager(linearLayoutManager);
            binding.rvFaceType.setAdapter(mFaceTypeAdapter);
            mFaceTypeAdapter.setOnItemClickCallBack(new FaceTypeAdapter.OnItemClickCallBack() {
                @Override
                public void onItemClick(FaceBlendShape faceBlendShape) {
                    mCurrentFaceBlendShape = faceBlendShape;
                    if (null != mFaceTypeShapesAdapter) {
                        setFaceTypeShapesData(Arrays.asList(mCurrentFaceBlendShape.getShapes()));
                    }
                }
            });
        }
        mFaceTypeAdapter.setDataList(mFaceBlendShapeDataList);


        if (mFaceTypeShapesAdapter == null) {
            mFaceTypeShapesAdapter = new FaceTypeShapesAdapter(getApplicationContext());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
            binding.rvFaceTypeShapes.setLayoutManager(gridLayoutManager);
            binding.rvFaceTypeShapes.setAdapter(mFaceTypeShapesAdapter);

            mFaceTypeShapesAdapter.setOnShapeChangeCallBack(new FaceTypeShapesAdapter.OnShapeChangeCallBack() {
                @Override
                public void onShapeChange(FaceBlendShapeItem faceBlendShapeItem, int value) {
                    MetaContext.getInstance().getRoleInfo().updateFaceParameter(faceBlendShapeItem.getKey(), value);
                    MetaContext.getInstance().sendRoleFaceInfo(new FaceParameterItem[]{new FaceParameterItem(faceBlendShapeItem.getKey(), value)});
                }
            });
        }
        setFaceTypeShapesData(Arrays.asList(mCurrentFaceBlendShape.getShapes()));
    }

    private void setFaceTypeShapesData(List<FaceBlendShapeItem> list) {
        if (null != mFaceTypeShapesAdapter) {
            mFaceTypeShapesAdapter.setDataList(list, MetaContext.getInstance().getRoleInfo().getFaceParameterResourceMap());
            binding.rvFaceTypeShapes.scrollToPosition(0);
        }
    }
}
