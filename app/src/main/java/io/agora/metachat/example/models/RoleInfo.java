package io.agora.metachat.example.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存换装的数据
 */
public class RoleInfo {

    public List<SkinInfo> skinInfos;
    //名字
    public String name;
    //性别
    public String gender;

    public RoleInfo(List<SkinInfo> skinInfos, String name, String gender) {
        this.skinInfos = skinInfos;
        this.name = name;
        this.gender = gender;
    }

    public List<SkinInfo> getChangeClothingList() {
        return skinInfos;
    }

    public void setChangeClothingList(List<SkinInfo> skinInfos) {
        this.skinInfos = skinInfos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @NonNull
    @Override
    public String toString() {
        return "RoleInfo{" +
                "skinInfos=" + skinInfos +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
