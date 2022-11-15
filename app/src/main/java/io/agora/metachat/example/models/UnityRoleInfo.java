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

    @NonNull
    @Override
    public String toString() {
        return "RoleInfo{" +
                "gender=" + gender +
                ", hair=" + hair +
                ", tops=" + tops +
                '}';
    }
}
