package io.agora.metachat.example.models;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class DressItemResource {
    private int id;
    private String name;
    private int[] assets;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getAssets() {
        return assets;
    }

    public void setAssets(int[] assets) {
        this.assets = assets;
    }

    @NonNull
    @Override
    public String toString() {
        return "DressResource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", assets=" + Arrays.toString(assets) +
                '}';
    }
}
