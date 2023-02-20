package io.agora.metachat.game.model

import androidx.annotation.DrawableRes
import io.agora.metachat.game.sence.karaoke.MChatAudioEffect

data class MusicListResp(
    val code: Long,
    val msg: String,
    val requestId: String,
    val ext: String = "",
    val data: MusicDetailData
)

data class MusicDetailData(
    val size: Long,
    val page: Long,
    val count: Long,
    val total: Long,
    val list: List<MusicDetail>?
)

data class MusicDetail(
    val songCode: Long,
    val name: String,
    val singer: String,
    val poster: String,
    val type: Long,
    val pitchType: Long,
    val mvUrl: String
) {

    fun supportScore(): Boolean {
        return this.pitchType == 1L
    }

    fun supportAccompany(): Boolean {
        return type == 4L || type == 5L
    }

    fun supportOriginal(): Boolean {
        return type == 4L || type == 5L
    }
}

data class MvResolution(
    val resolution: String,
    val bw: String
)

data class VideoUrlRespBody(
    val code: Long,
    val msg: String,
    val requestId: String,
    val ext: String,
    val data: VideoUrlData
)

data class VideoUrlData(
    val playUrl: String,
    val lyric: String,
    val expiryTime: Long,
    val mvList: List<VideoUrl>
)

data class VideoUrl(
    val resolution: String,
    val mvUrl: String
)

data class ConsoleAudioEffect(
    val audioEffect: MChatAudioEffect,
    @DrawableRes val effectBg: Int,
    val effectTxt: String,
)