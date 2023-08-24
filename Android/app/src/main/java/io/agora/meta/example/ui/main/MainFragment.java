package io.agora.meta.example.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import coil.ImageLoaders;
import coil.request.ImageRequest;
import io.agora.meta.example.databinding.MainFragmentBinding;
import io.agora.meta.example.dialog.CustomDialog;
import io.agora.meta.example.ui.game.DressActivity;
import io.agora.meta.example.ui.game.VoiceChatActivity;
import io.agora.meta.example.utils.DressAndFaceDataUtils;
import io.agora.meta.example.meta.MetaContext;
import io.agora.meta.example.R;
import io.agora.meta.example.adapter.SpinnerAdapter;
import io.agora.meta.example.ui.game.CoffeeActivity;
import io.agora.meta.example.utils.MetaConstants;
import io.agora.rtc2.Constants;

public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private MainViewModel mViewModel;
    private MainFragmentBinding binding;
    private int downloadProgress;
    private boolean isFront;
    private String avatarModelName;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @SuppressLint("CheckResult")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = MainFragmentBinding.inflate(inflater, container, false);

        SpinnerAdapter avatarModelAdapter = new SpinnerAdapter(requireContext(), Objects.requireNonNull(getContext()).getResources().getStringArray(R.array.avatar_model));
        binding.avatarModelSpinner.setAdapter(avatarModelAdapter);
        binding.avatarModelSpinner.setSelection(0);
        binding.avatarModelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                avatarModelAdapter.check(position);
                binding.avatarModel.setText(Objects.requireNonNull(getContext()).getResources().getStringArray(R.array.avatar_model)[position]);
                avatarModelName = Objects.requireNonNull(getContext()).getResources().getStringArray(R.array.avatar_model_value)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        SpinnerAdapter sceneAdapter = new SpinnerAdapter(requireContext(), Objects.requireNonNull(getContext()).getResources().getStringArray(R.array.scene));
        binding.sceneSpinner.setAdapter(sceneAdapter);
        binding.sceneSpinner.setSelection(0);
        binding.sceneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                sceneAdapter.check(position);
                try {
                    binding.scene.setText(Objects.requireNonNull(getContext()).getResources().getStringArray(R.array.scene)[position]);
                    MetaContext.getInstance().setCurrentScene(Integer.parseInt(Objects.requireNonNull(getContext()).getResources().getStringArray(R.array.scene_value)[position]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //防止多次频繁点击异常处理
        RxView.clicks(binding.enter).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            if (MetaConstants.SCENE_COFFEE == MetaContext.getInstance().getCurrentScene()) {
                if (checkCameraPermission()) {
                    enter();
                }
            } else {
                enter();
            }
        });

        RxView.clicks(binding.avatar).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            CustomDialog.showAvatarPicker(requireContext(), charSequence -> {
                mViewModel.setAvatar(charSequence.toString());
                return null;
            }, null, null);
        });

        initData();
        return binding.getRoot();
    }

    private boolean checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "requireActivity().checkSelfPermission(Manifest.permission.CAMERA):" + requireActivity().checkSelfPermission(Manifest.permission.CAMERA));
            if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予了相机权限，可以继续使用相机
                enter();
            } else {
                // 用户拒绝了相机权限，您可以根据需要采取适当的操作，例如显示一个提示消息或关闭相机功能
            }
        }
    }

    private void enter() {
        if (TextUtils.isEmpty(binding.nickname.getText().toString())) {
            Toast.makeText(requireActivity(), "请输入昵称", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(binding.roomName.getText().toString())) {
            Toast.makeText(requireActivity(), "请输入房间名称", Toast.LENGTH_LONG).show();
        } else {
            MetaContext.getInstance().initRoleInfo(getContext(), binding.nickname.getText().toString(), avatarModelName, mViewModel.getAvatar().getValue());
            MetaContext.getInstance().setRoomName(binding.roomName.getText().toString());
            mViewModel.getScenes();
        }
    }

    private void initData() {
        downloadProgress = -1;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        LifecycleOwner owner = getViewLifecycleOwner();
        Context context = requireContext();
        mViewModel.getAvatar().observe(owner, charSequence -> {
            ImageRequest request = new ImageRequest.Builder(context)
                    .data(charSequence)
                    .target(binding.avatar)
                    .build();
            ImageLoaders.create(context)
                    .enqueue(request);
        });

        mViewModel.getSceneList().observe(owner, metaSceneAssetsInfos -> {
            // TODO choose one
            if (metaSceneAssetsInfos.size() > 0) {
                for (int a = 0; a < metaSceneAssetsInfos.size(); a++) {
                    if (metaSceneAssetsInfos.get(a).getSceneId() == MetaContext.getInstance().getSceneId()) {
                        mViewModel.prepareScene(metaSceneAssetsInfos.get(a));
                        break;
                    }
                }
            }
        });
        mViewModel.getSelectScene().observe(owner, sceneInfo -> {
            if (!MetaContext.getInstance().isInitMeta()) {
                return;
            }

            if (-1 != downloadProgress) {
                downloadProgress = -1;
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            DressAndFaceDataUtils.getInstance().initData(MetaContext.getInstance().getScenePath() + "/" + MetaContext.getInstance().getSceneId());

            Class cls = DressActivity.class;
            switch (MetaContext.getInstance().getCurrentScene()) {
                case MetaConstants.SCENE_DRESS:
                    cls = DressActivity.class;
                    break;
                case MetaConstants.SCENE_COFFEE:
                    cls = CoffeeActivity.class;
                    break;
                case MetaConstants.SCENE_VOICE_CHAT:
                case MetaConstants.SCENE_FACE_CAPTURE_CHAT:
                    cls = VoiceChatActivity.class;
                    break;
                default:
                    break;
            }
            Intent intent = new Intent(context, cls);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        mViewModel.getRequestDownloading().observe(owner, aBoolean -> {
            if (!MetaContext.getInstance().isInitMeta()) {
                return;
            }
            if (aBoolean) {
                downloadingChooserDialog = CustomDialog.showDownloadingChooser(context, materialDialog -> {
                    mViewModel.downloadScene(MetaContext.getInstance().getSceneInfo());

                    return null;
                }, null);
            }
        });
        mViewModel.getDownloadingProgress().observe(owner, integer -> {
            if (!MetaContext.getInstance().isInitMeta()) {
                return;
            }
            if (integer >= 0) {
                downloadProgress = integer;
            }
            if (progressDialog == null) {
                progressDialog = CustomDialog.showDownloadingProgress(context, materialDialog -> {
                    downloadProgress = -1;
                    mViewModel.cancelDownloadScene(MetaContext.getInstance().getSceneInfo());
                    return null;
                });
            } else if (integer < 0) {
                if (isFront) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                return;
            }

            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }

            ConstraintLayout constraintLayout = CustomDialog.getCustomView(progressDialog);
            ProgressBar progressBar = constraintLayout.findViewById(R.id.progressBar);
            TextView textView = constraintLayout.findViewById(R.id.textView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(integer, true);
            }
            textView.setText(String.format(Locale.getDefault(), "%d%%", integer));
        });
    }

    private MaterialDialog progressDialog;
    private MaterialDialog downloadingChooserDialog;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!MetaContext.getInstance().isInitMeta() && progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if (!MetaContext.getInstance().isInitMeta() && downloadingChooserDialog != null && downloadingChooserDialog.isShowing()) {
            downloadingChooserDialog.dismiss();
            downloadingChooserDialog = null;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        isFront = true;
        if (downloadProgress >= 0) {
            Log.i(TAG, "onResume continue download");
            MetaContext.getInstance().downloadScene(MetaContext.getInstance().getSceneInfo());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isFront = false;
        if (downloadProgress >= 0) {
            Log.i(TAG, "onPause cancel download");
            MetaContext.getInstance().cancelDownloadScene(MetaContext.getInstance().getSceneInfo());
        }
    }
}