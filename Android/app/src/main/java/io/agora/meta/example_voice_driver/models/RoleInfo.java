package io.agora.meta.example_voice_driver.models;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.agora.meta.example_voice_driver.utils.MetaConstants;

public class RoleInfo {
    //名字
    private String name;
    //性别
    private int gender;
    //avatar
    private String avatarUrl;

    private String avatarType;

    private final Map<Integer, Integer> dressResourceMap;

    private final Map<String, FaceParameterItem> faceParameterResourceMap;

    public RoleInfo() {
        name = "";
        gender = -1;
        dressResourceMap = new HashMap<>();
        faceParameterResourceMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarType() {
        return avatarType;
    }

    public void setAvatarType(String avatarType) {
        this.avatarType = avatarType;
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
                ", gender=" + gender +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", avatarType='" + avatarType + '\'' +
                ", dressResourceMap=" + dressResourceMap +
                ", faceParameterResourceMap=" + faceParameterResourceMap +
                '}';
    }
}
