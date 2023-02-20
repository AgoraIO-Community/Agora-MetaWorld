package io.agora.metachat.game.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.databinding.MchatDialogBeginnerGuideBinding
import io.agora.metachat.databinding.MchatItemBeginnerGuideBinding
import io.agora.metachat.tools.DeviceTools

/**
 * @author create by zhangwei03
 */
class MChatBeginnerDialog constructor(val type: Int) : BaseFragmentDialog<MchatDialogBeginnerGuideBinding>() {
    companion object {
        const val NOVICE_TYPE = 1
        const val VISITOR_TYPE = 2
    }

    private lateinit var guideArray: Array<String>

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogBeginnerGuideBinding {
        return MchatDialogBeginnerGuideBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.root?.let {
            setDialogSize(it)
        }
        initData()
        initView()
    }

    private fun initData() {
        guideArray = if (type == NOVICE_TYPE) {
            resources.getStringArray(R.array.mchat_beginner_guide_content)
        } else {
            resources.getStringArray(R.array.mchat_visitor_mode_content)
        }
    }

    private fun initView() {
        binding?.apply {
            tvTitle.text =
                if (type == NOVICE_TYPE) {
                    resources.getString(R.string.mchat_beginner_guide)
                } else {
                    resources.getString(R.string.mchat_visitor_mode_title)
                }
            ivClose.setOnClickListener {
                dismiss()
            }
            val contents = mutableListOf<String>().apply {
                if (type == NOVICE_TYPE) {
                    add(resources.getString(R.string.mchat_beginner_guide_precautions))
                } else {
                    add(resources.getString(R.string.mchat_visitor_mode_precautions))
                }
                for (i in guideArray.indices) {
                    add(guideArray[i])
                }
            }
            rvContent.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            val guideAdapter = MChatGuideAdapter()
            rvContent.adapter = guideAdapter
            guideAdapter.submitList(contents)
        }
    }

    private fun setDialogSize(view: View) {
        val layoutParams: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = DeviceTools.dp2px(400).toInt()
        layoutParams.height = DeviceTools.dp2px(255).toInt()
        view.layoutParams = layoutParams
    }

    // guide adapter
    inner class MChatGuideAdapter : BaseQuickAdapter<String,MChatGuideAdapter.VH>() {
        //自定义ViewHolder类
        inner class VH constructor(
            val parent: ViewGroup,
            val binding: MchatItemBeginnerGuideBinding = MchatItemBeginnerGuideBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
            return VH(parent)
        }

        override fun onBindViewHolder(holder: VH, position: Int, data: String?) {
            data ?: return
            if (position == 0) {
                holder.binding.tvGuideTitle.isVisible = true
                holder.binding.ivGuideNumber.isVisible = false
                holder.binding.tvGuideContent.isVisible = false
                holder.binding.tvGuideTitle.text = data
            } else {
                holder.binding.tvGuideTitle.isVisible = false
                holder.binding.ivGuideNumber.isVisible = true
                holder.binding.tvGuideContent.isVisible = true
                val numberResName = "mchat_guide_$position"
                var numberDrawable = DeviceTools.getDrawableId(context, numberResName)
                if (numberDrawable == 0) numberDrawable = R.drawable.mchat_guide_1
                holder.binding.ivGuideNumber.setImageResource(numberDrawable)
                holder.binding.tvGuideContent.text = data
            }
        }
    }
}