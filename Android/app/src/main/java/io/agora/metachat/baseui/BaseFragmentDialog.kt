package io.agora.metachat.baseui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.viewbinding.ViewBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment

abstract class BaseFragmentDialog<B : ViewBinding> : DialogFragment() {

    var binding: B?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = getViewBinding(inflater, container)
        return this.binding?.root
    }

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): B?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onHandleOnBackPressed()
            }
        })
    }

    protected fun setOnApplyWindowInsets(root: View) {
        dialog?.window?.let {
            ViewCompat.setOnApplyWindowInsetsListener(root) { v: View?, insets: WindowInsetsCompat ->
                val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                root.setPadding(0, 0, 0, inset.bottom + root.paddingBottom)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    open fun onHandleOnBackPressed() {
        dismiss()
    }

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