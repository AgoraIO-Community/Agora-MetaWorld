package io.agora.metachat.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultLauncher
import io.agora.metachat.baseui.BaseUiActivity
import io.agora.metachat.databinding.MchatActivityMainBinding
import io.agora.metachat.game.MChatGameActivity
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.tools.LogTools

class MChatMainActivity : BaseUiActivity<MchatActivityMainBinding>() {

    companion object {

        fun startActivity(context: Context) {
            val intent = Intent(context, MChatMainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            context.startActivity(intent)
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): MchatActivityMainBinding? {
        return MchatActivityMainBinding.inflate(inflater)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        LogTools.d("MChatMainActivity===","onSaveInstanceState")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        LogTools.d("MChatMainActivity===","onRestoreInstanceState")
    }

    override fun onBackPressed() {

    }
}