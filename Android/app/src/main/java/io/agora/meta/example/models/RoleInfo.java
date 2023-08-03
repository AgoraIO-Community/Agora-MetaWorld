package io.agora.meta.example.models;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class RoleInfo {
    private String name;
    //avatar
    private String avatarUrl;

    private String avatarModelName;

    private final Map<Integer, Integer> dressResourceMap;

    private final Map<String, FaceParameterItem> faceParameterResourceMap;

    public RoleInfo() {
        name = "";
        dressResourceMap = new HashMap<>();
        faceParameterResourceMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarModelName() {
        return avatarModelName;
    }

    public void setAvatarModelName(String avatarModelName) {
        this.avatarModelName = avatarModelName;
    }

    public void updateDressResource(int type, int resId) {
        dressResourceMap.put(type, resId);
    }

    public Map<Integer, Integer> getDressResourceMap() {
        return dressResourceMap;
    }

    public void updateFaceParameter(String key, int value) {
        FaceParameterItem item = faceParameterResourceMap.get(key);
        if (null == item) {
            item = new FaceParameterItem(key, value);
            faceParameterResourceMap.put(key, item);
        } else {
            item.setValue(value);
        }
    }

    public Map<String, FaceParameterItem> getFaceParameterResourceMap() {
        return faceParameterResourceMap;
    }

    @NonNull
    @Override
    public String toString() {
        return "RoleInfo{" +
                "name='" + name + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", avatarModelName='" + avatarModelName + '\'' +
                ", dressResourceMap=" + dressResourceMap +
                ", faceParameterResourceMap=" + faceParameterResourceMap +
                '}';
    }
}
