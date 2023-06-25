package io.agora.meta.example_voice_driver.models;

import androidx.annotation.NonNull;

public class FaceParameterItem {
    private String key;
    private int value;

    public FaceParameterItem() {

    }

    public FaceParameterItem(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return "FaceParameterItem{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
