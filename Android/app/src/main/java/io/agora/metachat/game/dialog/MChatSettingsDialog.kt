package io.agora.metachat.game.dialog

import android.content.res.TypedArray
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.SeekBar
import androidx.core.view.isVisible
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.baseui.dialog.CommonFragmentAlertDialog
import io.agora.metachat.databinding.MchatDialogSettingsBinding
import io.agora.metachat.game.sence.MChatContext
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.home.dialog.MChatBadgeDialog
import io.agora.metachat.home.dialog.MChatPortraitDialog
import io.agora.metachat.imkit.MChatGroupIMManager
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ToastTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 */
class MChatSettingsDialog constructor() : BaseFragmentDialog<MchatDialogSettingsBinding>() {

    companion object {
        private const val GENERAL = 0
        private const val SOUND = 1
        private const val defaultPortrait = R.drawable.mchat_portrait0
        private const val defaultBadge = R.drawable.mchat_badge_level0
    }

    private val mchatContext by lazy {
        MChatContext.instance()
    }

    // 默认选中通用
    private var curSelected = GENERAL

    /**portrait */
    private lateinit var portraitArray: TypedArray

    /**badge */
    private lateinit var badgeArray: TypedArray

    private var defaultVirtualAvatar = R.drawable.mchat_female0

    private var exitCallback: (() -> Unit)? = null

    fun setExitCallback(exitCallback: () -> Unit) = apply {
        this.exitCallback = exitCallback
    }

    private var updateUserListener: OnUpdateUserListener? = null

    fun setOnUpdateUserListener(updateUserListener: OnUpdateUserListener) {
        this.updateUserListener = updateUserListener
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogSettingsBinding {
        return MchatDialogSettingsBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        initData()
        initView()
    }

    private fun initData() {
        portraitArray = resources.obtainTypedArray(R.array.mchat_portrait)
        badgeArray = resources.obtainTypedArray(R.array.mchat_user_badge)
        defaultVirtualAvatar = if (MChatKeyCenter.gender == MChatConstant.Gender.MALE) {
            R.drawable.mchat_male0
        } else {
            R.drawable.mchat_female0
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
                activity?.let {
                    width = DeviceTools.screenWidth(it)
                    height = DeviceTools.screenHeight(it)
                }
                gravity = Gravity.TOP
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
                window.attributes = this
            }
        }
    }

    fun initView() {
        binding?.let {
            setOnApplyWindowInsets(it.root)
            it.ivSettingsBack.setOnClickListener(OnIntervalClickListener(this::onClickSettingBack))
            it.layoutGeneralTab.setOnClickListener(OnIntervalClickListener(this::onClickGeneralTab))
            it.layoutSoundTab.setOnClickListener(OnIntervalClickListener(this::onClickSoundTab))
            it.layoutUserPortrait.setOnClickListener(OnIntervalClickListener(this::onClickUserPortrait))
            it.layoutUserBadge.setOnClickListener(OnIntervalClickListener(this::onClickUserBadge))
            it.layoutQuitRoom.setOnClickListener(OnIntervalClickListener(this::onClickExitRoom))
            it.etNickname.setText(MChatKeyCenter.nickname)
            it.etNickname.setOnEditorActionListener { textView, actionId, keyEvent ->
                when (actionId and EditorInfo.IME_MASK_ACTION) {
                    EditorInfo.IME_ACTION_DONE -> {
                        val name = binding?.etNickname?.text?.trim()?.toString()
                        updateNickname(name)
                    }
                    else -> {}
                }
                false
            }
            it.ivUserPortrait.setImageResource(
                portraitArray.getResourceId(MChatKeyCenter.portraitIndex, defaultPortrait)
            )
            it.ivUserBadge.setImageResource(
                badgeArray.getResourceId(MChatKeyCenter.badgeIndex, defaultBadge)
            )
            mchatContext.chatMediaPlayer()?.apply {
                it.pbTvVol.progress = tvVolume
                it.tvVolumeTvVol.text = "$tvVolume"
            }
            mchatContext.chatNpcManager()?.apply {
                it.pbNpcVol.progress = npcVolume
                it.tvVolumeNpcVol.text = "$npcVolume"
            }
            mchatContext.chatSpatialAudio()?.apply {
                it.pbRecvRange.progress = (recvRange * 10).toInt()
                it.tvRecvRangeValue.text = "$recvRange"
                it.pbDistanceUnit.progress = (distanceUnit * 10).toInt()
                it.tvDistanceUnitValue.text = "$distanceUnit"
            }

            it.pbTvVol.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    LogTools.d("onStopTrackingTouch tv volume:${seekBar.progress}")
                    val tvVolume = seekBar.progress
                    it.tvVolumeTvVol.text = "$tvVolume"
                    mchatContext.chatMediaPlayer()?.setPlayerVolume(tvVolume)
                }
            })

            it.pbNpcVol.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    LogTools.d("onStopTrackingTouch npc volume:${seekBar.progress}")
                    val npcVolume = seekBar.progress
                    it.tvVolumeNpcVol.text = "$npcVolume"
                    mchatContext.chatNpcManager()?.setNpcVolume(npcVolume)
                }
            })
            it.pbRecvRange.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    LogTools.d("onStopTrackingTouch recv range volume:${seekBar.progress}")
                    val recvRange = seekBar.progress / 10.0F
                    it.tvRecvRangeValue.text = "${recvRange}"
                    mchatContext.chatSpatialAudio()?.setAudioRecvRange(recvRange)
                }
            })

            it.pbDistanceUnit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    LogTools.d("onStopTrackingTouch distance unit volume:${seekBar.progress}")
                    val distanceUnit = seekBar.progress / 10.0F
                    it.tvDistanceUnitValue.text = "$distanceUnit"
                    mchatContext.chatSpatialAudio()?.setDistanceUnit(distanceUnit)
                }
            })
        }
    }

    private fun updateNickname(name: String?) {
        if (name.isNullOrEmpty() || name.length <= 1) {
            ToastTools.showError(R.string.mchat_nickname_error_tips)
            return
        }
        MChatKeyCenter.nickname = name
        mchatContext.getLocalUserAvatar()?.let { localUserAvatar ->
            val userInfo = localUserAvatar.userInfo.apply {
                mUserName = name
            }
            localUserAvatar.userInfo = userInfo
            localUserAvatar.applyInfo()
            updateUserListener?.onNickname()
        }
    }

    private fun onClickSettingBack(view: View) {
        dismiss()
    }

    // 通用点击
    private fun onClickGeneralTab(view: View) {
        curSelected = GENERAL
        binding?.apply {
            layoutGeneralTab.setBackgroundResource(R.drawable.mchat_bg_rect_radius9_purple)
            layoutSoundTab.setBackgroundColor(Color.TRANSPARENT)
            layoutGeneralContent.isVisible = true
            layoutSoundContent.isVisible = false
        }
    }

    // 声音点击
    private fun onClickSoundTab(view: View) {
        curSelected = SOUND
        binding?.apply {
            layoutGeneralTab.setBackgroundColor(Color.TRANSPARENT)
            layoutSoundTab.setBackgroundResource(R.drawable.mchat_bg_rect_radius9_purple)
            layoutGeneralContent.isVisible = false
            layoutSoundContent.isVisible = true
        }
    }

    // 点击更换头像
    private fun onClickUserPortrait(view: View) {
        MChatPortraitDialog().setConfirmCallback {
            if (MChatKeyCenter.portraitIndex == it) resources
            MChatKeyCenter.portraitIndex = it
            binding?.ivUserPortrait?.setImageResource(portraitArray.getResourceId(it, defaultPortrait))
            updateUserListener?.onUserPortrait()
        }.show(childFragmentManager, "portrait dialog")
    }

    // 点击更换徽章
    private fun onClickUserBadge(view: View) {
        MChatBadgeDialog().setConfirmCallback {
            if (MChatKeyCenter.badgeIndex == it) return@setConfirmCallback
            MChatKeyCenter.badgeIndex = it
            binding?.ivUserBadge?.setImageResource(badgeArray.getResourceId(it, defaultBadge))
            mchatContext.getLocalUserAvatar()?.let { localUserAvatar ->
                val userInfo = localUserAvatar.userInfo.apply {
                    mUserIconUrl = MChatConstant.getBadgeUrl(MChatKeyCenter.badgeIndex)
                }
                localUserAvatar.userInfo = userInfo
                localUserAvatar.applyInfo()
            }

        }.show(childFragmentManager, "badge dialog")
    }

    // 点击更换虚拟形象
    private fun onClickVirtualAvatar(view: View) {
        ToastTools.showCommon("todo click avatar")
    }

    // 退出房间
    private fun onClickExitRoom(view: View) {
        if (MChatGroupIMManager.instance().isRoomOwner()) {
            CommonFragmentAlertDialog()
                .titleText(resources.getString(R.string.mchat_exit_room))
                .contentText(resources.getString(R.string.mchat_exit_room_tips))
                .leftText(resources.getString(R.string.mchat_cancel))
                .rightText(resources.getString(R.string.mchat_confirm))
                .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                    override fun onConfirmClick() {
                        onClickSettingBack(view)
                        exitCallback?.invoke()
                    }
                }).show(childFragmentManager, "exit dialog")
        } else {
            exitCallback?.invoke()
        }
    }
}

interface OnUpdateUserListener {
    fun onNickname()

    fun onUserPortrait()
}