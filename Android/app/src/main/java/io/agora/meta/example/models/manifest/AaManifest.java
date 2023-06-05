package io.agora.meta.example.models.manifest;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class AaManifest {
    private DressResource[] dressResources;
    private FaceParameter[] faceParameters;

    public DressResource[] getDressResources() {
        return dressResources;
    }

    public void setDressResources(DressResource[] dressResources) {
        this.dressResources = dressResources;
    }

    public FaceParameter[] getFaceParameters() {
        return faceParameters;
    }

    public void setFaceParameters(FaceParameter[] faceParameters) {
        this.faceParameters = faceParameters;
    }

    @NonNull
    @Override
    public String toString() {
        return "AaManifest{" +
                "dressResources=" + Arrays.toString(dressResources) +
                ", faceParameters=" + Arrays.toString(faceParameters) +
                '}';
    }
}
