package io.agora.metachat.example.models;

import androidx.annotation.NonNull;

public class EnterSceneExtraInfo {
    private int sceneIndex;

    public int getSceneIndex() {
        return sceneIndex;
    }

    public void setSceneIndex(int sceneIndex) {
        this.sceneIndex = sceneIndex;
    }

    @NonNull
    @Override
    public String toString() {
        return "EnterSceneExtraInfo{" +
                "sceneIndex=" + sceneIndex +
                '}';
    }
}
