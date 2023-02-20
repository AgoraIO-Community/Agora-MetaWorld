package io.agora.metachat.game.sence

import io.agora.metachat.IMetachatScene
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.GsonTools
import io.agora.metachat.tools.LogTools

class MChatUnityCmd constructor(private val scene: IMetachatScene) {
    private val tag = "MChatUnityCmd"
    private val messageListeners: MutableSet<SceneCmdListener> = mutableSetOf()

    // 停止k歌
    fun stopKaraoke() {
        val value = mutableMapOf<String, Any>().apply {
            put("actionId", KaraokeAction.Stop.value)
        }
        GsonTools.beanToString(value)?.let {
            val obj = SceneMessageRequestBody(SceneMessageType.Karaoke.value, it)
            sendSceneMessage(GsonTools.beanToString(obj))
        }
    }

    // 发送消息
    fun sendMessage(message: String) {
        val value = mutableMapOf<String, Any>().apply {
            put("content", message)
        }
        GsonTools.beanToString(value)?.let {
            val obj = SceneMessageRequestBody(SceneMessageType.SendMessage.value, it)
            sendSceneMessage(GsonTools.beanToString(obj))
        }
    }

    // 系统语言
    fun changeLanguage() {
        val value = mutableMapOf<String, Any>().apply {
            put("lang", DeviceTools.getLanguageCode())
        }
        GsonTools.beanToString(value)?.let {
            val obj = SceneMessageRequestBody(SceneMessageType.Language.value, it)
            sendSceneMessage(GsonTools.beanToString(obj))
        }
    }

    // 发送给unity 消息编码
    private fun sendSceneMessage(msg: String?) {
        if (scene.sendMessageToScene(msg?.toByteArray()) == 0) {
            LogTools.d(tag, "sendSceneMessage done, $msg")
        } else {
            LogTools.e(tag, "sendSceneMessage fail, $msg")
        }
    }

    // 收到unity 消息解码
    fun handleSceneMessage(message: String) {
        LogTools.d(tag, "ready to handle scene message, $message")
        GsonTools.toBean(message, SceneMessageReceiveBody::class.java)?.let { body ->
            when (body.key) {
                SceneMessageType.Position.value ->
                    GsonTools.toBean(body.value.toString(), SceneMessageReceivePositions::class.java)?.let { pos ->
                        messageListeners.forEach {
                            it.onObjectPositionAcquired(pos)
                        }
                    }
                SceneMessageType.Karaoke.value ->
                    GsonTools.toBean(body.value.toString(), SceneMessageReceiveKaraoke::class.java)?.let { karaoke ->
                        messageListeners.forEach {
                            if (karaoke.actionId == KaraokeAction.Start.value) {
                                it.onKaraokeStarted()
                            } else {
                                it.onKaraokeStopped()
                            }
                        }
                    }
                else -> {}
            }
        }
    }

    fun registerListener(listener: SceneCmdListener) {
        messageListeners.add(listener)
    }

    fun unregisterListener(listener: SceneCmdListener) {
        messageListeners.remove(listener)
    }
}

enum class SceneMessageType(val value: String) {
    Position("objectLocation"),
    Karaoke("songAction"),
    SendMessage("chat"),
    Language("systemLang")
}

enum class KaraokeAction(val value: Int) {
    Start(1),
    Stop(2)
}

data class SceneMessageRequestBody constructor(
    val key: String,
    val value: Any
)

data class SceneMessageReceiveBody constructor(
    val key: String,
    val value: Any
)

data class SceneMessageReceiveKaraoke constructor(
    val actionId: Int,
)

data class SceneMessageReceivePositions(
    val objectId: Int,
    val position: FloatArray?,
    val forward: FloatArray?,
    val right: FloatArray?,
    val up: FloatArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SceneMessageReceivePositions

        if (objectId != other.objectId) return false
        if (!position.contentEquals(other.position)) return false
        if (!forward.contentEquals(other.forward)) return false
        if (!right.contentEquals(other.right)) return false
        if (!up.contentEquals(other.up)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = objectId
        result = 31 * result + position.contentHashCode()
        result = 31 * result + forward.contentHashCode()
        result = 31 * result + right.contentHashCode()
        result = 31 * result + up.contentHashCode()
        return result
    }
}

enum class SceneObjectId(val value: Int) {
    TV(1),
    NPC1(2),
    NPC2(3),
    NPC3(4),
}

interface SceneCmdListener {
    fun onObjectPositionAcquired(position: SceneMessageReceivePositions)

    fun onKaraokeStarted()

    fun onKaraokeStopped()
}