package io.agora.metachat.baseui

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import io.agora.metachat.R
import io.agora.metachat.tools.LogTools

abstract class BaseUiActivity<B : ViewBinding> : AppCompatActivity() {
    lateinit var binding: B

    private var loadingDialog: AlertDialog? = null

    open fun showLoading(cancelable: Boolean) {
        if (loadingDialog == null) {
            loadingDialog = AlertDialog.Builder(this).setView(R.layout.mchat_view_base_loading).create().apply {
                // 背景修改成透明
                window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        loadingDialog?.setCancelable(cancelable)
        loadingDialog?.show()
    }

    open fun dismissLoading() {
        loadingDialog?.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = getViewBinding(layoutInflater)
        if (binding == null) {
            LogTools.e("Inflate Error")
            finish()
        } else {
            this.binding = binding
            super.setContentView(this.binding.root)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    protected abstract fun getViewBinding(inflater: LayoutInflater): B?

    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    open fun showKeyboard(editText: EditText) {
        val imm = editText.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, 0)
    }

    fun getCurActivity(): Activity = this
}