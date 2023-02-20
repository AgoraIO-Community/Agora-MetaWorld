package io.agora.metachat.game.sence.npc

import android.content.Context
import io.agora.metachat.game.sence.MChatContext
import io.agora.metachat.game.sence.SceneObjectId
import io.agora.metachat.global.MChatConstant
import io.agora.rtc2.Constants

/**
 * @author create by zhangwei03
 */
class MChatNpcManager constructor() {

    companion object {
        const val TAG = "MChatNpcManager"
    }

    private lateinit var npc1: MchatNpc
    private lateinit var npc2: MchatNpc
    private lateinit var npc3: MchatNpc

    // npc 音量
    var npcVolume: Int = MChatConstant.DefaultValue.DEFAULT_NPC_VOLUME

    fun initNpcMediaPlayer(context: Context, chatContext: MChatContext, npcListener: NpcListener) {
        npc1 = MchatNpc(context, chatContext, SceneObjectId.NPC1.value, "npc_id_1.m4a", npcVolume, npcListener)
        npc2 = MchatNpc(context, chatContext, SceneObjectId.NPC2.value, "npc_id_2.m4a", npcVolume, npcListener)
        npc3 = MchatNpc(context, chatContext, SceneObjectId.NPC3.value, "npc_id_3.m4a", npcVolume, npcListener)
    }

    fun getNpc(id: Int): MchatNpc? {
        return when (id) {
            SceneObjectId.NPC1.value -> npc1
            SceneObjectId.NPC2.value -> npc2
            SceneObjectId.NPC3.value -> npc3
            else -> null
        }
    }

    //圆桌npc
    fun setNpcVolume(volume: Int, forced: Boolean = false): Boolean {
        var result = false
        if (forced || this.npcVolume != volume) {
            npc2.setPlayerVolume(volume)?.also {
                if (Constants.ERR_OK == it) {
                    this.npcVolume = volume
                    result = true
                }
            }
        }
        return result
    }

    fun play(id: Int) {
        when (id) {
            SceneObjectId.NPC1.value -> {
                npc1.play()
            }
            SceneObjectId.NPC2.value -> {
                npc2.play()
            }
            SceneObjectId.NPC3.value -> {
                npc3.play()
            }
        }
    }

    fun stop(id: Int) {
        when (id) {
            SceneObjectId.NPC1.value -> npc1.stop()
            SceneObjectId.NPC2.value -> npc2.stop()
            SceneObjectId.NPC3.value -> npc3.stop()
        }
    }

    fun playAll() {
        npc1.play()
        npc2.play()
        npc3.play()
    }

    fun stopAll() {
        npc1.stop()
        npc2.stop()
        npc3.stop()
    }

    fun destroy() {
        npc1.destroy()
        npc2.destroy()
        npc3.destroy()
    }
}

interface NpcListener {
    fun onNpcReady(id: Int, sourceName: String)

    fun onNpcFail()
}