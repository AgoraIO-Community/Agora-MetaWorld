package io.agora.metachat.example.models;

import androidx.annotation.NonNull;


/**
 * 保存换装的数据
 */
public class RoleInfo {
    //名字
    private String name;
    //性别
    private int gender;
    //avatar
    private String avatar;
    //头发
    private int hair;
    //上衣
    private int tops;

    //裤子
    private int lower;
    //鞋子
    private int shoes;

    public RoleInfo() {
        name = "";
        gender = -1;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getHair() {
        return hair;
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public int getTops() {
        return tops;
    }

    public void setTops(int tops) {
        this.tops = tops;
    }

    public int getLower() {
        return lower;
    }

    public void setLower(int lower) {
        this.lower = lower;
    }

    public int getShoes() {
        return shoes;
    }

    public void setShoes(int shoes) {
        this.shoes = shoes;
    }

    @NonNull
    @Override
    public String toString() {
        return "RoleInfo{" +
                "name='" + name + '\'' +
                ", gender=" + gender +
                ", avatar='" + avatar + '\'' +
                ", hair=" + hair +
                ", tops=" + tops +
                ", lower=" + lower +
                ", shoes=" + shoes +
                '}';
    }
}
