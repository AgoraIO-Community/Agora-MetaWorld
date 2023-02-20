package io.agora.metachat.home

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseUiFragment
import io.agora.metachat.databinding.MchatFragmentRoomListBinding
import io.agora.metachat.databinding.MchatItemRoomListBinding
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.home.dialog.MChatEncryptionInputDialog
import io.agora.metachat.service.MChatRoomModel
import io.agora.metachat.tools.*
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 *
 * meta chat room list
 */
class MChatRoomListFragment : BaseUiFragment<MchatFragmentRoomListBinding>(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private const val HIDE_REFRESH_DELAY = 3000L
    }

    private lateinit var mChatViewModel: MChatRoomCreateViewModel
    private var roomAdapter: BaseQuickAdapter<MChatRoomModel, MChatRoomAdapter.VH>? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatFragmentRoomListBinding {
        return MchatFragmentRoomListBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mChatViewModel = ViewModelProvider(this).get(MChatRoomCreateViewModel::class.java)
        initView()
        roomObservable()
    }

    private fun initView() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            LogTools.d("systemInset l:${systemInset.left},t:${systemInset.top},r:${systemInset.right},b:${systemInset.bottom}")
            binding.root.setPaddingRelative(0, systemInset.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }
        if (DeviceTools.getIsZh()) {
            binding.ivMchatCreateIntroduce.setImageResource(R.drawable.mchat_room_create_cn)
        } else {
            binding.ivMchatCreateIntroduce.setImageResource(R.drawable.mchat_room_create_en)
        }
        roomAdapter = MChatRoomAdapter()
        roomAdapter?.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<MChatRoomModel>{
            override fun onClick(adapter: BaseQuickAdapter<MChatRoomModel, *>, view: View, position: Int) {
                if (FastClickTools.isFastClick(view)) return
                adapter.getItem(position)?.let {
                    goCreateRole(it)
                }
            }
        })
        binding.rvRoomList.apply {
            val padding: Int = DeviceTools.dp2px(8).toInt()
            val itemDecoration = object : ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.top = padding
                    outRect.bottom = padding
                    outRect.left = padding
                    outRect.right = padding
                }
            }
            addItemDecoration(itemDecoration)
            layoutManager = GridLayoutManager(context, 2)
            adapter = roomAdapter
            setOnEmptyCallback {
                binding.groupRoomIntroduce.isVisible = it
                binding.linearCreateRoomBottom.isVisible = !it
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.linearCreateRoomBottom.setOnClickListener(OnIntervalClickListener(this::onClickCreateRoom))
        binding.linearCreateRoomIntroduce.setOnClickListener(OnIntervalClickListener(this::onClickCreateRoom))
    }

    private fun goCreateRole(roomModel: MChatRoomModel) {
        val args = Bundle().apply {
            putString(MChatConstant.Params.KEY_ROOM_NAME, roomModel.roomName)
            putString(MChatConstant.Params.KEY_ROOM_ID, roomModel.roomId)
            putInt(MChatConstant.Params.KEY_ROOM_COVER_INDEX, roomModel.roomCoverIndex)
        }
        if (roomModel.isPrivate) {
            MChatEncryptionInputDialog()
                .setDialogCancelable(true)
                .setOnClickListener(object : MChatEncryptionInputDialog.OnClickBottomListener {
                    override fun onCancelClick() {}
                    override fun onConfirmClick(password: String) {
                        if (TextUtils.equals(password, roomModel.roomPassword)) {
                            args.putString(MChatConstant.Params.KEY_ROOM_PASSWORD, roomModel.roomPassword)
                            findNavController().navigate(R.id.action_roomListFragment_to_crateRoleFragment, args)
                        } else {
                            ToastTools.showTips(R.string.mchat_room_incorrect_password)
                        }
                    }
                }).show(childFragmentManager, "encryption dialog")
        } else {
            findNavController().navigate(R.id.action_roomListFragment_to_crateRoleFragment, args)
        }
    }

    private fun onClickCreateRoom(view: View) {
        findNavController().navigate(R.id.action_roomListFragment_to_crateRoomFragment)
    }

    private fun roomObservable() {
        mChatViewModel.roomListObservable().observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            LogTools.d("meta chat room list size:${it?.size}")
            roomAdapter?.submitList(it ?: mutableListOf())
        }
    }

    override fun onResume() {
        activity?.let {
            val insetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
            insetsController.isAppearanceLightStatusBars = true
        }
        super.onResume()
        mChatViewModel.fetchRoomList()
    }

    override fun onRefresh() {
        ThreadTools.get().runOnMainThreadDelay({ mChatViewModel.fetchRoomList() }, HIDE_REFRESH_DELAY)
    }

    /**room list adapter*/
    inner class MChatRoomAdapter() : BaseQuickAdapter<MChatRoomModel, MChatRoomAdapter.VH>() {
        //自定义ViewHolder类
        inner class VH constructor(
            val parent: ViewGroup,
            val binding: MchatItemRoomListBinding = MchatItemRoomListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
            return VH(parent)
        }

        override fun onBindViewHolder(holder: VH, position: Int, data: MChatRoomModel?) {
            data ?: return
            holder.binding.ivRoomLock.isVisible = data.isPrivate
            holder.binding.ivRoomCover.setImageResource(getRoomCoverRes(data.roomCoverIndex))
            holder.binding.tvRoomName.text = data.roomName
            holder.binding.tvRoomId.text = context.getString(R.string.mchat_room_id, data.roomId)
            holder.binding.tvMembers.text = "${data.memberCount}"
        }

        @DrawableRes
        private fun getRoomCoverRes(index: Int): Int {
            val coverArray: TypedArray = context.resources.obtainTypedArray(R.array.mchat_room_cover)
            val localAvatarIndex = if (index >= 0 && index < coverArray.length()) index else 0
            return coverArray.getResourceId(localAvatarIndex, R.drawable.mchat_room_cover0)
        }
    }
}
