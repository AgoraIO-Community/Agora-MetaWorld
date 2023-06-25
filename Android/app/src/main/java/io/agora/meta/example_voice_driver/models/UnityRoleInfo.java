package io.agora.meta.example_voice_driver.models;

import androidx.annotation.NonNull;

/**
 * unity消息RoleInfo
 */
public class UnityRoleInfo {
    //性别
    private int gender;
    //头发

    public UnityRoleInfo() {
    }


    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    @NonNull
    @Override
    public String toString() {
        return "UnityRoleInfo{" +
                "gender=" + gender +
                '}';
    }
}
