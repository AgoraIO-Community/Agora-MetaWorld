package io.agora.metachat.home

import android.content.res.TypedArray
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseUiFragment
import io.agora.metachat.databinding.MchatFragmentCreateRoomBinding
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.service.MChatRoomModel
import io.agora.metachat.tools.GsonTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ToastTools
import io.agora.metachat.widget.OnIntervalClickListener
import java.util.*

/**
 * @author create by zhangwei03
 *
 * create a room
 */
class MChatCreateRoomFragment : BaseUiFragment<MchatFragmentCreateRoomBinding>() {

    companion object {
        private const val defaultCover = R.drawable.mchat_room_cover0
    }

    private lateinit var mChatViewModel: MChatRoomCreateViewModel
    private var chatRoomList: List<MChatRoomModel>? = null
    private var roomCoverIndex = 0

    /**room cover*/
    private lateinit var roomCoverArray: TypedArray

    /**room name*/
    private lateinit var roomNameArray: Array<String>
    private val random by lazy { Random() }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatFragmentCreateRoomBinding? {
        return MchatFragmentCreateRoomBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mChatViewModel = ViewModelProvider(this).get(MChatRoomCreateViewModel::class.java)
        initData()
        initView()
    }

    private fun initData() {
        roomCoverArray = resources.obtainTypedArray(R.array.mchat_room_cover)
        roomNameArray = resources.getStringArray(R.array.mchat_room_name)
    }

    private fun initView() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            LogTools.d("systemInset l:${systemInset.left},t:${systemInset.top},r:${systemInset.right},b:${systemInset.bottom}")
            binding.root.setPaddingRelative(0, systemInset.top, 0, systemInset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        binding.ivRoomCover.setImageResource(roomCoverArray.getResourceId(roomCoverIndex, defaultCover))
        binding.etRoomName.setText(roomNameArray[0])
        binding.titleView.setLeftClick(OnIntervalClickListener(this::onClickBack))
        binding.tvRoomNameRandom.setOnClickListener(OnIntervalClickListener(this::onClickRoomRandom))
        binding.btnCreateRoom.setOnClickListener(OnIntervalClickListener(this::onClickCreate))
        binding.rgRoomPermissions.setOnCheckedChangeListener { group, checkedId ->
            binding.tvPasswordTips.isVisible = false
            binding.etPassword.isVisible = checkedId == R.id.rb_private
            if (checkedId == R.id.rb_private) {
                binding.etPassword.isFocusable = true
                binding.etPassword.isFocusableInTouchMode = true
                binding.etPassword.requestFocus()
                showKeyboard(binding.etPassword)
            }
        }
        mChatViewModel.roomListObservable().observe(viewLifecycleOwner) {
            LogTools.d("获取房间列表:${GsonTools.beanToString(it)}")
            chatRoomList = it
        }
    }

    private fun onClickCreate(view: View) {
        if (binding.etRoomName.text.toString() != binding.etRoomName.text.toString().trim()) {
            ToastTools.showTips(R.string.mchat_room_create_cannot_use_spaces)
            return
        }
        val roomName = binding.etRoomName.text.toString().trim { it <= ' ' }
        if (roomName.isEmpty()) {
            ToastTools.showTips(R.string.mchat_room_create_empty_name)
            return
        }
        //for (item in chatRoomList ?: mutableListOf()) {
        //    if (item.ownerId.toString() == MChatKeyCenter.imUid) {
        //        ToastTools.showTips(R.string.mchat_room_create_equals_ownerid)
        //        return
        //    }
        //}
        // 名字是否相同
        var containsName = false
        for (item in chatRoomList ?: mutableListOf()) {
            if (item.roomName == roomName) {
                containsName = true
                break
            }
        }
        if (containsName) {
            ToastTools.showTips(R.string.mchat_room_create_equals_name)
            return
        }
        val isPrivate = binding.rgRoomPermissions.checkedRadioButtonId == R.id.rb_private
        if (isPrivate && binding.etPassword.length() != binding.etPassword.textLength) {
            binding.tvPasswordTips.isVisible = true
            ToastTools.showTips(R.string.mchat_room_set_password_tips)
            return
        }
        val args = Bundle().apply {
            putString(MChatConstant.Params.KEY_ROOM_NAME, roomName)
            putInt(MChatConstant.Params.KEY_ROOM_COVER_INDEX, roomCoverIndex)
            putBoolean(MChatConstant.Params.KEY_IS_CREATE, true)
            if (isPrivate) {
                val roomPassword = binding.etPassword.text.toString().trim { it <= ' ' }
                putString(MChatConstant.Params.KEY_ROOM_PASSWORD, roomPassword)
            }
        }
        findNavController().navigate(R.id.action_crateRoomFragment_to_crateRoleFragment, args)
    }

    private fun onClickRoomRandom(view: View) {
        roomCoverIndex = random.nextInt(roomCoverArray.length())
        binding.ivRoomCover.setImageResource(roomCoverArray.getResourceId(roomCoverIndex, defaultCover))
        val roomNameIndex = random.nextInt(roomNameArray.size)
        binding.etRoomName.setText(roomNameArray[roomNameIndex])
    }

    private fun onClickBack(view: View) {
        findNavController().popBackStack()
    }

    override fun onResume() {
        activity?.let {
            val insetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
            insetsController.isAppearanceLightStatusBars = true
        }
        mChatViewModel.fetchRoomList()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}