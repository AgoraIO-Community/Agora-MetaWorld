package io.agora.meta.example_voice_driver.models.manifest;

import androidx.annotation.NonNull;

public class FaceBlendShapeItem {
    private String key;
    private String ch;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCh() {
        return ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    @NonNull
    @Override
    public String toString() {
        return "FaceBlendShapeItem{" +
                "key='" + key + '\'' +
                ", ch='" + ch + '\'' +
                '}';
    }
}
