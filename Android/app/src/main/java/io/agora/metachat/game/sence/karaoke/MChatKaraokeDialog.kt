package io.agora.metachat.game.sence.karaoke

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.databinding.MchatDialogKaraokeBinding
import io.agora.metachat.game.model.MusicDetail
import io.agora.metachat.game.sence.MChatMediaPlayerListener
import io.agora.metachat.service.MChatServiceProtocol
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 */
class MChatKaraokeDialog constructor(
    private val karaokeManager: MChatKaraokeManager? = null,
    private val listener: OnKaraokeDialogListener? = null,

    ) : BaseFragmentDialog<MchatDialogKaraokeBinding>() {

    private var songOrderLayout: MChatSongOrderLayout? = null
    private var playlistLayout: MChatSongPlaylistLayout? = null
    private var consoleLayout: MChatSongConsoleLayout? = null

    private val tabTitle = mutableListOf<String>()
    private var playlistTitleView: TextView? = null

    private val chatServiceProtocol: MChatServiceProtocol = MChatServiceProtocol.getImplInstance()

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogKaraokeBinding {
        return MchatDialogKaraokeBinding.inflate(inflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        karaokeManager?.registerListener(karaokeListener)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        karaokeManager?.unregisterListener(karaokeListener)
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.attributes.windowAnimations = R.style.mchat_anim_left_to_right
            // Remove the system default rounded corner background
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(0f)
            window.decorView.setPadding(0, 0, 0, 0)

            window.attributes.apply {
                width = WindowManager.LayoutParams.WRAP_CONTENT
                activity?.let {
                    height = DeviceTools.screenHeight(it)
                }
                gravity = Gravity.START
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
                window.attributes = this
            }
        }
    }

    // 更新ui
    private val karaokeListener = object : MChatMediaPlayerListener {

        override fun onPlayCompleted(url: String) {
            resetPlaylistTabTitle()
            playlistLayout?.refreshPlaylist()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(true)
        initView()
    }

    private fun initView() {
        binding?.apply {
            tabTitle.clear()
            tabTitle.add(resources.getString(R.string.mchat_choose_a_song))
            tabTitle.add(resources.getString(R.string.mchat_selected))
            ivSongConsole.setOnClickListener(OnIntervalClickListener(this@MChatKaraokeDialog::onClickConsole))
            songOrderLayout = MChatSongOrderLayout(root.context).also {
                it.chatKaraokeManager = karaokeManager
                it.refreshSongOrder()
                it.onMusicSelectListener = object : OnMusicSelectListener {

                    override fun onMusicSelected(select: Boolean, musicDetail: MusicDetail?) {
                        musicDetail ?: return
                        if (select) {
                            playlistLayout?.insertSong(musicDetail)
                        } else {
                            playlistLayout?.deleteSong(musicDetail)
                        }
                        songOrderLayout?.refreshSongOrder()
                        listener?.onMusicInserted(select, musicDetail)
                        resetPlaylistTabTitle()
                    }

                }
            }
            playlistLayout = MChatSongPlaylistLayout(root.context).also {
                it.chatKaraokeManager = karaokeManager
                it.initAdapterData()
                it.refreshPlaylist()
                it.operateListener = object : OnPlaylistOperateListener {
                    override fun onMusicSwitch(musicDetail: MusicDetail?) {
                        musicDetail ?: return
                        resetPlaylistTabTitle()
                    }

                    override fun onMusicDelete(musicDetail: MusicDetail?) {
                        musicDetail ?: return
                        resetPlaylistTabTitle()
                    }

                    override fun onMusicMovedTop(musicDetail: MusicDetail?) {
                        musicDetail ?: return
                        resetPlaylistTabTitle()
                    }
                }
            }
            consoleLayout = MChatSongConsoleLayout(root.context).also {
                layoutConsoleContainer.addView(it)
                it.chatKaraokeManager = karaokeManager
                it.onConsoleListener = object : OnConsoleListener {
                    override fun onUseOriginal(original: Boolean) {
                        if (original) {
                            chatServiceProtocol.enableOriginalSinging {

                            }
                        } else {
                            chatServiceProtocol.disableOriginalSinging {

                            }
                        }
                    }

                    override fun onEarMonitoring(monitor: Boolean) {
                        if (monitor) {
                            chatServiceProtocol.enableEarphoneMonitoring {

                            }
                        } else {
                            chatServiceProtocol.disableEarphoneMonitoring {

                            }
                        }
                    }

                    override fun onPitchChanged(pitch: Int) {
                        chatServiceProtocol.changePitchSong(pitch) {

                        }
                    }

                    override fun onVolumeChanged(volume: Int) {
                    }

                    override fun onAccompanyVolumeChange(volume: Int) {
                        chatServiceProtocol.changeAccompanimentVolume(volume) {

                        }
                    }

                    override fun onAudioEffectChanged(effect: MChatAudioEffect) {
                        chatServiceProtocol.changeAudioEffect(effect.value) {}
                    }

                    override fun onConsoleClosed() {
                        layoutConsoleContainer.isVisible = false
                        groupSongList.isVisible = true
                        isCancelable = true
                    }
                }
            }
            initViewPage()
            resetPlaylistTabTitle()
            root.post {
                karaokeManager?.findAndPlayFirstMusic()
            }
        }

    }

    // 点击控制台
    private fun onClickConsole(view: View) {
        binding?.apply {
            layoutConsoleContainer.isVisible = true
            groupSongList.isVisible = false
            isCancelable = false
        }
    }

    private fun initViewPage() {
        binding?.apply {
            vpSongPager.adapter = songPagerAdapter
            val tabMediator = TabLayoutMediator(tabSong, vpSongPager) { tab, position ->
                val customView =
                    LayoutInflater.from(root.context).inflate(R.layout.mchat_item_song_pager_tab, tab.view, false)
                val tabText = customView.findViewById<TextView>(R.id.tv_tab_name)
                tab.customView = customView
                tabText.text = tabTitle[position]
                if (position == 0) {
                    onTabLayoutSelected(tab)
                } else {
                    onTabLayoutUnselected(tab)
                }
                if (position == 1) playlistTitleView = tabText
            }
            tabMediator.attach()
            tabSong.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    onTabLayoutSelected(tab)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    onTabLayoutUnselected(tab)
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            })
            vpSongPager.setCurrentItem(0, false)
        }
    }

    private fun onTabLayoutSelected(tab: TabLayout.Tab) {
        tab.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.tv_tab_name)
            tabText.setTextColor(DeviceTools.getColor(resources, R.color.mchat_white))
            tabText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tabText.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            val tabTip = it.findViewById<View>(R.id.v_tab_indicator)
            tabTip.visibility = View.VISIBLE
        }
    }

    private fun onTabLayoutUnselected(tab: TabLayout.Tab) {
        tab.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.tv_tab_name)
            tabText.setTextColor(DeviceTools.getColor(resources, R.color.mchat_white_60))
            tabText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            tabText.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            val tabTip = it.findViewById<View>(R.id.v_tab_indicator)
            tabTip.visibility = View.GONE
        }
    }


    @MainThread
    private fun resetPlaylistTabTitle() {
        val playlistSize = karaokeManager?.songListPlaylist?.size ?: 0
        playlistTitleView?.text = if (playlistSize == 0) tabTitle[1] else "${tabTitle[1]} $playlistSize"
    }

    private val songPagerAdapter = object : RecyclerView.Adapter<SongPagerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongPagerViewHolder {
            val layout = RelativeLayout(parent.context).also {
                it.layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
            }
            return SongPagerViewHolder(layout)
        }

        override fun onBindViewHolder(holder: SongPagerViewHolder, position: Int) {
            if (position == 0) {
                songOrderLayout?.let {
                    holder.getContainer().addView(
                        it,
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                }
            } else if (position == 1) {
                playlistLayout?.let {
                    holder.getContainer().addView(
                        it,
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                }
            }
        }

        override fun getItemCount(): Int {
            return tabTitle.size
        }
    }

    // 歌单，已点
    inner class SongPagerViewHolder(private val container: RelativeLayout) : RecyclerView.ViewHolder(container) {

        fun getContainer(): RelativeLayout {
            return container
        }
    }
}

interface OnKaraokeDialogListener {
    fun onMusicInserted(insert: Boolean, detail: MusicDetail)
    fun onConsoleOpened()
}