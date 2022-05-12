package io.agora.metachat.example.ui.game;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;

import coil.ImageLoaders;
import coil.request.ImageRequest;
import io.agora.meta.AgoraMetaActivity;
import io.agora.meta.AgoraMetaView;
import io.agora.metachat.example.MainActivity;
import io.agora.metachat.example.MetaChatContext;
import io.agora.metachat.example.R;
import io.agora.metachat.example.databinding.CardViewBinding;
import io.agora.metachat.example.databinding.GameActivityBinding;
import io.agora.metachat.example.dialog.CustomDialog;
import io.agora.rtc2.Constants;

public class GameActivity extends AgoraMetaActivity implements View.OnClickListener {

    private GameActivityBinding binding1;
    private CardViewBinding binding2;
    private final ObservableBoolean enableMic = new ObservableBoolean(true);
    private final ObservableBoolean enableSpeaker = new ObservableBoolean(true);
    private final ObservableBoolean isBroadcaster = new ObservableBoolean(true);
    private final Observable.OnPropertyChangedCallback callback =
            new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (sender == enableMic) {
                        if (!MetaChatContext.getInstance().enableLocalAudio(enableMic.get())) {
                            return;
                        }
                        binding1.mic.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        enableMic.get() ? R.mipmap.microphone_on : R.mipmap.microphone_off,
                                        getTheme()
                                )
                        );
                    } else if (sender == enableSpeaker) {
                        if (!MetaChatContext.getInstance().setDefaultAudioRoutetoSpeakerphone(enableSpeaker.get())) {
                            return;
                        }
                        binding1.speaker.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        enableSpeaker.get() ? R.mipmap.voice_on : R.mipmap.voice_off,
                                        getTheme()
                                )
                        );
                    } else if (sender == isBroadcaster) {
                        binding2.mode.setText(isBroadcaster.get() ? "语聊模式" : "游客模式");
                        binding2.tips.setVisibility(isBroadcaster.get() ? View.GONE : View.VISIBLE);
                        binding2.role.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        isBroadcaster.get() ? R.mipmap.offbtn : R.mipmap.onbtn,
                                        getTheme()
                                )
                        );
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding1 = GameActivityBinding.inflate(getLayoutInflater());
        binding2 = CardViewBinding.bind(binding1.getRoot());
        setContentView(binding1.getRoot());

        binding1.back.setOnClickListener(this);
        binding2.mode.setOnClickListener(this);
        binding2.role.setOnClickListener(this);
        binding1.users.setOnClickListener(this);
        binding1.mic.setOnClickListener(this);
        binding1.speaker.setOnClickListener(this);

        enableMic.addOnPropertyChangedCallback(callback);
        enableSpeaker.addOnPropertyChangedCallback(callback);
        isBroadcaster.addOnPropertyChangedCallback(callback);

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
        enableMic.removeOnPropertyChangedCallback(callback);
        enableSpeaker.removeOnPropertyChangedCallback(callback);
        isBroadcaster.removeOnPropertyChangedCallback(callback);
    }

    private void refreshByIntent(Intent intent) {
        String nickname = intent.getStringExtra("nickname");
        if (nickname != null) {
            binding2.nickname.setText(nickname);
        }

        String avatar = intent.getStringExtra("avatar");
        if (avatar != null) {
            ImageRequest request = new ImageRequest.Builder(this)
                    .data(avatar)
                    .target(binding2.avatar)
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
                MetaChatContext.getInstance().destroy();
                unloadUnity();
                break;
            case R.id.mode:
                if (!isBroadcaster.get()) {
                    CustomDialog.showTips(this);
                }
                break;
            case R.id.role:
                MetaChatContext.getInstance().updateRole(isBroadcaster.get() ?
                        Constants.CLIENT_ROLE_AUDIENCE : Constants.CLIENT_ROLE_BROADCASTER);
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
        binding1.unity.addView(view);
    }

    @Override
    public void onUnityPlayerUnloaded() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    public void onUnityPlayerQuitted() {
    }

}
