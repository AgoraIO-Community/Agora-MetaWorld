package io.agora.metachat.game.sence.karaoke

import android.content.Context
import io.agora.metachat.R
import io.agora.metachat.game.model.ConsoleAudioEffect
import io.agora.metachat.game.model.MusicDetail

/**
 * @author create by zhangwei03
 */
object MChatKaraokeConstructor {

    @JvmStatic
    fun buildSongType(): List<MChatSongType> {
        return mutableListOf(
            MChatSongType.Christmas,
            MChatSongType.Hot,
        )
    }

    @JvmStatic
    fun buildAllSongMap(): Map<MChatSongType, List<MusicDetail>> {
        return mutableMapOf(
            MChatSongType.Christmas to buildSongData(MChatSongType.Christmas),
            MChatSongType.Hot to buildSongData(MChatSongType.Hot),
        )
    }

    @JvmStatic
    private fun buildSongData(songType: MChatSongType): List<MusicDetail> {
        when (songType) {
            MChatSongType.Christmas -> {
                return mutableListOf(
                    MusicDetail(
                        mvUrl = "http://tyst.migu.cn/public/product6th/productaichang/productA02/mg720p_vod2B_done/2020051811/All I Want For Christmas Is You-Justin%2BBieber-v1.mkv",
                        songCode = 238861,
                        name = "All I Want For Christmas Is You",
                        singer = "Justin Bieber",
                        poster = "mchat_portrait10",
                        pitchType = 1,
                        type = 2,
                    ),
                    MusicDetail(
                        mvUrl = "http://tyst.migu.cn/public/product6th/productaichang/productA02/mg720p_vod6_done/2020051815/Last Christmas-Taylor Swift[泰勒斯威夫特]-v1.mkv",
                        songCode = 270637,
                        name = "All I Want For Christmas Is You",
                        singer = "Last Christmas",
                        poster = "avatar8",
                        pitchType = 1,
                        type = 3,
                    ),
                    MusicDetail(
                        mvUrl = "http://tyst.migu.cn/public/product6th/productaichang/productA02/mg720p_vod2B_done/2020051811/Jingle Bells-圣诞歌-v1.mkv",
                        songCode = 134358,
                        name = "Jingle Bells",
                        singer = "Christmas Song",
                        poster = "avatar6",
                        pitchType = 1,
                        type = 4,
                    ),
                    MusicDetail(
                        mvUrl = "http://tyst.migu.cn/public/product6th/productaichang/productA02/mg720_vod3A_done/2020051422/Silent Night-Kathleen%2BHeath%2BStephanie%2BWong-v1.mkv",
                        songCode = 241733,
                        name = "Silent Night",
                        singer = "Kathleen Battle",
                        poster = "avatar4",
                        pitchType = 1,
                        type = 5,
                    ),
                    MusicDetail(
                        mvUrl = "http://tyst.migu.cn/public/product6th/productaichang/productA02/mg720p_vod3B_done/2020051812/Let It Snow Let It Snow-其他-v1.mkv",
                        songCode = 273955,
                        name = "Let It Snow Let It Snow",
                        singer = "Christmas Song",
                        poster = "avatar2",
                        pitchType = 1,
                        type = 3,
                    ),
                )
            }
            MChatSongType.Hot -> {
                return mutableListOf(
                    MusicDetail(
                        mvUrl = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/Backstreet_Boys_I_Want_It_That_Way(Video_Karaoke_with_a_colored_background)_55190.mp4",
                        songCode = 8530,
                        name = "I Want It That Way",
                        singer = "Backstreet Boys",
                        poster = "mchat_portrait9",
                        pitchType = 1,
                        type = 2,
                    ),
                    MusicDetail(
                        mvUrl = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/Eurythmics_Sweet_Dreams_(Are_Made_of_This)(Video_Karaoke_with_a_colored_background)_10601948.mp4",
                        songCode = 69124,
                        name = "Sweet Dreams (Are Made of This)",
                        singer = "Eurythmics",
                        poster = "mchat_portrait7",
                        pitchType = 1,
                        type = 2,
                    ),
                    MusicDetail(
                        mvUrl = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/Gloria_Gaynor_I_Will_Survive(Video_Karaoke_with_a_colored_background)_56048.mp4",
                        songCode = 5921,
                        name = "I Will Survive",
                        singer = "Gloria Gaynor",
                        poster = "mchat_portrait13",
                        pitchType = 1,
                        type = 4,
                    ),
                    MusicDetail(
                        mvUrl = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/Queen_Bohemian_Rhapsody(Video_Karaoke_with_a_colored_background)_55911.mp4",
                        songCode = 12617,
                        name = "Bohemian Rhapsody",
                        singer = "Queen",
                        poster = "mchat_portrait14",
                        pitchType = 2,
                        type = 5,
                    ),
                    MusicDetail(
                        mvUrl = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/Whitney_Houston_I_Wanna_Dance_with_Somebody(Video_Karaoke_with_a_colored_background)_134240.mp4",
                        songCode = 5468,
                        name = "I Wanna Dance with Somebody",
                        singer = "Whitney Houston",
                        poster = "mchat_portrait15",
                        pitchType = 1,
                        type = 3,
                    ),
                )
            }
            else -> return mutableListOf()
        }
    }

    @JvmStatic
    fun buildAudioEffect(context: Context): List<ConsoleAudioEffect> {
        return mutableListOf(
            ConsoleAudioEffect(
                MChatAudioEffect.Studio,
                R.drawable.mchat_bg_recording_studio,
                context.resources.getString(R.string.mchat_recording_studio)
            ),
            ConsoleAudioEffect(
                MChatAudioEffect.Concert,
                R.drawable.mchat_bg_concert,
                context.resources.getString(R.string.mchat_concert)
            ),
            ConsoleAudioEffect(
                MChatAudioEffect.KTV,
                R.drawable.mchat_bg_ktv,
                context.resources.getString(R.string.mchat_ktv)
            ),
            ConsoleAudioEffect(
                MChatAudioEffect.Spatial,
                R.drawable.mchat_bg_hollow_sound,
                context.resources.getString(R.string.mchat_hollow_sound)
            ),
        )
    }
}