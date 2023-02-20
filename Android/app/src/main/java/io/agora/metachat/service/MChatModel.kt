package io.agora.metachat.service

import java.io.Serializable

/**
 * @author create by zhangwei03
 */
interface IKeepProguard

interface MChatBaseModel : IKeepProguard, Serializable

/**创建房间*/
data class MChatCreateRoomInputModel constructor(
    val roomName: String = "",
    val roomId: String = "",
    var roomCoverIndex: Int = 0,
    val isPrivate: Boolean = false,
    val password: String = "",
) : MChatBaseModel

data class MChatCreateRoomOutputModel constructor(
    val roomId: String = "",
    val password: String = ""
) : MChatBaseModel

/**加入房间*/
data class MChatJoinRoomInputModel constructor(
    val roomId: String = "",
    val password: String = ""
) : MChatBaseModel

data class MChatJoinRoomOutputModel constructor(
    var roomId: String = "",
    var roomName: String = "",
    var roomIconIndex: Int = 0,
    var ownerId: Int = -1,
) : MChatBaseModel

/**房间数据*/
data class MChatRoomModel constructor(
    var roomId: String = "", // agora channelName, chat roomId
    var isPrivate: Boolean = false,
    var roomName: String = "",
    var roomCoverIndex: Int = 0,
    var createdAt: Long = 0L,
    var roomPassword: String = "",
    var memberCount: Int = 0,
    var ownerId: Int = -1,
) : MChatBaseModel

/**data stream 数据流*/
data class StreamDataBaseBody constructor(
    val action: Int,
    val msg: Any
)