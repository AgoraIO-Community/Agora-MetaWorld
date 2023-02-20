package io.agora.metachat.imkit

/**
 * @author create by zhangwei03
 */
data class MChatMessageModel(
    var from: String? = null,
    var to: String? = null,
    var messageId: String? = null,
    var conversationId: String? = null,
    var content: String? = null,
    var timeStamp:Long = 0L,
    var nickname: String? = null,
    var portraitIndex: Int = -1,
)
