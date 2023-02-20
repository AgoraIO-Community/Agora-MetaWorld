package io.agora.metachat.baseui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import io.agora.metachat.R

abstract class BaseUiFragment<B : ViewBinding> : Fragment() {

    lateinit var binding: B

    private var loadingDialog: AlertDialog? = null

    open fun showLoading(cancelable: Boolean) {
        if (loadingDialog == null) {
            loadingDialog = AlertDialog.Builder(requireActivity()).setView(R.layout.mchat_view_base_loading).create().apply {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = getViewBinding(inflater, container) ?: return null
        this.binding = binding
        return this.binding.root
    }

    protected fun parentAct(): BaseUiActivity<*>? {
        if (activity is BaseUiActivity<*>?) return activity as BaseUiActivity<*>?
        return null
    }

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): B?

    fun hideKeyboard() {
        activity?.apply {
            val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            if (window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                if (currentFocus != null) {
                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }
        }
    }

    open fun showKeyboard(editText: EditText) {
        val imm = editText.context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, 0)
    }
}