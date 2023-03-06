package io.agora.metachat.home

import android.app.Activity
import android.content.res.TypedArray
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseUiFragment
import io.agora.metachat.baseui.dialog.CommonFragmentAlertDialog
import io.agora.metachat.databinding.MchatFragmentCreateRoleBinding
import io.agora.metachat.game.MChatGameActivity
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.home.dialog.MChatBadgeDialog
import io.agora.metachat.home.dialog.MChatDownloadDialog
import io.agora.metachat.home.dialog.MChatPortraitDialog
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.widget.OnIntervalClickListener
import java.util.*

/**
 * @author create by zhangwei03
 *
 * create a role
 */
class MChatCreateRoleFragment : BaseUiFragment<MchatFragmentCreateRoleBinding>() {

    companion object {
        private const val defaultPortrait = R.drawable.mchat_portrait0
        private const val defaultBadge = R.drawable.mchat_badge_level0
    }

    private lateinit var mChatViewModel: MChatRoomCreateViewModel
    private var nicknameIllegal = false

    /**portrait */
    private lateinit var portraitArray: TypedArray

    /**badge */
    private lateinit var badgeArray: TypedArray

    /**nickname*/
    private lateinit var nicknameArray: Array<String>

    private val random by lazy { Random() }

    private val roomId: String by lazy {
        arguments?.getString(MChatConstant.Params.KEY_ROOM_ID) ?: ""
    }

    private val roomName: String by lazy {
        arguments?.getString(MChatConstant.Params.KEY_ROOM_NAME) ?: ""
    }

    private val roomCoverIndex: Int by lazy {
        arguments?.getInt(MChatConstant.Params.KEY_ROOM_COVER_INDEX) ?: 0
    }

    private val roomPassword: String by lazy {
        arguments?.getString(MChatConstant.Params.KEY_ROOM_PASSWORD) ?: ""
    }

    private val isFromCreate: Boolean by lazy {
        arguments?.getBoolean(MChatConstant.Params.KEY_IS_CREATE) ?: false
    }

    private var downloadDialog: MChatDownloadDialog? = null

    private val actLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { actResult ->
//        if (actResult.resultCode == Activity.RESULT_OK) {
//            // 进入游戏返回,回到列表页面
//            LogTools.d("go unity page success")
//            findNavController().navigate(R.id.action_crateRoleFragment_to_roomListFragment)
//        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatFragmentCreateRoleBinding? {
        return MchatFragmentCreateRoleBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mChatViewModel = ViewModelProvider(this).get(MChatRoomCreateViewModel::class.java)
        LogTools.d("$roomName $roomPassword $roomCoverIndex")
        initData()
        initView()
        roomObservable()
    }

    private fun initData() {
        portraitArray = resources.obtainTypedArray(R.array.mchat_portrait)
        badgeArray = resources.obtainTypedArray(R.array.mchat_user_badge)
        nicknameArray = resources.getStringArray(R.array.mchat_random_nickname)
    }

    private fun initView() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            LogTools.d("systemInset l:${systemInset.left},t:${systemInset.top},r:${systemInset.right},b:${systemInset.bottom}")
            binding.root.setPaddingRelative(0, systemInset.top, 0, systemInset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        binding.ivPortrait.setImageResource(portraitArray.getResourceId(MChatKeyCenter.portraitIndex, defaultPortrait))
        binding.ivBadge.setImageResource(badgeArray.getResourceId(MChatKeyCenter.badgeIndex, defaultBadge))
        binding.etNickname.setText(nicknameArray[0])
        MChatKeyCenter.nickname = nicknameArray[0]
        if (MChatKeyCenter.gender == MChatConstant.Gender.MALE) {
            binding.linearMale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple_stroke_red)
            binding.linearFemale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple)
        } else {
            binding.linearMale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple)
            binding.linearFemale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple_stroke_red)
        }
        binding.titleView.setLeftClick(OnIntervalClickListener(this::onClickBack))
        binding.ivPortrait.setOnClickListener(OnIntervalClickListener(this::onClickPortrait))
        binding.ivBadge.setOnClickListener(OnIntervalClickListener(this::onClickBadge))
        binding.ivBadgeArrow.setOnClickListener(OnIntervalClickListener(this::onClickBadge))
        binding.ivNicknameRandom.setOnClickListener(OnIntervalClickListener(this::onClickNicknameRandom))
        binding.linearMale.setOnClickListener(OnIntervalClickListener(this::onClickMale))
        binding.linearFemale.setOnClickListener(OnIntervalClickListener(this::onClickFemale))
        binding.ivSelectAvatar.setOnClickListener(OnIntervalClickListener(this::onClickEnterRoom))
        binding.etNickname.doAfterTextChanged {
            if (it.isNullOrEmpty() || it.length <= 1) {
                // 防止多次更改edittext背景
                if (nicknameIllegal) return@doAfterTextChanged
                nicknameIllegal = true
                binding.tvNicknameIllegal.isVisible = true
                binding.etNickname.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_light_gray_stroke_red)
                MChatKeyCenter.nickname = ""
            } else {
                if (!nicknameIllegal) return@doAfterTextChanged
                nicknameIllegal = false
                binding.tvNicknameIllegal.isVisible = false
                binding.etNickname.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_light_grey)
                MChatKeyCenter.nickname = binding.etNickname.text.toString()
            }
        }
    }

    private fun roomObservable() {
        mChatViewModel.sceneListObservable().observe(viewLifecycleOwner) { sceneInfos ->
            if (sceneInfos.isNullOrEmpty()) return@observe
            sceneInfos.find { it.mSceneId == MChatConstant.DefaultValue.DEFAULT_SCENE_ID }?.let { sceneInfo ->
                mChatViewModel.prepareScene(sceneInfo)
            } ?: run {
                LogTools.e("no available meta chat scene found")
                dismissLoading()
            }
        }
        mChatViewModel.selectSceneObservable().observe(viewLifecycleOwner) {
            downloadDialog?.dismiss()
            downloadDialog = null
            showLoading(false)
            if (isFromCreate) {
                mChatViewModel.createRoom(roomName, roomCoverIndex, roomPassword)
            } else {
                mChatViewModel.joinRoom(roomId, roomPassword)
            }
        }
        mChatViewModel.requestDownloadingObservable().observe(viewLifecycleOwner) {
            dismissLoading()
            if (it) {
                CommonFragmentAlertDialog()
                    .titleText(resources.getString(R.string.mchat_download_title))
                    .contentText(
                        resources.getString(
                            R.string.mchat_download_content,
                            DeviceTools.getNetFileSizeDescription(mChatViewModel.getSceneInfo().mTotalSize)
                        )
                    )
                    .leftText(resources.getString(R.string.mchat_download_next_time))
                    .rightText(resources.getString(R.string.mchat_download_now))
                    .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                        override fun onConfirmClick() {
                            mChatViewModel.downloadScene()
                        }
                    }).show(childFragmentManager, "downloadTips")
            }
        }
        mChatViewModel.downloadingProgressObservable().observe(viewLifecycleOwner) {
            dismissLoading()
            if (downloadDialog == null) {
                downloadDialog = MChatDownloadDialog()
                    .setCancelCallback {
                        mChatViewModel.cancelDownloadScene()
                    }
                downloadDialog?.show(childFragmentManager, "downloadProgress")
            } else if (it < 0) {
                downloadDialog?.dismiss()
                downloadDialog = null
                return@observe
            }
            if (it > 0) {
                downloadDialog?.updateProgress(it)
            }
        }

        mChatViewModel.createRoomObservable().observe(viewLifecycleOwner) {
            if (it.roomId.isNotEmpty()) mChatViewModel.joinRoom(it.roomId, it.password)
        }
        mChatViewModel.joinRoomObservable().observe(viewLifecycleOwner) { joinOutput ->
            dismissLoading()
            if (joinOutput.roomId.isNotEmpty()) {
                activity?.let {
                    findNavController().navigate(R.id.action_crateRoleFragment_to_roomListFragment)
                    MChatGameActivity.startActivity(actLaunch, it, joinOutput.roomId)
                }
            }
        }
    }

    private fun onClickBack(view: View) {
        findNavController().popBackStack()
    }

    /**show portrait dialog*/
    private fun onClickPortrait(view: View) {
        MChatPortraitDialog().setConfirmCallback {
            if (MChatKeyCenter.portraitIndex == it) return@setConfirmCallback
            MChatKeyCenter.portraitIndex = it
            binding.ivPortrait.setImageResource(portraitArray.getResourceId(it, defaultPortrait))
        }.show(childFragmentManager, "portrait")
    }

    /**show badge dialog*/
    private fun onClickBadge(view: View) {
        MChatBadgeDialog().setConfirmCallback {
            if (MChatKeyCenter.badgeIndex == it) return@setConfirmCallback
            MChatKeyCenter.badgeIndex = it
            binding.ivBadge.setImageResource(badgeArray.getResourceId(it, defaultBadge))
        }.show(childFragmentManager, "badge")
    }

    private fun onClickNicknameRandom(view: View) {
        val nickName = nicknameArray[random.nextInt(nicknameArray.size)]
        binding.etNickname.setText(nickName)
        MChatKeyCenter.nickname = nickName
    }

    private fun onClickMale(view: View) {
        binding.linearMale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple_stroke_red)
        binding.linearFemale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple)
        MChatKeyCenter.gender = MChatConstant.Gender.MALE
    }

    private fun onClickFemale(view: View) {
        binding.linearMale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple)
        binding.linearFemale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple_stroke_red)
        MChatKeyCenter.gender = MChatConstant.Gender.FEMALE
    }

    private fun onClickEnterRoom(view: View) {
        val nickname = binding.etNickname.text?.toString() ?: ""
        if (nickname.length <= 1) {
            nicknameIllegal = true
            binding.tvNicknameIllegal.isVisible = true
            binding.etNickname.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_light_gray_stroke_red)
            return
        }
        MChatKeyCenter.nickname = nickname
        showLoading(false)
        mChatViewModel.getScenes()
    }

    override fun onResume() {
        activity?.let {
            val insetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
            insetsController.isAppearanceLightStatusBars = false
        }
        super.onResume()
    }
}