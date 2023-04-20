package io.agora.meta.example.models;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Arrays;

public class FaceBlendShape {
    @JSONField(name = "Face")
    private FaceBlendShapeItem[] face;
    @JSONField(name = "Eyebrow")
    private FaceBlendShapeItem[] eyeBrow;
    @JSONField(name = "Eye")
    private FaceBlendShapeItem[] eye;
    @JSONField(name = "Nose")
    private FaceBlendShapeItem[] nose;
    @JSONField(name = "Mouth")
    private FaceBlendShapeItem[] mouth;

    public FaceBlendShapeItem[] getFace() {
        return face;
    }

    public void setFace(FaceBlendShapeItem[] face) {
        this.face = face;
    }

    public FaceBlendShapeItem[] getEyeBrow() {
        return eyeBrow;
    }

    public void setEyeBrow(FaceBlendShapeItem[] eyeBrow) {
        this.eyeBrow = eyeBrow;
    }

    public FaceBlendShapeItem[] getEye() {
        return eye;
    }

    public void setEye(FaceBlendShapeItem[] eye) {
        this.eye = eye;
    }

    public FaceBlendShapeItem[] getNose() {
        return nose;
    }

    public void setNose(FaceBlendShapeItem[] nose) {
        this.nose = nose;
    }

    public FaceBlendShapeItem[] getMouth() {
        return mouth;
    }

    public void setMouth(FaceBlendShapeItem[] mouth) {
        this.mouth = mouth;
    }

    @NonNull
    @Override
    public String toString() {
        return "FaceBlendShape{" +
                "face=" + Arrays.toString(face) +
                ", eyeBrow=" + Arrays.toString(eyeBrow) +
                ", eye=" + Arrays.toString(eye) +
                ", nose=" + Arrays.toString(nose) +
                ", mouth=" + Arrays.toString(mouth) +
                '}';
    }
}
