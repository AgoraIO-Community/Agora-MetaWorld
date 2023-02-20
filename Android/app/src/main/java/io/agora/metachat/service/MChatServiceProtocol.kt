package io.agora.metachat.service

import io.agora.metachat.MChatApp
import io.agora.metachat.tools.LogTools

/**
 * metachat 协议
 *
 * @author create by zhangwei03
 */
interface MChatServiceProtocol {

    enum class MChatSubscribe {
        SubscribeCreated,  //创建
        SubscribeDeleted,  //删除
        SubscribeUpdated,  //更新
    }

    companion object {
        const val ERR_OK = 0
        const val ERR_FAILED = 1
        const val ERR_CREATE_ACCOUNT_ERROR = 2
        const val ERR_LOGIN_ERROR = 3
        const val ERR_LOGIN_SUCCESS = 4
        const val ERR_GROUP_UNAVAILABLE = 5
        const val ERR_PASSWORD_ERROR = 6
        const val ERR_CREATE_GROUP_ERROR = 7
        const val ERR_CREATE_GROUP_SUCCESS = 8
        const val ERR_JOIN_GROUP_ERROR = 9
        const val ERR_JOIN_GROUP_SUCCESS = 10
        const val ERR_LEAVE_GROUP_ERROR = 11
        const val ERR_LEAVE_GROUP_SUCCESS = 12

        private val instance by lazy {
            MChatSyncManagerServiceImp(MChatApp.instance()) { error ->
                LogTools.e("MChatSyncManager", "${error?.message}")
            }
        }

        @JvmStatic
        fun getImplInstance(): MChatServiceProtocol = instance
    }

    /**
     * 注册订阅
     * @param delegate 聊天室内IM回调处理
     */
    fun subscribeEvent(delegate: MChatSubscribeDelegate)

    /**取消订阅*/
    fun unsubscribeEvent(delegate: MChatSubscribeDelegate)

    fun getSubscribeDelegates(): Set<MChatSubscribeDelegate>

    /**reset*/
    fun reset()

    /**房间列表*/
    fun fetchRoomList(completion: (error: Int, list: List<MChatRoomModel>?) -> Unit)

    /**创建房间*/
    fun createRoom(
        inputModel: MChatCreateRoomInputModel,
        completion: (error: Int, result: MChatCreateRoomOutputModel?) -> Unit
    )

    /**加入房间*/
    fun joinRoom(
        inputModel: MChatJoinRoomInputModel,
        completion: (error: Int, result: MChatJoinRoomOutputModel?) -> Unit
    )

    /**离开房间*/
    fun leaveRoom(completion: (error: Int) -> Unit)

    /**开始k 歌，通知其他人*/
    fun sendStartKaraoke(completion: (result: Boolean) -> Unit)

    /**结束k 歌，通知其他人*/
    fun sendStopKaraoke(completion: (result: Boolean) -> Unit)

    /**开启原唱*/
    fun enableOriginalSinging(completion: (result: Boolean) -> Unit)

    /**关闭原唱*/
    fun disableOriginalSinging(completion: (result: Boolean) -> Unit)

    /**开启耳返*/
    fun enableEarphoneMonitoring(completion: (result: Boolean) -> Unit)

    /**关闭耳返*/
    fun disableEarphoneMonitoring(completion: (result: Boolean) -> Unit)

    /**升降调*/
    fun changePitchSong(value: Int, completion: (result: Boolean) -> Unit)

    /**伴奏音量*/
    fun changeAccompanimentVolume(value: Int, completion: (result: Boolean) -> Unit)

    /** 音效*/
    fun changeAudioEffect(value: Int, completion: (result: Boolean) -> Unit)
}