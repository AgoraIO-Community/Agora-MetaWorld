package io.agora.metachat.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import io.agora.metachat.R
import io.agora.metachat.databinding.MchatViewCommonTitleBinding

/**
 * @author create by zhangwei03
 */
class MChatCommonTitleView : ConstraintLayout {

    private var binding: MchatViewCommonTitleBinding

    private var onBackClickListener: OnClickListener? = null
    private var onRightClickListener: OnClickListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
        binding = MchatViewCommonTitleBinding.inflate(LayoutInflater.from(context), this)
        initView(context, attrs, defStyleAttr)
    }

    private fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val ta = context.theme.obtainStyledAttributes(attrs, R.styleable.mchat_title_view, defStyleAttr, 0)
        val leftImageDrawable: Drawable? = ta.getDrawable(R.styleable.mchat_title_view_mchat_ctv_leftImageSrc)
        leftImageDrawable?.let {
            binding.ivBackIcon.setImageDrawable(it)
            binding.ivBackIcon.isVisible = true
        }
        val isHideLeftImage: Boolean = ta.getBoolean(R.styleable.mchat_title_view_mchat_ctv_hideLeftImage, false)
        binding.ivBackIcon.isGone = isHideLeftImage

        val leftText = ta.getString(R.styleable.mchat_title_view_mchat_ctv_leftText)
        if (!leftText.isNullOrEmpty()) {
            binding.tvBackTitle.text = leftText
            binding.tvBackTitle.isVisible = true
            binding.ivBackIcon.isVisible = false
        } else {
            binding.tvBackTitle.isVisible = false
        }

        val centerText = ta.getString(R.styleable.mchat_title_view_mchat_ctv_centerText)
        if (!centerText.isNullOrEmpty()) {
            binding.tvCenterTitle.text = centerText
            binding.tvCenterTitle.isVisible = true
        } else {
            binding.tvCenterTitle.isVisible = false
        }
        val centerTextColor = ta.getColor(R.styleable.mchat_title_view_mchat_ctv_centerTextColor, Color.BLACK)
        binding.tvCenterTitle.setTextColor(centerTextColor)

        val rightImageDrawable: Drawable? = ta.getDrawable(R.styleable.mchat_title_view_mchat_ctv_rightImageSrc)
        rightImageDrawable?.let {
            binding.ivRightIcon.setImageDrawable(it)
            binding.ivRightIcon.isVisible = true
        }
        val isHideRightImage: Boolean = ta.getBoolean(R.styleable.mchat_title_view_mchat_ctv_hideRightImage, true)
        binding.ivRightIcon.isGone = isHideRightImage

        val rightText = ta.getString(R.styleable.mchat_title_view_mchat_ctv_rightText)
        if (!rightText.isNullOrEmpty()) {
            binding.tvRightTitle.text = leftText
            binding.tvRightTitle.isVisible = true
            binding.ivRightIcon.isVisible = false
        } else {
            binding.tvRightTitle.isVisible = false
        }
    }

    fun setLeftText(text: String) = apply {
        binding.tvBackTitle.text = text
        binding.tvBackTitle.isVisible = true
    }

    fun setLeftClick(onClickListener: OnClickListener) = apply {
        binding.tvBackTitle.setOnClickListener(onClickListener)
        binding.ivBackIcon.setOnClickListener(onClickListener)
    }

    fun setCenterText(text: String) = apply {
        binding.tvCenterTitle.text = text
        binding.tvCenterTitle.isVisible = true
    }

    fun setRightText(text: String) = apply {
        binding.tvRightTitle.text = text
        binding.tvRightTitle.isVisible = true
    }

    fun setRightClick(onClickListener: OnClickListener) = apply {
        binding.tvRightTitle.setOnClickListener(onClickListener)
        binding.ivRightIcon.setOnClickListener(onClickListener)
    }
}