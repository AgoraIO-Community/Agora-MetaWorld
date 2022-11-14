package io.agora.metachat.example.utils;

import java.util.Random;

import io.agora.media.RtcTokenBuilder;
import io.agora.metachat.example.BuildConfig;
import io.agora.rtm.RtmTokenBuilder;

public class KeyCenter {

    public static final String CHANNEL_ID = "MetaChatTest112";

    public static final String APP_ID = BuildConfig.APP_ID;
    public static final int RTC_UID = new Random().nextInt(1000); // 不要设置成0！！！
    public static final String RTC_TOKEN = new RtcTokenBuilder().buildTokenWithUid(
            APP_ID,
            BuildConfig.APP_CERTIFICATE,
            CHANNEL_ID,
            RTC_UID,
            RtcTokenBuilder.Role.Role_Publisher,
            0
    );

    public static final String RTM_UID = String.valueOf(RTC_UID); // Demo侧使用String的RTC_UID
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

}
