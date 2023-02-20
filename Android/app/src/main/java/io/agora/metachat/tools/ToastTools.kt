package io.agora.metachat.tools

import android.widget.Toast
import androidx.annotation.StringRes
import io.agora.metachat.MChatApp
import io.agora.metachat.tools.internal.InternalToast

/**
 * @author create by zhangwei03
 */
object ToastTools {

    @JvmStatic
    fun showCommon(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        show(msg, InternalToast.COMMON, duration)
    }

    @JvmStatic
    fun showCommon(@StringRes stringRes: Int, duration: Int = Toast.LENGTH_SHORT) {
        show(stringRes, InternalToast.COMMON, duration)
    }

    @JvmStatic
    fun showTips(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        show(msg, InternalToast.TIPS, duration)
    }

    @JvmStatic
    fun showTips(@StringRes stringRes: Int, duration: Int = Toast.LENGTH_SHORT) {
        show(stringRes, InternalToast.TIPS, duration)
    }

    @JvmStatic
    fun showError(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        show(msg, InternalToast.ERROR, duration)
    }

    @JvmStatic
    fun showError(@StringRes stringRes: Int, duration: Int = Toast.LENGTH_SHORT) {
        show(stringRes, InternalToast.TIPS, duration)
    }

    @JvmStatic
    private fun show(
        @StringRes stringRes: Int,
        toastType: Int = InternalToast.COMMON,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        show(MChatApp.instance().getString(stringRes), toastType, duration)
    }

    @JvmStatic
    private fun show(msg: String, toastType: Int = InternalToast.COMMON, duration: Int = Toast.LENGTH_SHORT) {
        if (ThreadTools.get().isMainThread) {
            InternalToast.init(MChatApp.instance())
            InternalToast.show(msg, toastType, duration)
        } else {
            ThreadTools.get().runOnMainThread {
                InternalToast.init(MChatApp.instance())
                InternalToast.show(msg, toastType, duration)
            }
        }
    }
}