package io.agora.metachat.example.models;

import androidx.annotation.NonNull;

public class UnityMessage {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return "UnityMessage{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
