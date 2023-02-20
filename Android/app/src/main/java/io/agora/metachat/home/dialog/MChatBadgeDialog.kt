package io.agora.metachat.home.dialog

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.databinding.MchatDialogSelectBadgeBinding
import io.agora.metachat.databinding.MchatItemBadgeListBinding
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.ToastTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 */
class MChatBadgeDialog constructor() : BaseFragmentDialog<MchatDialogSelectBadgeBinding>() {

    private lateinit var badgeArray: TypedArray
    private var defaultBadge = R.drawable.mchat_badge_level0
    private var selBadgeIndex: Int = 0

    private var confirmCallback: ((selBadgeIndex: Int) -> Unit)? = null

    fun setConfirmCallback(confirmCallback: ((selBadgeIndex: Int) -> Unit)) = apply {
        this.confirmCallback = confirmCallback
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogSelectBadgeBinding {
        return MchatDialogSelectBadgeBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        initData()
        initView()
    }

    private fun initData() {
        badgeArray = resources.obtainTypedArray(R.array.mchat_user_badge)
    }

    private fun initView() {
        val portraits = mutableListOf<Int>().apply {
            for (i in 0 until badgeArray.length()) {
                add(getVirtualAvatarRes(i))
            }
        }
        val badgeAdapter = MChatBadgeAdapter()
        badgeAdapter.setOnItemClickListener { adapter, view, position ->
            if (selBadgeIndex == position) return@setOnItemClickListener
            selBadgeIndex = position
            adapter.notifyDataSetChanged()
        }

        binding?.apply {
            rvBadge.addItemDecoration(
                MaterialDividerItemDecoration(root.context, MaterialDividerItemDecoration.HORIZONTAL).apply {
                    dividerThickness = DeviceTools.dp2px(16).toInt()
                    dividerColor = Color.TRANSPARENT
                })
            rvBadge.layoutManager = GridLayoutManager(root.context, 3)
            rvBadge.adapter = badgeAdapter
            badgeAdapter.submitList(portraits)
            mbLeft.setOnClickListener(OnIntervalClickListener(this@MChatBadgeDialog::onClickCancel))
            mbRight.setOnClickListener(OnIntervalClickListener(this@MChatBadgeDialog::onClickConfirm))
        }
    }

    private fun onClickCancel(view: View) {
        dismiss()
    }

    private fun onClickConfirm(view: View) {
        dismiss()
        confirmCallback?.invoke(selBadgeIndex)
    }

    @DrawableRes
    private fun getVirtualAvatarRes(avatarIndex: Int): Int {
        val localBadgeIndex = if (avatarIndex >= 0 && avatarIndex < badgeArray.length()) avatarIndex else 0
        return badgeArray.getResourceId(localBadgeIndex, defaultBadge)
    }

    /**badge adapter*/
    inner class MChatBadgeAdapter() : BaseQuickAdapter<Int, MChatBadgeAdapter.VH>() {
        //自定义ViewHolder类
        inner class VH constructor(
            val parent: ViewGroup,
            val binding: MchatItemBadgeListBinding = MchatItemBadgeListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
            return VH(parent)
        }

        override fun onBindViewHolder(holder: VH, position: Int, data: Int?) {
            data ?: return
            holder.binding.ivUserBadge.setImageResource(data)
            holder.binding.ivBadgeBg.isVisible = selBadgeIndex == position
        }
    }
}