package io.agora.meta.example_voice_driver.models.manifest;


import androidx.annotation.NonNull;

import java.util.Arrays;

public class FaceBlendShape {
    private String type;
    private FaceBlendShapeItem[] shapes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FaceBlendShapeItem[] getShapes() {
        return shapes;
    }

    public void setShapes(FaceBlendShapeItem[] shapes) {
        this.shapes = shapes;
    }

    @NonNull
    @Override
    public String toString() {
        return "FaceBlendShape{" +
                "type='" + type + '\'' +
                ", shapes=" + Arrays.toString(shapes) +
                '}';
    }
}
