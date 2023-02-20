package io.agora.metachat.tools

import androidx.annotation.StringRes
import io.agora.metachat.MChatApp
import io.agora.metachat.tools.internal.EntLogger

/**
 * @author create by zhangwei03
 *
 */
object LogTools {

    private const val TAG = "MChat"

    private val entLogger = EntLogger(EntLogger.Config("MChat"))

    @JvmStatic
    fun d(message: String) {
        entLogger.d(TAG, message)
    }

    @JvmStatic
    fun d(@StringRes stringRes: Int) {
        entLogger.d(TAG, MChatApp.instance().getString(stringRes))
    }

    @JvmStatic
    fun e(message: String) {
        entLogger.e(TAG, message)
    }

    @JvmStatic
    fun e(@StringRes stringRes: Int) {
        entLogger.e(TAG, MChatApp.instance().getString(stringRes))
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        entLogger.d(tag, message)
    }

    @JvmStatic
    fun d(tag: String, @StringRes stringRes: Int) {
        entLogger.d(tag, MChatApp.instance().getString(stringRes))
    }

    @JvmStatic
    fun e(tag: String, message: String) {
        entLogger.e(tag, message)
    }

    @JvmStatic
    fun e(tag: String, @StringRes stringRes: Int) {
        entLogger.e(tag, MChatApp.instance().getString(stringRes))
    }
}