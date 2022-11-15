package io.agora.metachat.example.models;

import androidx.annotation.NonNull;

public class UnityMessage {
    private String key;
    private Object value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
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
