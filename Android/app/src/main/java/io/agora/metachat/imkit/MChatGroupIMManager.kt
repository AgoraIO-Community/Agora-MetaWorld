package io.agora.metachat.imkit

import android.content.Context
import android.text.TextUtils
import com.hyphenate.*
import com.hyphenate.chat.*
import com.hyphenate.chat.EMGroupManager.EMGroupStyle
import com.hyphenate.chat.adapter.EMAError
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.service.MChatServiceProtocol
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ThreadTools

/**
 * @author create by zhangwei03
 */
class MChatGroupIMManager private constructor() : EMConnectionListener, EMMessageListener, EMGroupChangeListener {

    private lateinit var context: Context
    private lateinit var chatServiceProtocol: MChatServiceProtocol
    private var groupId: String = ""
    private var ownerId: String = ""
    private val notAddedDataList = mutableListOf<MChatMessageModel>() // 未添加到的数据
    private val allData = mutableListOf<MChatMessageModel>()

    companion object {
        private const val TAG = "MChatroomIMManager"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_PORTRAIT_INDEX = "portrait_index"
        private const val GROUP_MAX_USERS = 20 // 群组默认最大20人

        private val groupIMManager by lazy {
            MChatGroupIMManager()
        }

        @JvmStatic
        fun instance(): MChatGroupIMManager = groupIMManager
    }

    fun getAllData(): List<MChatMessageModel> {
        if (allData.isEmpty()) {
            getAllMsgList()
        }
        return allData
    }

    fun getAllMsgList(): List<MChatMessageModel> {
        val data = mutableListOf<MChatMessageModel>()
        data.addAll(notAddedDataList)
        notAddedDataList.clear()
        allData.addAll(data)
        return data
    }

    fun isRoomOwner(): Boolean {
        return ownerId.isNotEmpty() && ownerId == MChatKeyCenter.imUid
    }

    fun initConfig(context: Context, imKey: String) {
        this.context = context
        // 在主进程中进行初始化：
        val options = EMOptions().apply {
            appKey = imKey
            autoLogin = false
        }
        if (!DeviceTools.isMainProcess(context)) {
            LogTools.e(TAG, "im init need the main process!")
            return
        }
        EMClient.getInstance().init(context, options)
        // 注册连接状态监听
        EMClient.getInstance().addConnectionListener(this)
        chatServiceProtocol = MChatServiceProtocol.getImplInstance()
    }

    /**
     * 创建房间或者加入房间前需要创建环信账号(如果本地没有的话)，登录环信IM
     * @param loginCallBack on io thread
     *  <-- http://docs-im-beta.easemob.com/document/android/overview.html -->
     */
    fun loginIMTask(loginCallBack: (error: Int) -> Unit) {
        ThreadTools.get().runOnIOThread {
            val imUid = MChatKeyCenter.imUid
            val imPassword = MChatKeyCenter.imPassword
            // 本地没有账号则创建im 账号
            if (!MChatKeyCenter.accountCreated()) {
                try {
                    EMClient.getInstance().createAccount(imUid, imPassword)
                    MChatKeyCenter.setAccountCreated()
                } catch (e: Exception) {
                    LogTools.e(TAG, "im create account error:${e.message}")
                    loginCallBack.invoke(MChatServiceProtocol.ERR_CREATE_ACCOUNT_ERROR)
                    return@runOnIOThread
                }
            }
            EMClient.getInstance().login(imUid, imPassword, object : EMCallBack {
                override fun onSuccess() {
                    LogTools.d(TAG, "im login success")
                    loginCallBack.invoke(MChatServiceProtocol.ERR_LOGIN_SUCCESS)
                }

                override fun onError(code: Int, error: String?) {
                    if (code == EMAError.USER_ALREADY_LOGIN) {
                        LogTools.d(TAG, "im already login")
                        loginCallBack.invoke(MChatServiceProtocol.ERR_LOGIN_SUCCESS)
                    } else {
                        LogTools.e(TAG, "im login code:$code,error:$error")
                        loginCallBack.invoke(MChatServiceProtocol.ERR_LOGIN_ERROR)
                    }
                }
            })
        }
    }

    /**
     * 创建群组
     * @param createCallBack 异步回调
     * <-- http://docs-im-beta.easemob.com/document/android/group_manage.html -->
     */
    fun createGroupTask(groupName: String, createCallBack: (groupId: String, error: Int) -> Unit) {
        val option = EMGroupOptions().apply {
            maxUsers = GROUP_MAX_USERS
            style = EMGroupStyle.EMGroupStylePublicOpenJoin
        }
        EMClient.getInstance().groupManager().asyncCreateGroup(groupName, null, emptyArray(), null, option, object :
            EMValueCallBack<EMGroup> {
            override fun onSuccess(value: EMGroup) {
                val groupId = value.groupId
                LogTools.d(TAG, "im create group success:${value.groupName},$groupId")
                createCallBack.invoke(groupId, MChatServiceProtocol.ERR_CREATE_GROUP_SUCCESS)
            }

            override fun onError(code: Int, errorMsg: String?) {
                LogTools.e(TAG, "im create group failed code:$code,errorMsg:$errorMsg")
                createCallBack.invoke("", MChatServiceProtocol.ERR_CREATE_GROUP_ERROR)
            }
        })
    }

    /**
     * 加入群组
     * @param groupId 群组id
     * @param ownerId 房主id
     * @param joinGroupCallback 异步回调
     */
    fun joinGroupTask(groupId: String, ownerId: String, joinGroupCallback: (error: Int) -> Unit) {
        loginIMTask { loginResult ->
            if (loginResult == MChatServiceProtocol.ERR_LOGIN_SUCCESS) {
                EMClient.getInstance().groupManager().asyncJoinGroup(groupId, object : EMCallBack {
                    override fun onSuccess() {
                        this@MChatGroupIMManager.groupId = groupId
                        this@MChatGroupIMManager.ownerId = ownerId
                        // 注册消息监听
                        EMClient.getInstance().chatManager().addMessageListener(this@MChatGroupIMManager)
                        // 注册群组监听
                        EMClient.getInstance().groupManager().addGroupChangeListener(this@MChatGroupIMManager)
                        LogTools.e(TAG, "im join group success")
                        joinGroupCallback.invoke(MChatServiceProtocol.ERR_JOIN_GROUP_SUCCESS)
                    }

                    override fun onError(code: Int, errorMsg: String?) {
                        when (code) {
                            EMAError.GROUP_ALREADY_JOINED -> {
                                this@MChatGroupIMManager.groupId = groupId
                                this@MChatGroupIMManager.ownerId = ownerId
                                // 注册消息监听
                                EMClient.getInstance().chatManager().addMessageListener(this@MChatGroupIMManager)
                                // 注册群组监听
                                EMClient.getInstance().groupManager().addGroupChangeListener(this@MChatGroupIMManager)
                                LogTools.e(TAG, "User already joined the group")
                                joinGroupCallback.invoke(MChatServiceProtocol.ERR_JOIN_GROUP_SUCCESS)
                            }
                            EMAError.GROUP_INVALID_ID -> {
                                LogTools.e(TAG, "im join group failed code:$code,errorMsg:$errorMsg")
                                joinGroupCallback.invoke(MChatServiceProtocol.ERR_GROUP_UNAVAILABLE)
                            }
                            else -> {
                                LogTools.e(TAG, "im join group failed code:$code,errorMsg:$errorMsg")
                                joinGroupCallback.invoke(MChatServiceProtocol.ERR_JOIN_GROUP_ERROR)
                            }
                        }
                    }
                })

            } else {
                joinGroupCallback.invoke(loginResult)
            }
        }
    }


    /**
     * 离开群组，普通用户退出群组，房主离开则解散群组
     */
    fun leaveGroupTask(leaveCallback: (error: Int) -> Unit) {
        if (checkEmptyGroup()) return
        if (isRoomOwner()) { // 房主
            EMClient.getInstance().groupManager().asyncDestroyGroup(groupId, object : EMCallBack {
                override fun onSuccess() {
                    LogTools.d(TAG, "im destroy group success:$groupId")
                    reset()
                    leaveCallback.invoke(MChatServiceProtocol.ERR_LEAVE_GROUP_SUCCESS)
                }

                override fun onError(code: Int, error: String?) {
                    LogTools.d(TAG, "im destroy group failed code:$code,error:$error")
                    leaveCallback.invoke(MChatServiceProtocol.ERR_LEAVE_GROUP_ERROR)
                }
            })
        } else {
            EMClient.getInstance().groupManager().asyncLeaveGroup(groupId, object : EMCallBack {
                override fun onSuccess() {
                    LogTools.d(TAG, "im leave group success:$groupId")
                    reset()
                    leaveCallback.invoke(MChatServiceProtocol.ERR_LEAVE_GROUP_SUCCESS)
                }

                override fun onError(code: Int, error: String?) {
                    LogTools.d(TAG, "im leave group failed code:$code,error:$error")
                    leaveCallback.invoke(MChatServiceProtocol.ERR_LEAVE_GROUP_ERROR)
                }
            })
        }
    }

    private fun checkEmptyGroup(): Boolean {
        if (groupId.isEmpty()) {
            LogTools.e(TAG, "group id is null!")
            return true
        }
        return false
    }

    fun reset() {
        // 移除消息监听
        EMClient.getInstance().chatManager().removeMessageListener(this)
        // 移除群组监听
        EMClient.getInstance().groupManager().removeGroupChangeListener(this)
        notAddedDataList.clear()
        allData.clear()
        groupId = ""
        ownerId = ""
    }

    //--------------------Connection start-----------------
    override fun onConnected() {
        LogTools.d(TAG, "onConnected")
    }

    override fun onDisconnected(errorCode: Int) {
        LogTools.d(TAG, "onDisconnected errorCode:$errorCode")
    }

    override fun onTokenExpired() {
        LogTools.d(TAG, "onTokenExpired")
    }

    override fun onTokenWillExpire() {
        LogTools.d(TAG, "onTokenWillExpire")
    }

    override fun onLogout(errorCode: Int) {
        LogTools.d(TAG, "onLogout errorCode:$errorCode")
    }
    //--------------------Connection end-----------------


    //--------------------Message start-----------------
    /**
     * 收到消息，遍历消息队列，解析和显示。
     * @param messages 消息队列
     */
    override fun onMessageReceived(messages: List<EMMessage>) {
        for (i in messages.indices) {
            val emMessage = messages[i]
            // 只判断文本消息
            if (emMessage.type != EMMessage.Type.TXT) continue
            val message = parseChatMessage(emMessage)
            if (TextUtils.equals(groupId, message.conversationId)) {
                // 同一个群组的才添加
                notAddedDataList.add(message)
                chatServiceProtocol.getSubscribeDelegates().forEach { chatDelegate ->
                    chatDelegate.onReceiveTextMsg(message.conversationId ?: "", message)
                }
            }
        }
    }

    /**
     * 发送文本消息
     * @param content 文本内容
     * @param nickName 用户昵称
     * @param portraitIndex 头像
     * @param sendMsgCallback 回调
     */
    fun sendTxtMsg(content: String, nickName: String, portraitIndex: Int, sendMsgCallback: (result: Boolean) -> Unit) {
        if (checkEmptyGroup()) return
        val message: EMMessage = EMMessage.createTextSendMessage(content, groupId)
        message.setAttribute(KEY_NICKNAME, nickName)
        message.setAttribute(KEY_PORTRAIT_INDEX, portraitIndex)
        message.chatType = EMMessage.ChatType.GroupChat
        message.setMessageStatusCallback(object : EMCallBack {
            override fun onSuccess() {
                LogTools.e(TAG, "sendTxtMsg success")
                notAddedDataList.add(parseChatMessage(message))
                sendMsgCallback.invoke(true)
            }

            override fun onError(code: Int, error: String?) {
                LogTools.e(TAG, "sendTxtMsg failed code:$code,error:$error")
                sendMsgCallback.invoke(false)
            }
        })
        EMClient.getInstance().chatManager().sendMessage(message)
    }

    /**
     * 解析环信IM 消息
     * @param chatMessage 环信IM 消息对象，代表一条发送或接收到的消息。
     */
    fun parseChatMessage(chatMessage: EMMessage): MChatMessageModel {
        val chatMsgModel = MChatMessageModel().apply {
            from = chatMessage.from
            to = chatMessage.to
            conversationId = chatMessage.conversationId()
            messageId = chatMessage.msgId
            timeStamp = chatMessage.msgTime
            nickname = chatMessage.ext()[KEY_NICKNAME]?.toString()
            portraitIndex = chatMessage.ext()[KEY_PORTRAIT_INDEX]?.toString()?.toIntOrNull() ?: -1
        }
        if (chatMessage.body is EMTextMessageBody) {
            chatMsgModel.content = (chatMessage.body as EMTextMessageBody).message
        }
        return chatMsgModel
    }
    //--------------------Message end-----------------


    //--------------------Group start-----------------
    // 当前用户收到了入群邀请。受邀用户会收到该回调。例如，用户 B 邀请用户 A 入群，则用户 A 会收到该回调。
    override fun onInvitationReceived(groupId: String?, groupName: String?, inviter: String?, reason: String?) {}

    // 群主或群管理员收到进群申请。群主和所有管理员收到该回调。
    override fun onRequestToJoinReceived(groupId: String?, groupName: String?, applicant: String?, reason: String?) {}

    // 群主或群管理员同意用户的进群申请。申请人、群主和管理员（除操作者）收到该回调。
    override fun onRequestToJoinAccepted(groupId: String?, groupName: String?, accepter: String?) {}

    // 群主或群管理员拒绝用户的进群申请。申请人、群主和管理员（除操作者）收到该回调。
    override fun onRequestToJoinDeclined(groupId: String?, groupName: String?, decliner: String?, reason: String?) {}

    // 用户同意进群邀请。邀请人收到该回调。
    override fun onInvitationAccepted(groupId: String?, invite: String?, reason: String?) {}

    // 用户拒绝进群邀请。邀请人收到该回调。
    override fun onInvitationDeclined(groupId: String?, invitee: String?, reason: String?) {}

    // 有成员被移出群组。被踢出群组的成员会收到该回调。
    override fun onUserRemoved(groupId: String, groupName: String?) {
        LogTools.d(TAG, "onUserRemoved groupId:$groupId,groupName:$groupName")
        chatServiceProtocol.getSubscribeDelegates().forEach { chatDelegate ->
            chatDelegate.onUserRemoved(groupId)
        }
    }

    // 群组解散。群主解散群组时，所有群成员均会收到该回调。
    override fun onGroupDestroyed(groupId: String, groupName: String?) {
        LogTools.d(TAG, "onGroupDestroyed groupId:$groupId,groupName:$groupName")
        chatServiceProtocol.getSubscribeDelegates().forEach { chatDelegate ->
            chatDelegate.onGroupDestroyed(groupId)
        }
    }

    // 有用户自动同意加入群组。邀请人收到该回调。
    override fun onAutoAcceptInvitationFromGroup(groupId: String?, inviter: String?, inviteMessage: String?) {}

    // 有成员被加入群组禁言列表。被禁言的成员及群主和群管理员（除操作者外）会收到该回调。
    override fun onMuteListAdded(groupId: String?, mutes: MutableList<String>?, muteExpire: Long) {}

    // 有成员被移出禁言列表。被解除禁言的成员及群主和群管理员（除操作者外）会收到该回调。
    override fun onMuteListRemoved(groupId: String?, mutes: MutableList<String>?) {}

    // 有成员被加入群组白名单。被添加的成员及群主和群管理员（除操作者外）会收到该回调。
    override fun onWhiteListAdded(groupId: String?, whitelist: MutableList<String>?) {}

    // 有成员被移出群组白名单。被移出的成员及群主和群管理员（除操作者外）会收到该回调。
    override fun onWhiteListRemoved(groupId: String?, whitelist: MutableList<String>?) {}

    // 全员禁言状态变化。群组所有成员（除操作者外）会收到该回调。
    override fun onAllMemberMuteStateChanged(groupId: String?, isMuted: Boolean) {}

    // 设置管理员。群主、新管理员和其他管理员会收到该回调。
    override fun onAdminAdded(groupId: String?, administrator: String?) {}

    // 群组管理员被移除。被移出的成员及群主和群管理员（除操作者外）会收到该回调。
    override fun onAdminRemoved(groupId: String?, administrator: String?) {}

    // 群主转移权限。原群主和新群主会收到该回调。
    override fun onOwnerChanged(groupId: String?, newOwner: String?, oldOwner: String?) {}

    // 有新成员加入群组。除了新成员，其他群成员会收到该回调。
    override fun onMemberJoined(groupId: String, member: String) {
        LogTools.d(TAG, "onMemberJoined groupId:$groupId,member:$member")
        chatServiceProtocol.getSubscribeDelegates().forEach { chatDelegate ->
            chatDelegate.onMemberJoined(groupId, member)
        }
    }

    // 有成员主动退出群。除了退群的成员，其他群成员会收到该回调。
    override fun onMemberExited(groupId: String, member: String) {
        LogTools.d(TAG, "onMemberExited groupId:$groupId,member:$member")
        chatServiceProtocol.getSubscribeDelegates().forEach { chatDelegate ->
            chatDelegate.onMemberExited(groupId, member)
        }
    }

    // 群组公告更新。群组所有成员会收到该回调。
    override fun onAnnouncementChanged(groupId: String?, announcement: String?) {}

    // 有成员新上传群组共享文件。群组所有成员会收到该回调。
    override fun onSharedFileAdded(groupId: String?, sharedFile: EMMucSharedFile?) {}

    // 群组共享文件被删除。群组所有成员会收到该回调。
    override fun onSharedFileDeleted(groupId: String?, fileId: String?) {}
    //--------------------Group end-----------------
}