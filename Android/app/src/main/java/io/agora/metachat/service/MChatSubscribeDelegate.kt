package io.agora.metachat.service

import io.agora.metachat.imkit.MChatMessageModel

/**
 * @author create by zhangwei03
 *
 * im 回调协议
 */
interface MChatSubscribeDelegate {

    /**
     * 收到文本消息
     * @param groupId 环信IM 群组id
     * @param message 环信IM 消息
     */
    fun onReceiveTextMsg(groupId: String, message: MChatMessageModel?) {}

    /**
     * 用户离开群组
     * @param groupId 环信IM 群组id
     * @param chatUid 环信IM 用户uid
     */
    fun onMemberExited(groupId: String, chatUid: String) {}

    /**
     * 用户加入群组
     * @param groupId 环信IM 群组id
     * @param chatUid 环信IM 用户uid
     */
    fun onMemberJoined(groupId: String, chatUid: String) {}

    /**
     * 群组解散
     * @param groupId 环信IM 群组id
     */
    fun onGroupDestroyed(groupId: String) {}

    /**
     * 当前用户被踢出群组
     * @param groupId 环信IM 群组id
     */
    fun onUserRemoved(groupId: String) {}

    /**
     * k 歌
     */
    fun onKaraoke(start: Boolean) {}

    /**
     * 原唱
     */
    fun onOriginalSinging(value: Boolean) {}

    /**
     * 耳返
     */
    fun onEarphoneMonitoring(value: Boolean) {}

    /**
     * 升降调
     */
    fun onChangeSongKey(value: Int) {}

    /**
     * 伴奏
     */
    fun onAccompanimentMusic(value: Int) {}

    /**
     * 音效
     */
    fun onAudioEffect(value: Int) {}
}