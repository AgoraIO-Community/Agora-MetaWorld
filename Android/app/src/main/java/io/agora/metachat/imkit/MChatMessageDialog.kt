package io.agora.metachat.imkit

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.EditorInfo
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.databinding.MchatDialogMessageBinding
import io.agora.metachat.databinding.MchatItemMessageBinding
import io.agora.metachat.game.sence.MChatContext
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.service.MChatSubscribeDelegate
import io.agora.metachat.service.MChatServiceProtocol
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ThreadTools
import io.agora.metachat.widget.OnIntervalClickListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author create by zhangwei03
 */
class MChatMessageDialog constructor() : BaseFragmentDialog<MchatDialogMessageBinding>() {

    private val chatContext by lazy {
        MChatContext.instance()
    }

    private val chatServiceProtocol: MChatServiceProtocol = MChatServiceProtocol.getImplInstance()

    private var messageAdapter: BaseQuickAdapter<MChatMessageModel, MChatMessageAdapter.VH>? = null

    private val chatDelegate = object: MChatSubscribeDelegate {
        override fun onReceiveTextMsg(groupId: String, message: MChatMessageModel?) {
            // 收到消息后刷新消息列表
            ThreadTools.get().runOnMainThread {
                refreshMessage()
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogMessageBinding {
        return MchatDialogMessageBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setOnShowListener {
            chatServiceProtocol.subscribeEvent(chatDelegate)
        }
        dialog?.setOnDismissListener {
            onDismiss(it)
            chatServiceProtocol.unsubscribeEvent(chatDelegate)
        }
        initView()
    }

    private fun initView() {
        messageAdapter = MChatMessageAdapter()
        binding?.apply {
            setDialogSize(root)
            rvMessageContent.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            messageAdapter?.addAll(MChatGroupIMManager.instance().getAllData())
            rvMessageContent.adapter = messageAdapter
            ivSendMessage.setOnClickListener(OnIntervalClickListener(this@MChatMessageDialog::onClickSend))
            etMessage.setOnEditorActionListener { textView, actionId, keyEvent ->
                when (actionId and EditorInfo.IME_MASK_ACTION) {
                    EditorInfo.IME_ACTION_DONE -> {
                        val message = binding?.etMessage?.text?.trim()?.toString()
                        sendMessage(message)
                    }
                    else -> {}
                }
                false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.attributes.windowAnimations = R.style.mchat_anim_bottom_to_top
            // Remove the system default rounded corner background
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(0f)
            window.decorView.setPadding(0, 0, 0, 0)

            window.attributes.apply {
                width = DeviceTools.dp2px(295).toInt()
                activity?.let {
                    height = DeviceTools.screenHeight(it)
                }
                gravity = Gravity.START
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
                window.attributes = this
            }
        }
    }

    private fun setDialogSize(view: View) {
        val marginParams: MarginLayoutParams = view.layoutParams as MarginLayoutParams
        marginParams.topMargin = DeviceTools.dp2px(60).toInt()
        marginParams.bottomMargin = DeviceTools.dp2px(30).toInt()
        marginParams.leftMargin = DeviceTools.dp2px(45).toInt()
        view.layoutParams = marginParams
    }


    // 点击消息输入框，弹出软键盘
    private fun sendMessage(message: String?) {
        if (message.isNullOrEmpty()) return
        MChatGroupIMManager.instance().sendTxtMsg(message, MChatKeyCenter.nickname,MChatKeyCenter.portraitIndex) {
            if (it) {
                chatContext.getUnityCmd()?.sendMessage(message)
                ThreadTools.get().runOnMainThread {
                    binding?.etMessage?.setText("")
                    refreshMessage()
                }
            }
        }
    }

    private fun onClickSend(view: View) {
        val message = binding?.etMessage?.text?.trim()?.toString()
        sendMessage(message)
    }

    private fun refreshMessage() {
        messageAdapter?.let {
            val startPosition: Int = it.itemCount
            it.addAll(MChatGroupIMManager.instance().getAllMsgList())
            LogTools.d("refresh message startPosition:$startPosition")
            if (it.itemCount > 0) {
                binding?.rvMessageContent?.smoothScrollToPosition(it.itemCount - 1)
            }
        }
    }

    inner class MChatMessageAdapter : BaseQuickAdapter<MChatMessageModel, MChatMessageAdapter.VH>() {
        //自定义ViewHolder类
        inner class VH constructor(
            val parent: ViewGroup,
            val binding: MchatItemMessageBinding = MchatItemMessageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
            return VH(parent)
        }

        override fun onBindViewHolder(holder: VH, position: Int, data: MChatMessageModel?) {
            data ?: return
            holder.binding.ivUserPortrait.setImageResource(getPortraitCoverRes(data.portraitIndex))
            holder.binding.tvNickname.text = data.nickname ?: ""
            holder.binding.tvCurrentUser.isVisible = !data.from.isNullOrEmpty() && data.from == MChatKeyCenter.imUid
            holder.binding.tvMessage.text = data.content ?: ""
            holder.binding.tvSendTime.text = getSendTime(data.timeStamp)
        }

        @DrawableRes
        private fun getPortraitCoverRes(index: Int): Int {
            val coverArray: TypedArray = context.resources.obtainTypedArray(R.array.mchat_portrait)
            val localAvatarIndex = if (index >= 0 && index < coverArray.length()) index else 0
            return coverArray.getResourceId(localAvatarIndex, R.drawable.mchat_room_cover0)
        }

        private fun getSendTime(timeStamp: Long): String {
            val dateFormat = SimpleDateFormat("HH:mm")
            return dateFormat.format(Date(timeStamp))
        }
    }
}