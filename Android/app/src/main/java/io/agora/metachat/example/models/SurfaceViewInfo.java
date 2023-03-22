package io.agora.metachat.example.models;

import android.view.View;

import androidx.annotation.NonNull;

public class SurfaceViewInfo {
    private View view;
    private int uid;

    public SurfaceViewInfo() {

    }

    public SurfaceViewInfo(View view, int uid) {
        this.view = view;
        this.uid = uid;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @NonNull
    @Override
    public String toString() {
        return "SurfaceViewInfo{" +
                "view=" + view +
                ", uid=" + uid +
                '}';
    }
}
