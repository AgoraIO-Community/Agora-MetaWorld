package io.agora.metachat.service

import android.content.Context
import io.agora.metachat.game.sence.MChatContext
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.imkit.MChatGroupIMManager
import io.agora.metachat.tools.GsonTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ThreadTools
import io.agora.rtc2.Constants
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.DataListCallback
import io.agora.syncmanager.rtm.Sync.JoinSceneCallback


/**
 * @author create by zhangwei03
 */
class MChatSyncManagerServiceImp constructor(
    val context: Context, val errorHandler: ((Exception?) -> Unit)
) : MChatServiceProtocol {

    private val TAG = "MChatSyncManager"

    @Volatile
    private var syncUtilsInit = false

    @Volatile
    private var currRoomId: String = ""
    private var mSceneReference: SceneReference? = null
    private val roomMap = mutableMapOf<String, MChatRoomModel>() // key: roomNo
    private val roomSubscribeDelegates = mutableSetOf<MChatSubscribeDelegate>() // im 回调协议集合

    private fun initScene(completion: () -> Unit) {
        if (syncUtilsInit) {
            completion.invoke()
            return
        }
        val chatSceneId = "scene_mateChat"
        Sync.Instance().init(context,
            mapOf(Pair("appid", MChatKeyCenter.RTC_APP_ID), Pair("defaultChannel", chatSceneId)),
            object : Sync.Callback {
                override fun onSuccess() {
                    ThreadTools.get().runOnMainThread {
                        Sync.Instance().joinScene(chatSceneId, object : JoinSceneCallback {
                            override fun onSuccess(scene: SceneReference?) {
                                scene?.subscribe(object : Sync.EventListener {
                                    override fun onCreated(item: IObject?) {}

                                    override fun onUpdated(item: IObject?) {
                                        item ?: return
                                        val roomInfo = item.toObject(MChatRoomModel::class.java)
                                        roomMap[roomInfo.roomId] = roomInfo
                                        LogTools.d("syncManager RoomChanged onUpdated:$roomInfo")
                                    }

                                    override fun onDeleted(item: IObject?) {
                                        item ?: return
                                        val roomInfo = roomMap[item.id] ?: return
                                        resetCacheInfo(roomInfo.roomId, true)
                                        LogTools.d("syncManager RoomChanged onDeleted:${roomInfo.roomId}")
                                    }

                                    override fun onSubscribeError(ex: SyncManagerException?) {
                                        errorHandler.invoke(ex)
                                    }
                                })
                                syncUtilsInit = true
                                ThreadTools.get().runOnMainThread {
                                    LogTools.d("SyncManager init success")
                                    completion.invoke()
                                }
                            }

                            override fun onFail(exception: SyncManagerException?) {
                                ThreadTools.get().runOnMainThread {
                                    LogTools.e("SyncManager init failed:${exception?.message}")
                                    errorHandler.invoke(exception)
                                }
                            }
                        })
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadTools.get().runOnMainThread {
                        LogTools.e("SyncManager init failed:${exception?.message}")
                        errorHandler.invoke(exception)
                    }
                }
            })
    }

    private fun resetCacheInfo(roomId: String, isRoomDestroyed: Boolean = false) {
        if (isRoomDestroyed) {
            roomMap.remove(roomId)
        }
        mSceneReference = null
    }

    override fun subscribeEvent(delegate: MChatSubscribeDelegate) {
        roomSubscribeDelegates.add(delegate)
    }

    override fun unsubscribeEvent(delegate: MChatSubscribeDelegate) {
        roomSubscribeDelegates.remove(delegate)
    }

    override fun getSubscribeDelegates(): Set<MChatSubscribeDelegate> {
        return roomSubscribeDelegates
    }

    override fun reset() {
        if (syncUtilsInit) {
            Sync.Instance().destroy()
            syncUtilsInit = false
        }
    }

    override fun fetchRoomList(completion: (error: Int, list: List<MChatRoomModel>?) -> Unit) {
        initScene {
            Sync.Instance().getScenes(object : DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    LogTools.d(TAG, "fetchRoomList success room size:${result?.size}")
                    roomMap.clear()
                    val ret = mutableListOf<MChatRoomModel>()
                    result?.forEach { iObj ->
                        try {
                            val chatRoom = iObj.toObject(MChatRoomModel::class.java)
                            ret.add(chatRoom)
                            roomMap[chatRoom.roomId] = chatRoom
                        } catch (e: Exception) {
                            LogTools.e(TAG, "fetchRoomList failed:${e.message}")
                        }
                    }
                    //按照创建时间顺序排序
                    ret.sortBy { it.createdAt }
                    ThreadTools.get().runOnMainThread {
                        completion.invoke(MChatServiceProtocol.ERR_OK, ret)
                    }
                }

                override fun onFail(e: SyncManagerException?) {
                    LogTools.e(TAG, "fetchRoomList failed:${e?.message}")
                    ThreadTools.get().runOnMainThread {
                        completion.invoke(MChatServiceProtocol.ERR_FAILED, null)
                    }
                }
            })
        }
    }

    override fun createRoom(
        inputModel: MChatCreateRoomInputModel, completion: (error: Int, result: MChatCreateRoomOutputModel?) -> Unit
    ) {
        // 1、根据用户输入信息创建房间信息
        val chatRoomModel = MChatRoomModel().apply {
            isPrivate = inputModel.isPrivate
            roomName = inputModel.roomName
            roomCoverIndex = inputModel.roomCoverIndex
            roomPassword = inputModel.password
            ownerId = MChatKeyCenter.curUid
        }
        // 2.登录环信IM
        MChatGroupIMManager.instance().loginIMTask { loginResult ->
            if (loginResult == MChatServiceProtocol.ERR_LOGIN_SUCCESS) {
                // 3、创建群组
                MChatGroupIMManager.instance().createGroupTask(inputModel.roomName) { groupId, createResult ->
                    if (createResult == MChatServiceProtocol.ERR_CREATE_GROUP_SUCCESS) {
                        // 4、创建syncManager 房间
                        chatRoomModel.roomId = groupId
                        chatRoomModel.createdAt = System.currentTimeMillis()
                        initScene {
                            val scene = Scene().apply {
                                id = chatRoomModel.roomId
                                userId = chatRoomModel.ownerId.toString()
                                property = GsonTools.beanToMap(chatRoomModel)
                            }
                            Sync.Instance().createScene(scene, object : Sync.Callback {
                                override fun onSuccess() {
                                    LogTools.d(TAG, "syncManager createScene success sceneId:${scene.id}")
                                    roomMap[chatRoomModel.roomId] = chatRoomModel
                                    ThreadTools.get().runOnMainThread {
                                        val ret =
                                            MChatCreateRoomOutputModel(chatRoomModel.roomId, chatRoomModel.roomPassword)
                                        completion.invoke(MChatServiceProtocol.ERR_OK, ret)
                                    }
                                }

                                override fun onFail(e: SyncManagerException?) {
                                    LogTools.d(TAG, "syncManager createScene failed sceneId:${scene.id},${e?.message}")
                                    ThreadTools.get().runOnMainThread {
                                        completion.invoke(MChatServiceProtocol.ERR_FAILED, null)
                                    }
                                }
                            })
                        }
                    } else {
                        ThreadTools.get().runOnMainThread {
                            completion.invoke(MChatServiceProtocol.ERR_OK, null)
                        }
                    }
                }
            } else {
                ThreadTools.get().runOnMainThread {
                    completion.invoke(loginResult, null)
                }
            }
        }
    }

    override fun joinRoom(
        inputModel: MChatJoinRoomInputModel, completion: (error: Int, result: MChatJoinRoomOutputModel?) -> Unit
    ) {
        initScene {
            //1. check room
            val curRoomInfo = roomMap[inputModel.roomId]
            if (curRoomInfo == null) {
                LogTools.e(TAG, "joinRoom The room is not existent ")
                completion.invoke(MChatServiceProtocol.ERR_GROUP_UNAVAILABLE, null)
                return@initScene
            }
            if (curRoomInfo.isPrivate && curRoomInfo.roomPassword != inputModel.password) {
                LogTools.e(TAG, "joinRoom The password is error!")
                completion.invoke(MChatServiceProtocol.ERR_PASSWORD_ERROR, null)
                return@initScene
            }
            //2. join scene
            Sync.Instance().joinScene(inputModel.roomId, object : JoinSceneCallback {
                override fun onSuccess(sceneReference: SceneReference?) {
                    mSceneReference = sceneReference
                    currRoomId = inputModel.roomId
                    LogTools.d(TAG, "joinRoom success:${sceneReference?.id},update room")
                    curRoomInfo.memberCount = curRoomInfo.memberCount + 1
                    val updateMap = hashMapOf<String, Any>().apply {
                        putAll(GsonTools.beanToMap(curRoomInfo))
                    }
                    updateRoom(updateMap) { result ->
                        if (result) {
                            //3. join im group
                            MChatGroupIMManager.instance()
                                .joinGroupTask(inputModel.roomId, curRoomInfo.ownerId.toString()) { joinGroupResult ->
                                    when (joinGroupResult) {
                                        MChatServiceProtocol.ERR_JOIN_GROUP_SUCCESS -> {
                                            val outputModel = MChatJoinRoomOutputModel().apply {
                                                roomId = inputModel.roomId
                                                roomName = curRoomInfo.roomName
                                                roomIconIndex = curRoomInfo.roomCoverIndex
                                                ownerId = curRoomInfo.ownerId
                                            }
                                            ThreadTools.get().runOnMainThread {
                                                completion.invoke(MChatServiceProtocol.ERR_OK, outputModel)
                                            }
                                        }
                                        MChatServiceProtocol.ERR_GROUP_UNAVAILABLE -> {
                                            // 环信房间解散了，移除syncManager 房间
                                            LogTools.d(TAG, "im room unavailable, delete sync manager room")
                                            deleteRoom {
                                                completion.invoke(joinGroupResult, null)
                                            }
                                        }
                                        else -> {
                                            ThreadTools.get().runOnMainThread {
                                                completion.invoke(joinGroupResult, null)
                                            }
                                        }
                                    }
                                }
                        } else {
                            ThreadTools.get().runOnMainThread {
                                completion.invoke(MChatServiceProtocol.ERR_FAILED, null)
                            }
                        }
                    }
                }

                override fun onFail(e: SyncManagerException?) {
                    LogTools.e(TAG, "joinRoom failed:${e?.message}")
                    ThreadTools.get().runOnMainThread {
                        completion.invoke(MChatServiceProtocol.ERR_FAILED, null)
                    }
                }
            })
        }
    }

    private fun deleteRoom(completion: (result: Boolean) -> Unit) {
        // 移除房间
        mSceneReference?.delete(object : Sync.Callback {
            override fun onSuccess() {
                LogTools.d(TAG, "syncManager delete room success")
                resetCacheInfo(currRoomId, true)
                ThreadTools.get().runOnMainThread {
                    completion.invoke(true)
                }
            }

            override fun onFail(e: SyncManagerException?) {
                LogTools.e(TAG, "syncManager delete room failed:${e?.message}")
                ThreadTools.get().runOnMainThread {
                    completion.invoke(false)
                }
            }
        })
    }

    private fun updateRoom(updateMap: HashMap<String, Any>, completion: (result: Boolean) -> Unit) {
        mSceneReference?.update(updateMap, object : Sync.DataItemCallback {
            override fun onSuccess(result: IObject?) {
                LogTools.d(TAG, "syncManager update success")
                ThreadTools.get().runOnMainThread {
                    completion.invoke(true)
                }
            }

            override fun onFail(exception: SyncManagerException?) {
                LogTools.e(TAG, "syncManager update failed:${exception?.message}")
                ThreadTools.get().runOnMainThread {
                    completion.invoke(false)
                }
            }
        })
    }

    override fun leaveRoom(completion: (error: Int) -> Unit) {
        val curRoomInfo = roomMap[currRoomId] ?: return
        if (curRoomInfo.ownerId == MChatKeyCenter.curUid) {
            // 移除房间
            deleteRoom { result ->
                completion.invoke(if (result) MChatServiceProtocol.ERR_OK else MChatServiceProtocol.ERR_FAILED)
            }
        } else {
            curRoomInfo.memberCount = curRoomInfo.memberCount - 1
            val updateMap = hashMapOf<String, Any>().apply {
                putAll(GsonTools.beanToMap(curRoomInfo))
            }
            LogTools.d(TAG, "leaveRoom member count $curRoomInfo")
            updateRoom(updateMap) { result ->
                resetCacheInfo(currRoomId, false)
                completion.invoke(if (result) MChatServiceProtocol.ERR_OK else MChatServiceProtocol.ERR_FAILED)
            }
        }
    }

    override fun sendStartKaraoke(completion: (result: Boolean) -> Unit) {
        val data = StreamDataBaseBody(MChatConstant.StreamParam.ACTION_KARAOKE, MChatConstant.StreamParam.VALUE_OPEN)
        val result= sendDataStream(GsonTools.beanToString(data))
        completion.invoke(result)
    }

    override fun sendStopKaraoke(completion: (result: Boolean) -> Unit) {
        val data = StreamDataBaseBody(MChatConstant.StreamParam.ACTION_KARAOKE, MChatConstant.StreamParam.VALUE_CLOSE)
        val result= sendDataStream(GsonTools.beanToString(data))
        completion.invoke(result)
    }

    override fun enableOriginalSinging(completion: (result: Boolean) -> Unit) {
        val data =
            StreamDataBaseBody(MChatConstant.StreamParam.ACTION_ORIGINAL_SINGING, MChatConstant.StreamParam.VALUE_OPEN)
        val result= sendDataStream(GsonTools.beanToString(data))
        completion.invoke(result)
    }

    override fun disableOriginalSinging(completion: (result: Boolean) -> Unit) {
        val data =
            StreamDataBaseBody(MChatConstant.StreamParam.ACTION_ORIGINAL_SINGING, MChatConstant.StreamParam.VALUE_CLOSE)
        val result= sendDataStream(GsonTools.beanToString(data))
        completion.invoke(result)
    }

    override fun enableEarphoneMonitoring(completion: (result: Boolean) -> Unit) {
        val data = StreamDataBaseBody(
            MChatConstant.StreamParam.ACTION_EARPHONE_MONITORING,
            MChatConstant.StreamParam.VALUE_OPEN
        )
        val result= sendDataStream(GsonTools.beanToString(data))
        completion.invoke(result)
    }

    override fun disableEarphoneMonitoring(completion: (result: Boolean) -> Unit) {
        val data = StreamDataBaseBody(
            MChatConstant.StreamParam.ACTION_EARPHONE_MONITORING,
            MChatConstant.StreamParam.VALUE_CLOSE
        )
        val result= sendDataStream(GsonTools.beanToString(data))
        completion.invoke(result)
    }

    override fun changePitchSong(value: Int, completion: (result: Boolean) -> Unit) {
        val data = StreamDataBaseBody(MChatConstant.StreamParam.ACTION_SONG_KEY, value)
        val result= sendDataStream(GsonTools.beanToString(data))
        completion.invoke(result)
    }

    override fun changeAccompanimentVolume(value: Int, completion: (result: Boolean) -> Unit) {
        val data = StreamDataBaseBody(MChatConstant.StreamParam.ACTION_ACCOMPANIMENT, value)
        val result= sendDataStream(GsonTools.beanToString(data))
        completion.invoke(result)
    }

    override fun changeAudioEffect(value: Int, completion: (result: Boolean) -> Unit) {
        val data = StreamDataBaseBody(MChatConstant.StreamParam.ACTION_AUDIO_EFFECT, value)
        val result= sendDataStream(GsonTools.beanToString(data))
        completion.invoke(result)
    }

    private lateinit var chatContext: MChatContext

    private fun sendDataStream(data: String?): Boolean {
        data ?: return false
        LogTools.d(TAG,"sendDataStream $data")
        if (!this::chatContext.isInitialized) {
            chatContext = MChatContext.instance()
        }
        return chatContext.sendStreamMessage(data) == Constants.ERR_OK
    }
}