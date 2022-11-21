package io.agora.metachat.example.models;

import androidx.annotation.NonNull;

/**
 * unity消息RoleInfo
 */
public class UnityRoleInfo {
    //性别
    private int gender;
    //头发
    private int hair;
    //上衣
    private int tops;
    //裤子
    private int lower;
    //鞋子
    private int shoes;

    public UnityRoleInfo() {
    }


    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
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
        return "UnityRoleInfo{" +
                "gender=" + gender +
                ", hair=" + hair +
                ", tops=" + tops +
                ", lower=" + lower +
                ", shoes=" + shoes +
                '}';
    }
}
