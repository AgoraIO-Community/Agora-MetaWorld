package io.agora.metachat.example.ui.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;

import java.util.Locale;

import coil.ImageLoaders;
import coil.request.ImageRequest;
import io.agora.meta.AgoraMetaActivity;
import io.agora.meta.AgoraMetaView;
import io.agora.metachat.IMetachatSceneEventHandler;
import io.agora.metachat.MetachatUserPositionInfo;
import io.agora.metachat.example.MainActivity;
import io.agora.metachat.example.MetaChatContext;
import io.agora.metachat.example.R;
import io.agora.metachat.example.databinding.GameActivityBinding;
import io.agora.metachat.example.dialog.CustomDialog;
import io.agora.rtc2.Constants;

public class GameActivity extends AgoraMetaActivity implements View.OnClickListener, IMetachatSceneEventHandler {

    private GameActivityBinding binding;
    private final ObservableBoolean isEnterScene = new ObservableBoolean(false);
    private final ObservableBoolean enableMic = new ObservableBoolean(true);
    private final ObservableBoolean enableSpeaker = new ObservableBoolean(true);
    private final ObservableBoolean isBroadcaster = new ObservableBoolean(true);
    private final Observable.OnPropertyChangedCallback callback =
            new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (sender == isEnterScene) {
                        binding.back.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                        binding.card.getRoot().setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                        binding.users.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                        binding.mic.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
                        binding.speaker.setVisibility(isEnterScene.get() ? View.VISIBLE : View.GONE);
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

        initUnity();

        refreshByIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        refreshByIntent(intent);
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

    private void refreshByIntent(Intent intent) {
        String nickname = intent.getStringExtra("nickname");
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

        String roomName = intent.getStringExtra("roomName");
        if (roomName != null) {
            MetaChatContext.getInstance().createAndEnterScene(roomName);
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
    public void onUnityPlayerLoaded(AgoraMetaView view) {
        binding.unity.addView(view);
    }

    @Override
    public void onUnityPlayerUnloaded() {
        // 必须在onUnityPlayerUnloaded里调用
        MetaChatContext.getInstance().destroy();

        isEnterScene.set(false);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    public void onUnityPlayerQuitted() {
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
        if (errorCode == 0) {
            unloadUnity();
        }
    }

    @Override
    public void onRecvMessageFromScene(byte[] message) {

    }

    @Override
    public void onUserPositionChanged(String uid, MetachatUserPositionInfo posInfo) {

    }
}
