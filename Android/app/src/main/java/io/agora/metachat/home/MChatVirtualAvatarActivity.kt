package io.agora.metachat.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseUiActivity
import io.agora.metachat.databinding.MchatActivityVirtualAvatarBinding
import io.agora.metachat.databinding.MchatItemVirtualAvatarListBinding
import io.agora.metachat.game.MChatGameActivity
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 *
 * virtual avatar
 */
class MChatVirtualAvatarActivity : BaseUiActivity<MchatActivityVirtualAvatarBinding>() {

    companion object {

        fun startActivity(
            launcher: ActivityResultLauncher<Intent>,
            context: Context,
            isCreate: Boolean,
            roomName: String,
            roomId: String,
            roomCoverIndex: Int,
            roomPassword: String,
        ) {
            val intent = Intent(context, MChatVirtualAvatarActivity::class.java).apply {
                putExtra(MChatConstant.Params.KEY_IS_CREATE, isCreate)
                putExtra(MChatConstant.Params.KEY_ROOM_NAME, roomName)
                putExtra(MChatConstant.Params.KEY_ROOM_ID, roomId)
                putExtra(MChatConstant.Params.KEY_ROOM_COVER_INDEX, roomCoverIndex)
                putExtra(MChatConstant.Params.KEY_ROOM_PASSWORD, roomPassword)
            }
            launcher.launch(intent)
        }
    }

    private lateinit var mChatViewModel: MChatRoomCreateViewModel

    private val roomId by lazy { intent.getStringExtra(MChatConstant.Params.KEY_ROOM_ID) ?: "" }
    private val roomName by lazy { intent.getStringExtra(MChatConstant.Params.KEY_ROOM_NAME) ?: "" }
    private val roomCoverIndex by lazy { intent.getIntExtra(MChatConstant.Params.KEY_ROOM_COVER_INDEX, 0) }
    private val roomPassword by lazy { intent.getStringExtra(MChatConstant.Params.KEY_ROOM_PASSWORD) ?: "" }
    private val isFromCreate: Boolean by lazy { intent.getBooleanExtra(MChatConstant.Params.KEY_IS_CREATE, false) }

    /**virtual avatar */
    private lateinit var virtualAvatarArray: TypedArray

    private var defaultVirtualAvatar = R.drawable.mchat_female0

    override fun getViewBinding(inflater: LayoutInflater): MchatActivityVirtualAvatarBinding {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = this
            }
        }
        return MchatActivityVirtualAvatarBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        super.onCreate(savedInstanceState)
        mChatViewModel = ViewModelProvider(this).get(MChatRoomCreateViewModel::class.java)
        initData()
        initView()
        roomObservable()
    }

    private fun initData() {
        virtualAvatarArray = if (MChatKeyCenter.gender == MChatConstant.Gender.MALE) {
            resources.obtainTypedArray(R.array.mchat_avatar_male)
        } else {
            resources.obtainTypedArray(R.array.mchat_avatar_female)
        }
        defaultVirtualAvatar = if (MChatKeyCenter.gender == MChatConstant.Gender.MALE) {
            R.drawable.mchat_male0
        } else {
            R.drawable.mchat_female0
        }
    }

    @DrawableRes
    private fun getVirtualAvatarRes(avatarIndex: Int): Int {
        val localAvatarIndex = if (avatarIndex >= 0 && avatarIndex < virtualAvatarArray.length()) avatarIndex else 0
        return virtualAvatarArray.getResourceId(localAvatarIndex, defaultVirtualAvatar)
    }

    private fun initView() {
        if (isFromCreate) {
            binding.tvEnterRoom.setText(R.string.mchat_create_and_enter)
        } else {
            binding.tvEnterRoom.setText(R.string.mchat_confirm_and_enter)
        }
        val virtualAvatars = mutableListOf<Int>().apply {
            for (i in 0 until virtualAvatarArray.length()) {
                add(getVirtualAvatarRes(i))
            }
        }
        binding.tvChooseAvatarTips.text =
            resources.getString(R.string.mchat_choose_your_avatar, MChatKeyCenter.nickname)
        binding.ivCurrentAvatar.setImageResource(
            virtualAvatarArray.getResourceId(MChatKeyCenter.virtualAvatarIndex, defaultVirtualAvatar)
        )
        val avatarAdapter = MChatVirtualAvatarAdapter()
        avatarAdapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<Int> {
            override fun onClick(adapter: BaseQuickAdapter<Int, *>, view: View, position: Int) {
                if (MChatKeyCenter.virtualAvatarIndex == position) return
                MChatKeyCenter.virtualAvatarIndex = position
                virtualAvatars[MChatKeyCenter.virtualAvatarIndex].let {
                    binding.ivCurrentAvatar.setImageResource(it)
                }
                avatarAdapter.notifyDataSetChanged()
            }
        })
        binding.rvAvatarList.addItemDecoration(
            MaterialDividerItemDecoration(getCurActivity(), MaterialDividerItemDecoration.VERTICAL).apply {
                dividerThickness = DeviceTools.dp2px(15).toInt()
                dividerColor = Color.TRANSPARENT
            })
        binding.rvAvatarList.layoutManager = GridLayoutManager(getCurActivity(), 5)
        binding.rvAvatarList.adapter = avatarAdapter
        avatarAdapter.submitList(virtualAvatars)
        binding.linearEnterRoom.setOnClickListener(OnIntervalClickListener(this::onClickEnterGame))
        binding.linearAvatarBack.setOnClickListener(OnIntervalClickListener(this::onClickBack))
    }

    private val actLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { actResult ->
        if (actResult.resultCode == Activity.RESULT_OK) {
            LogTools.d("go game success")
        }
    }

    private fun roomObservable() {
        mChatViewModel.createRoomObservable().observe(this) {
            if (it.roomId.isNotEmpty()) mChatViewModel.joinRoom(it.roomId, it.password)
        }
        mChatViewModel.joinRoomObservable().observe(this) { joinOutput ->
            dismissLoading()
            if (joinOutput.roomId.isNotEmpty()) {
                setResult(RESULT_OK)
                finish()
                MChatGameActivity.startActivity(actLaunch, this, joinOutput.roomId)
            }
        }
    }

    private fun onClickEnterGame(view: View) {
        showLoading(false)
        if (isFromCreate) {
            mChatViewModel.createRoom(roomName, roomCoverIndex, roomPassword)
        } else {
            mChatViewModel.joinRoom(roomId, roomPassword)
        }
    }

    private fun onClickBack(view: View) {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
    }

    /**virtual avatar adapter*/
    inner class MChatVirtualAvatarAdapter() : BaseQuickAdapter<Int, MChatVirtualAvatarAdapter.VH>() {
        //自定义ViewHolder类
        inner class VH constructor(
            val parent: ViewGroup,
            val binding: MchatItemVirtualAvatarListBinding = MchatItemVirtualAvatarListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
            return VH(parent)
        }

        override fun onBindViewHolder(holder: VH, position: Int, data: Int?) {
            data ?: return
            holder.binding.ivUserAvatar.setImageResource(data)
            holder.binding.ivAvatarBg.isVisible = MChatKeyCenter.virtualAvatarIndex == position
        }
    }
}