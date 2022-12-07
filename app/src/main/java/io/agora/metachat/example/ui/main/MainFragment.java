package io.agora.metachat.example.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import coil.ImageLoaders;
import coil.request.ImageRequest;
import io.agora.metachat.example.metachat.MetaChatContext;
import io.agora.metachat.example.R;
import io.agora.metachat.example.adapter.SexAdapter;
import io.agora.metachat.example.databinding.MainFragmentBinding;
import io.agora.metachat.example.dialog.CustomDialog;
import io.agora.metachat.example.ui.game.GameActivity;
import io.agora.metachat.example.utils.MetaChatConstants;

public class MainFragment extends Fragment {

    private MainViewModel mViewModel;
    private MainFragmentBinding binding;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @SuppressLint("CheckResult")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = MainFragmentBinding.inflate(inflater, container, false);
        binding.nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setNickname(s.toString());
            }
        });
        SexAdapter adapter = new SexAdapter(requireContext());
        binding.spinner.setAdapter(adapter);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.check(i);
                if (i == 0) {
                    mViewModel.setSex(MetaChatConstants.GENDER_MAN);
                } else {
                    mViewModel.setSex(MetaChatConstants.GENDER_WOMEN);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        //防止多次频繁点击异常处理
        RxView.clicks(binding.enter).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            if (TextUtils.isEmpty(binding.nickname.getText().toString())) {
                Toast.makeText(requireActivity(), "请输入昵称", Toast.LENGTH_LONG).show();
            } else {
                MetaChatContext.getInstance().initRoleInfo(binding.nickname.getText().toString(),
                        mViewModel.getSex().getValue() == null ? MetaChatConstants.GENDER_MAN : mViewModel.getSex().getValue());
                MetaChatContext.getInstance().getRoleInfo().setAvatar(mViewModel.getAvatar().getValue());
                mViewModel.getScenes();
            }
        });

        RxView.clicks(binding.avatar).throttleFirst(1, TimeUnit.SECONDS).subscribe(o -> {
            CustomDialog.showAvatarPicker(requireContext(), charSequence -> {
                mViewModel.setAvatar(charSequence.toString());
                return null;
            }, null, null);
        });

        return binding.getRoot();
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
        mViewModel.getNickname().observe(owner, charSequence -> {
            if (charSequence.length() < 2 || charSequence.length() > 10) {
                binding.tips.setVisibility(View.VISIBLE);
            } else {
                binding.tips.setVisibility(View.GONE);
            }
        });
        mViewModel.getSex().observe(owner, i -> {
            if (i == MetaChatConstants.GENDER_MAN) {
                binding.sex.setText(context.getResources().getStringArray(R.array.sex_array)[0]);
            } else {
                binding.sex.setText(context.getResources().getStringArray(R.array.sex_array)[1]);
            }

        });
        mViewModel.getSceneList().observe(owner, metachatSceneInfos -> {
            // TODO choose one
            if (metachatSceneInfos.size() > 0) {
                for (int a = 0; a < metachatSceneInfos.size(); a++) {
                    //8为内容中心测试的ID号
                    if (metachatSceneInfos.get(a).getSceneId() == 8) {
                        mViewModel.prepareScene(metachatSceneInfos.get(a));
                        break;
                    }
                }
            }
        });
        mViewModel.getSelectScene().observe(owner, sceneInfo -> {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            Intent intent = new Intent(context, GameActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        mViewModel.getRequestDownloading().observe(owner, aBoolean -> {
            if (aBoolean) {
                CustomDialog.showDownloadingChooser(context, materialDialog -> {
                    mViewModel.downloadScene(MetaChatContext.getInstance().getSceneInfo());

                    return null;
                }, null);
            }
        });
        mViewModel.getDownloadingProgress().observe(owner, integer -> {
            if (progressDialog == null) {
                progressDialog = CustomDialog.showDownloadingProgress(context, materialDialog -> {
                    mViewModel.cancelDownloadScene(MetaChatContext.getInstance().getSceneInfo());
                    return null;
                });
            } else if (integer < 0) {
                progressDialog.dismiss();
                progressDialog = null;
                return;
            }

            ConstraintLayout constraintLayout = CustomDialog.getCustomView(progressDialog);
            ProgressBar progressBar = constraintLayout.findViewById(R.id.progressBar);
            TextView textView = constraintLayout.findViewById(R.id.textView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(integer, true);
            } else {
                progressBar.setProgress(integer);
            }
            textView.setText(String.format(Locale.getDefault(), "%d%%", integer));
        });
    }

    private MaterialDialog progressDialog;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (MetaChatConstants.SCENE_NONE != MetaChatContext.getInstance().getCurrentScene()) {
            mViewModel.getScenes();
        }
    }
}