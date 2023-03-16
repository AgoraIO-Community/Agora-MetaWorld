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

    private lateinit var npc1: MchatNpc // 圆桌NPC
    private lateinit var npc2: MchatNpc // 移动NPC

    // npc 音量
    var npcVolume: Int = MChatConstant.DefaultValue.DEFAULT_NPC_VOLUME

    fun initNpcMediaPlayer(context: Context, chatContext: MChatContext, npcListener: NpcListener) {
        npc1 = MchatNpc(context, chatContext, SceneObjectId.NPC1.value, "npc_id_1.m4a", npcVolume, npcListener)
        npc2 = MchatNpc(context, chatContext, SceneObjectId.NPC2.value, "npc_id_2.m4a", npcVolume, npcListener)
    }

    fun getNpc(id: Int): MchatNpc? {
        return when (id) {
            SceneObjectId.NPC1.value -> npc1
            SceneObjectId.NPC2.value -> npc2
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

    fun playAll() {
        npc1.play()
        npc2.play()
    }

    fun stopAll() {
        npc1.stop()
        npc2.stop()
    }

    fun destroy() {
        npc1.destroy()
        npc2.destroy()
    }
}

interface NpcListener {
    fun onNpcReady(id: Int, sourceName: String)

    fun onNpcFail()
}