package io.agora.metachat.game

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.*
import android.view.TextureView.SurfaceTextureListener
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseUiActivity
import io.agora.metachat.baseui.dialog.CommonFragmentAlertDialog
import io.agora.metachat.databinding.MchatActivityGameBinding
import io.agora.metachat.game.dialog.MChatBeginnerDialog
import io.agora.metachat.game.dialog.MChatSettingsDialog
import io.agora.metachat.game.dialog.OnUpdateUserListener
import io.agora.metachat.game.model.MusicDetail
import io.agora.metachat.game.sence.MChatContext
import io.agora.metachat.game.sence.SceneCmdListener
import io.agora.metachat.game.sence.SceneMessageReceivePositions
import io.agora.metachat.game.sence.SceneObjectId
import io.agora.metachat.game.sence.karaoke.MChatKaraokeDialog
import io.agora.metachat.game.sence.karaoke.MChatKaraokeManager
import io.agora.metachat.game.sence.karaoke.OnKaraokeDialogListener
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.home.MChatMainActivity
import io.agora.metachat.imkit.MChatMessageDialog
import io.agora.metachat.service.MChatServiceProtocol
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ThreadTools
import io.agora.metachat.widget.OnIntervalClickListener
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

/**
 * @author create by zhangwei03
 *
 * unity game
 */
class MChatGameActivity : BaseUiActivity<MchatActivityGameBinding>(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    companion object {

        private const val defaultPortrait = R.drawable.mchat_portrait0
        const val RC_PERMISSIONS = 101
        fun startActivity(launcher: ActivityResultLauncher<Intent>, context: Context, roomId: String) {
            val intent = Intent(context, MChatGameActivity::class.java).apply {
                putExtra(MChatConstant.Params.KEY_ROOM_ID, roomId)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            launcher.launch(intent)
        }
    }

    private val chatContext by lazy {
        MChatContext.instance()
    }

    private var mTextureView: TextureView? = null
    private lateinit var gameViewModel: MChatGameViewModel

    private val roomId by lazy { intent.getStringExtra(MChatConstant.Params.KEY_ROOM_ID) ?: "" }

    // karaoke manager
    private var karaokeManager: MChatKaraokeManager? = null

    private var messageDialog: MChatMessageDialog? = null
    private var karaokeDialog: MChatKaraokeDialog? = null
    private var settingDialog: MChatSettingsDialog? = null

    /**portrait */
    private lateinit var portraitArray: TypedArray

    override fun getViewBinding(inflater: LayoutInflater): MchatActivityGameBinding {
        return MchatActivityGameBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 强制横屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        gameViewModel = ViewModelProvider(this).get(MChatGameViewModel::class.java)
        gameViewModel.resetSceneState()
        initView()
        requestPermission()
        gameObservable()
    }

    override fun onNewIntent(intent: Intent?) {
        gameViewModel.mReCreateScene = true
        //just for call setRequestedOrientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onNewIntent(intent)
        mTextureView?.let {
            val result = gameViewModel.maybeCreateScene(this@MChatGameActivity, roomId, it)
            if (result) resetViewVisibility()
        }
    }

    private fun initView() {
        portraitArray = resources.obtainTypedArray(R.array.mchat_portrait)
        binding.tvNickname.text = MChatKeyCenter.nickname
        binding.ivUserPortrait.setImageResource(
            portraitArray.getResourceId(MChatKeyCenter.portraitIndex, defaultPortrait)
        )
        binding.tvMicOnline.setOnClickListener(OnIntervalClickListener(this::onClickMicOnline))
        binding.layoutMuteRemote.setOnClickListener(OnIntervalClickListener(this::onClickMuteRemote))
        binding.layoutMuteLocal.setOnClickListener(OnIntervalClickListener(this::onClickMuteLocal))
        binding.ivSettings.setOnClickListener(OnIntervalClickListener(this::onClickSettings))
        binding.ivMsg.setOnClickListener(OnIntervalClickListener(this::onClickMsg))
        binding.tvVisitorMode.setOnClickListener(OnIntervalClickListener(this::onClickVisitor))
        binding.tvNoviceGuide.setOnClickListener(OnIntervalClickListener(this::onClickNovice))
        binding.linearSongList.setOnClickListener(OnIntervalClickListener(this::onClickSongList))
        binding.linearEndSong.setOnClickListener(OnIntervalClickListener(this::onClickEndSong))
    }

    private fun resetViewVisibility() {
        binding.groupNativeView.isVisible = false
    }

    // 上下麦
    private fun onClickMicOnline(view: View) {
        gameViewModel.sendOnlineEvent()
    }

    // 禁远端
    private fun onClickMuteRemote(view: View) {
        gameViewModel.sendMuteRemoteEvent()
    }

    // 禁本地
    private fun onClickMuteLocal(view: View) {
        gameViewModel.sendMuteLocalEvent()
    }

    // 设置
    private fun onClickSettings(view: View) {
        if (settingDialog == null) {
            settingDialog = MChatSettingsDialog()
            settingDialog?.setOnUpdateUserListener(object : OnUpdateUserListener {
                override fun onNickname() {
                    binding.tvNickname.text = MChatKeyCenter.nickname
                }

                override fun onUserPortrait() {
                    binding.ivUserPortrait.setImageResource(
                        portraitArray.getResourceId(MChatKeyCenter.portraitIndex, defaultPortrait)
                    )
                }

            })
            settingDialog?.setExitCallback {
                showLoading(false)
                gameViewModel.leaveRoom()
            }
        }
        settingDialog?.show(supportFragmentManager, "settings dialog")
    }

    // 聊天
    private fun onClickMsg(view: View) {
        if (messageDialog == null) {
            messageDialog = MChatMessageDialog()
        }
        messageDialog?.show(supportFragmentManager, "message dialog")
    }

    // 游客模式说明
    private fun onClickVisitor(view: View) {
        // 上麦就是语聊模式，不显示游客模式弹框
        if (gameViewModel.onlineMicObservable().value == true) return
        MChatBeginnerDialog(MChatBeginnerDialog.VISITOR_TYPE).show(supportFragmentManager, "visitor dialog")
    }

    // 新手引导说明
    private fun onClickNovice(view: View) {
        MChatBeginnerDialog(MChatBeginnerDialog.NOVICE_TYPE).show(supportFragmentManager, "novice dialog")
    }

    // 点击歌单列表
    private fun onClickSongList(view: View) {
        showKaraokeDialog()
    }

    // 点击结束k歌
    private fun onClickEndSong(view: View) {
        unityCmdListener.onKaraokeStopped()
        chatContext.getUnityCmd()?.stopKaraoke()
    }

    // 申请麦克风权限
    private fun requestPermission() {
        val perms = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            onPermissionGrant()
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(PermissionRequest.Builder(this, RC_PERMISSIONS, *perms).build())
        }
    }

    private fun showKaraokeDialog() {
        if (karaokeDialog == null) {
            karaokeDialog = MChatKaraokeDialog(karaokeManager, object : OnKaraokeDialogListener {
                override fun onMusicInserted(insert: Boolean, detail: MusicDetail) {
                }

                override fun onConsoleOpened() {

                }
            })
        }
        karaokeDialog?.show(supportFragmentManager, "karaoke dialog")
    }

    private fun dismissKaraokeDialog() {
        karaokeDialog?.dismissAllowingStateLoss()
        karaokeDialog = null
    }

    private fun gameObservable() {
        gameViewModel.isEnterSceneObservable().observe(this) { enter ->
            if (enter) binding.groupNativeView.isVisible = true
            // 进入场景注册unity 监听
            chatContext.getUnityCmd()?.let { unityCmd ->
                if (enter) {
                    unityCmd.registerListener(unityCmdListener)
                } else {
                    unityCmd.unregisterListener(unityCmdListener)
                }
            }
            karaokeManager = MChatKaraokeManager(chatContext)
        }
        gameViewModel.onlineMicObservable().observe(this) {
            if (it) {
                binding.layoutUser.setBackgroundResource(R.drawable.mchat_bg_rect_radius18_gradient_pure)
                binding.tvMicOnline.setBackgroundResource(R.drawable.mchat_bg_rect_radius14_stroke_white15)
                binding.tvMicOnline.setText(R.string.mchat_online)
                binding.layoutMuteLocal.isVisible = true
                binding.tvVisitorMode.setText(R.string.mchat_audio_chat_mode)
                binding.tvVisitorMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            } else {
                binding.layoutUser.setBackgroundResource(R.drawable.mchat_bg_rect_radius18_black40)
                binding.tvMicOnline.setBackgroundResource(R.drawable.mchat_bg_rect_radius14_purple)
                binding.tvMicOnline.setText(R.string.mchat_offline)
                binding.layoutMuteLocal.isVisible = false
                binding.tvVisitorMode.setText(R.string.mchat_visitor_mode)
                binding.tvVisitorMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.mchat_ic_tips, 0)
            }
        }
        gameViewModel.muteRemoteObservable().observe(this) {
            binding.ivMuteRemoteFlag.isVisible = !it
        }
        gameViewModel.muteLocalObservable().observe(this) {
            binding.ivMuteLocalFlag.isVisible = !it
        }
        gameViewModel.exitGameObservable().observe(this) {
            karaokeManager?.clearSubscribe()
            dismissLoading()
            settingDialog?.dismiss()
            settingDialog = null
            MChatMainActivity.startActivity(this@MChatGameActivity)
        }
        gameViewModel.leaveRoomObservable().observe(this) {
            if (it) {
                chatContext.leaveScene()
            }
        }
        gameViewModel.sceneConnectErrorObservable().observe(this) {
            if (it.first == 4) {
                CommonFragmentAlertDialog()
                    .titleText(resources.getString(R.string.mchat_notice))
                    .contentText(
                        resources.getString(R.string.mchat_scene_error, "${it.first}", "${it.second}")
                    )
                    .rightText(resources.getString(R.string.mchat_exit))
                    .showSingleBtn(true)
                    .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                        override fun onConfirmClick() {
                            chatContext.leaveScene()
                        }
                    }).show(supportFragmentManager, "error")
            }
        }
        gameViewModel.groupDestroyRoomObservable().observe(this) {
            if (it) {
                CommonFragmentAlertDialog()
                    .titleText(resources.getString(R.string.mchat_notice))
                    .contentText(resources.getString(R.string.mchat_delete_room_notify_alert_title))
                    .rightText(resources.getString(R.string.mchat_exit))
                    .showSingleBtn(true)
                    .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                        override fun onConfirmClick() {
                            chatContext.leaveScene()
                        }
                    }).show(supportFragmentManager, "roomDestroy")
            }
        }
    }

    private fun initUnityView() {
        mTextureView = TextureView(this)
        mTextureView?.let {
            it.surfaceTextureListener = object : SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
                    gameViewModel.createScene(this@MChatGameActivity, roomId, it)
                    resetViewVisibility()
                }

                override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
                    val result = gameViewModel.maybeCreateScene(this@MChatGameActivity, roomId, it)
                    if (result) resetViewVisibility()
                }

                override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                    return false
                }

                override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
                }
            }
            val layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            binding.unityContainer.addView(it, layoutParams)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun onPermissionGrant() {
        gameViewModel.initMChatScene()
        initUnityView()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        LogTools.d("onPermissionsGranted requestCode$requestCode $perms")
        if (requestCode == RC_PERMISSIONS) {
            onPermissionGrant()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        LogTools.d("onPermissionsDenied requestCode:$requestCode $perms")
    }

    override fun onRationaleAccepted(requestCode: Int) {
        LogTools.d("onRationaleAccepted requestCode:$requestCode")
        if (requestCode == RC_PERMISSIONS) {
            onPermissionGrant()
        }
    }

    override fun onRationaleDenied(requestCode: Int) {
        LogTools.d("onRationaleDenied requestCode:$requestCode")
    }

    override fun onPause() {
        super.onPause()
        if (chatContext.isInScene()) {
            chatContext.chatMediaPlayer()?.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (chatContext.isInScene()) {
            chatContext.chatMediaPlayer()?.resume()
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }

    // unity listener
    private var unityCmdListener = object : SceneCmdListener {
        override fun onObjectPositionAcquired(position: SceneMessageReceivePositions) {
            val p = position.position ?: floatArrayOf(0.0f, 0.0f, 0.0f)

            val f = position.forward ?: floatArrayOf(0.0f, 0.0f, 0.0f)

            val id: Int? = when (position.objectId) {
                SceneObjectId.TV.value -> chatContext.chatMediaPlayer()?.mediaPlayerId()
                else -> {
                    chatContext.chatNpcManager()?.getNpc(position.objectId)?.playerId() ?: -1
                }
            }
            id?.let {
                chatContext.chatSpatialAudio()?.updateLocalMediaPlayerPosition(it, p, f)
            }
        }

        override fun onKaraokeStarted() {
            karaokeManager?.startKaraoke()
            chatContext.chatNpcManager()?.stopAll()
            MChatServiceProtocol.getImplInstance().sendStartKaraoke { }
            ThreadTools.get().runOnMainThread {
                binding.linearSongList.isVisible = true
                binding.linearEndSong.isVisible = true
                binding.ivSettings.isVisible = false
                showKaraokeDialog()
            }
        }

        override fun onKaraokeStopped() {
            karaokeManager?.stopKaraoke()
            chatContext.chatNpcManager()?.playAll()
            MChatServiceProtocol.getImplInstance().sendStopKaraoke { }
            ThreadTools.get().runOnMainThread {
                binding.linearSongList.isVisible = false
                binding.linearEndSong.isVisible = false
                binding.ivSettings.isVisible = true
                dismissKaraokeDialog()
            }
        }
    }
}