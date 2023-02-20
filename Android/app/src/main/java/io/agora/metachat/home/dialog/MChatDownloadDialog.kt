package io.agora.metachat.home.dialog

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.databinding.MchatDialogDownloadBinding
import io.agora.metachat.game.sence.MChatContext
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 */
class MChatDownloadDialog constructor() : BaseFragmentDialog<MchatDialogDownloadBinding>() {

    private val chatContext by lazy {
        MChatContext.instance()
    }

    private var cancelCallback: (() -> Unit)? = null

    fun setCancelCallback(cancelCallback: () -> Unit) = apply {
        this.cancelCallback = cancelCallback
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogDownloadBinding {
        return MchatDialogDownloadBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        initView()
    }

    private fun initView() {
        binding?.let {
            it.mbCancel.setOnClickListener(OnIntervalClickListener(this::onClickCancel))
            it.mtContent.text =
                resources.getString(
                    R.string.mchat_download_content,
                    DeviceTools.getNetFileSizeDescription(chatContext.getSceneInfo().mTotalSize)
                )
        }
    }

    private fun onClickCancel(view: View) {
        cancelCallback?.invoke()
    }


    fun updateProgress(progress: Int) {
        if (progress < 0 || progress > 100) return
        binding?.apply {
            tvProgress.text = "$progress%"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(progress, true)
            } else {
                progressBar.progress = progress
            }
        }
    }
}