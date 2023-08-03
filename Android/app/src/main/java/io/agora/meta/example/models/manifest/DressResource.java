package io.agora.meta.example.models.manifest;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class DressResource {

    private String avatar;
    private DressItemResource[] resources;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public DressItemResource[] getResources() {
        return resources;
    }

    public void setResources(DressItemResource[] resources) {
        this.resources = resources;
    }

    @NonNull
    @Override
    public String toString() {
        return "DressResource{" +
                "avatar='" + avatar + '\'' +
                ", resources=" + Arrays.toString(resources) +
                '}';
    }
}
