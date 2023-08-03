package io.agora.meta.example.utils;

import java.util.Random;

import io.agora.media.RtcTokenBuilder;
import io.agora.meta.example.BuildConfig;
import io.agora.rtm.RtmTokenBuilder;

public class KeyCenter {
    public static final String APP_ID = BuildConfig.APP_ID;

    /**
     * 不要设置成0！！！
     */
    public static final int RTC_UID = new Random().nextInt(100000);

    public static String getRtcToken(String channelId){
        return new RtcTokenBuilder().buildTokenWithUid(
                APP_ID,
                BuildConfig.APP_CERTIFICATE,
                channelId,
                RTC_UID,
                RtcTokenBuilder.Role.Role_Publisher,
                0
        );
    }

    /**
     * Demo侧使用String的RTC_UID
     */
    public static final String RTM_UID = String.valueOf(RTC_UID);
    public static String RTM_TOKEN = null;

    static {
        try {
            RTM_TOKEN = new RtmTokenBuilder().buildToken(
                    APP_ID,
                    BuildConfig.APP_CERTIFICATE,
                    String.valueOf(RTC_UID),
                    RtmTokenBuilder.Role.Rtm_User,
                    0
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String FACE_CAP_APP_ID = BuildConfig.FACE_CAP_APP_ID;
    public static final String FACE_CAP_APP_KEY = BuildConfig.FACE_CAP_APP_KEY;

}
