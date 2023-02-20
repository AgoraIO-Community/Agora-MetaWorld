package io.agora.metachat.game.sence

import io.agora.metachat.global.MChatConstant
import io.agora.metachat.service.MChatServiceProtocol
import io.agora.metachat.service.StreamDataBaseBody
import io.agora.metachat.tools.GsonTools
import io.agora.metachat.tools.LogTools

/**
 * @author create by zhangwei03
 */
object MChatStreamParser {

    fun parse(uid: Int, streamId: Int, data: ByteArray) {
        val msg = String(data)
        LogTools.d("MChatStreamParser", "${Thread.currentThread()} uid:$uid,streamId:$streamId,msg:$msg")
        GsonTools.toBean(msg, StreamDataBaseBody::class.java)?.let { dataBody ->
            MChatServiceProtocol.getImplInstance().getSubscribeDelegates().forEach { chatSubscribe ->
                when (dataBody.action) {
                    MChatConstant.StreamParam.ACTION_KARAOKE -> {
                        val value = GsonTools.toBean(dataBody.msg.toString(), Int::class.java)
                        chatSubscribe.onKaraoke(value == MChatConstant.StreamParam.VALUE_OPEN)
                    }
                    MChatConstant.StreamParam.ACTION_ORIGINAL_SINGING -> {
                        val value = GsonTools.toBean(dataBody.msg.toString(), Int::class.java)
                        chatSubscribe.onOriginalSinging(value == MChatConstant.StreamParam.VALUE_OPEN)
                    }
                    MChatConstant.StreamParam.ACTION_EARPHONE_MONITORING -> {
                        val value = GsonTools.toBean(dataBody.msg.toString(), Int::class.java)
                        chatSubscribe.onEarphoneMonitoring(value == MChatConstant.StreamParam.VALUE_OPEN)
                    }
                    MChatConstant.StreamParam.ACTION_SONG_KEY -> {
                        GsonTools.toBean(dataBody.msg.toString(), Int::class.java)?.let {
                            chatSubscribe.onChangeSongKey(it)
                        }
                    }
                    MChatConstant.StreamParam.ACTION_ACCOMPANIMENT -> {
                        GsonTools.toBean(dataBody.msg.toString(), Int::class.java)?.let {
                            chatSubscribe.onAccompanimentMusic(it)
                        }
                    }
                    MChatConstant.StreamParam.ACTION_AUDIO_EFFECT -> {
                        GsonTools.toBean(dataBody.msg.toString(), Int::class.java)?.let {
                            chatSubscribe.onAudioEffect(it)
                        }
                    }
                }
            }
        }

    }
}