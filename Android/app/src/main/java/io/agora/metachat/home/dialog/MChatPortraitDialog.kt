package io.agora.metachat.home.dialog

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.databinding.MchatDialogSelectPortraitBinding
import io.agora.metachat.databinding.MchatItemPortraitListBinding
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.ToastTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 */
class MChatPortraitDialog constructor() : BaseFragmentDialog<MchatDialogSelectPortraitBinding>() {

    private lateinit var portraitArray: TypedArray
    private var defaultPortrait = R.drawable.mchat_portrait0
    private var selPortraitIndex: Int = MChatKeyCenter.portraitIndex

    private var confirmCallback: ((selPortraitIndex: Int) -> Unit)? = null

    fun setConfirmCallback(confirmCallback: ((selPortraitIndex: Int) -> Unit)) = apply {
        this.confirmCallback = confirmCallback
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogSelectPortraitBinding {
        return MchatDialogSelectPortraitBinding.inflate(inflater)
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
    }

    private fun initView() {
        val portraits = mutableListOf<Int>().apply {
            for (i in 0 until portraitArray.length()) {
                add(getVirtualAvatarRes(i))
            }
        }
        val portraitAdapter = MChatPortraitAdapter()
        portraitAdapter.setOnItemClickListener { adapter, view, position ->
            if (selPortraitIndex == position) return@setOnItemClickListener
            selPortraitIndex = position
            portraitAdapter.notifyDataSetChanged()
        }
        binding?.apply {
            rvPortrait.addItemDecoration(
                MaterialDividerItemDecoration(root.context, MaterialDividerItemDecoration.HORIZONTAL).apply {
                    dividerThickness = DeviceTools.dp2px(26).toInt()
                    dividerColor = Color.TRANSPARENT
                })
            rvPortrait.layoutManager = GridLayoutManager(root.context, 3)
            rvPortrait.adapter = portraitAdapter
            portraitAdapter.submitList(portraits)
            mbLeft.setOnClickListener(OnIntervalClickListener(this@MChatPortraitDialog::onClickCancel))
            mbRight.setOnClickListener(OnIntervalClickListener(this@MChatPortraitDialog::onClickConfirm))
        }
    }

    private fun onClickCancel(view: View) {
        dismiss()
    }

    private fun onClickConfirm(view: View) {
        dismiss()
        confirmCallback?.invoke(selPortraitIndex)
    }

    @DrawableRes
    private fun getVirtualAvatarRes(avatarIndex: Int): Int {
        val localPortraitIndex = if (avatarIndex >= 0 && avatarIndex < portraitArray.length()) avatarIndex else 0
        return portraitArray.getResourceId(localPortraitIndex, defaultPortrait)
    }

    /**portrait adapter*/
    inner class MChatPortraitAdapter() : BaseQuickAdapter<Int, MChatPortraitAdapter.VH>() {
        //自定义ViewHolder类
        inner class VH constructor(
            val parent: ViewGroup,
            val binding: MchatItemPortraitListBinding = MchatItemPortraitListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
            return VH(parent)
        }

        override fun onBindViewHolder(holder: VH, position: Int, data: Int?) {
            data ?: return
            holder.binding.ivUserPortrait.setImageResource(data)
            holder.binding.ivPortraitBg.isVisible = selPortraitIndex == position
        }
    }
}