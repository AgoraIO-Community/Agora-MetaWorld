package io.agora.meta.example_voice_driver.utils;

public class MetaConstants {
    /**
     * https://test.cdn.sbnh.cn/9ad87c1738bf0485b7f243ee5cfb409f.mp4
     * http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/15c116aca7590992f261143935d6f2cb.mov
     */
    public static final String VIDEO_URL = "https://download.agora.io/demo/test/agora_meta_ads.mov";
    public static final int PLAY_ADVERTISING_VIDEO_REPEAT = -1;

    public static final String MMKV_ID = "meta";
    public static final String MMKV_ROLE_INFO = "role_info";
    public static final String MMKV_CATALOG_HAS = "catalog_has";

    public static final int SCENE_NONE = -1;
    public static final int SCENE_DRESS = 0;
    public static final int SCENE_GAME = 1;

    public static final int SCENE_ID_META_1_0 = 15;
    public static final int SCENE_ID_META_1_1_VOICE_DRIVER = 24;

    public static final String KEY_UNITY_MESSAGE_UPDATE_DRESS = "updateDress";
    public static final String KEY_UNITY_MESSAGE_UPDATE_FACE = "updateFace";
    public static final String KEY_UNITY_MESSAGE_FACE_CAPTURE = "faceCapture";

    public static final String SCENE_MESSAGE_ADD_SCENE_VIEW_SUCCESS = "addSceneViewSuccess";
    public static final String SCENE_MESSAGE_REMOVE_SCENE_VIEW_SUCCESS = "removeSceneViewSuccess";

    public static final String KEY_FACE_CAPTURE_INFO = "FaceCaptureInfo";


    public static final int GENDER_BOY = 0;
    public static final int GENDER_GIRL = 1;

    public static final String AVATAR_TYPE_BOY = "boy";
    public static final String AVATAR_TYPE_GIRL = "girl";
    public static final String AVATAR_TYPE_HUAMULAN = "huamulan";

    public static final int AUDIO_SAMPLE_RATE = 16000;
    public static final int AUDIO_SAMPLE_NUM_OF_CHANNEL = 1;
    public static final int AUDIO_BITS_PER_SAMPLE = 16;
}
