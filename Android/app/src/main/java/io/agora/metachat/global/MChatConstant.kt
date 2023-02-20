package io.agora.metachat.global

/**
 * @author create by zhangwei03
 */
object MChatConstant {

    object Gender {
        const val MALE = 0
        const val FEMALE = 1
    }

    object Params {
        const val KEY_ROOM_ID: String = "key_room_id"
        const val KEY_ROOM_NAME: String = "key_room_name"
        const val KEY_ROOM_COVER_INDEX: String = "key_room_cover_index"
        const val KEY_ROOM_PASSWORD: String = "key_room_password"
        const val KEY_IS_CREATE: String = "key_is_create"
    }

    object Scene {
        const val SCENE_NONE = -1
        const val SCENE_GAME = 0
        const val SCENE_DRESS = 1
    }

    /**
     * https://test.cdn.sbnh.cn/9ad87c1738bf0485b7f243ee5cfb409f.mp4
     * http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/15c116aca7590992f261143935d6f2cb.mov
     */
    const val VIDEO_URL = "http://agora.fronted.love/yyl.mov"
    const val DEFAULT_PORTRAIT = "https://accpic.sd-rtn.com/pic/test/png/2.png"

    private const val badgeUrl0 =
        "http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/login_badge_0.png"
    private const val badgeUrl1 =
        "http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/login_badge_1.png"
    private const val badgeUrl2 =
        "http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/login_badge_2.png"

    fun getBadgeUrl(badgeIndex: Int): String {
        return when (badgeIndex) {
            0 -> badgeUrl0
            1 -> badgeUrl1
            2 -> badgeUrl2
            else -> badgeUrl0
        }
    }

    /***
     * 默认值
     */
    object DefaultValue {
        // 默认场景
        const val DEFAULT_SCENE_ID: Long = 17

        // 电视默认音量25，最大100
        const val DEFAULT_TV_VOLUME: Int = 25

        // 圆桌默认音量25，最大100
        const val DEFAULT_NPC_VOLUME: Int = 25

        // 默认音效距离5.0,最大15
        const val DEFAULT_RECV_RANGE: Float = 5.0F

        // 默认衰减系数8.6,最大10
        const val DEFAULT_DISTANCE_UNIT: Float = 8.6F

        // video frame display id
        const val VIDEO_DISPLAY_ID: String = "1"
    }

    object StreamParam {
        // k歌
        const val ACTION_KARAOKE: Int = 1

        // 原唱
        const val ACTION_ORIGINAL_SINGING: Int = 2

        // 耳返
        const val ACTION_EARPHONE_MONITORING: Int = 3

        // 升降调
        const val ACTION_SONG_KEY: Int = 4

        // 伴奏音量
        const val ACTION_ACCOMPANIMENT: Int = 5

        // 音效
        const val ACTION_AUDIO_EFFECT: Int = 6

        // 开
        const val VALUE_OPEN: Int = 1

        // 关
        const val VALUE_CLOSE: Int = 0
    }
}