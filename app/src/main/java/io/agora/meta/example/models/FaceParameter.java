package io.agora.meta.example.models;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;

public class FaceParameter {
    private String avatar;
    @JSONField(name = "blendshape")

    private FaceBlendShape blendShape;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public FaceBlendShape getBlendShape() {
        return blendShape;
    }

    public void setBlendShape(FaceBlendShape blendShape) {
        this.blendShape = blendShape;
    }

    @NonNull
    @Override
    public String toString() {
        return "FaceParameter{" +
                "avatar='" + avatar + '\'' +
                ", blendShape=" + blendShape +
                '}';
    }
}
