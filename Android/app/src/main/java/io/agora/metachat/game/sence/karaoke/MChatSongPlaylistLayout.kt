package io.agora.metachat.game.sence.karaoke

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import io.agora.metachat.R
import io.agora.metachat.databinding.MchatItemSongPlaylistBinding
import io.agora.metachat.databinding.MchatViewSongPlaylistLayoutBinding
import io.agora.metachat.game.model.MusicDetail
import io.agora.metachat.tools.DeviceTools

/**
 * @author create by zhangwei03
 */
class MChatSongPlaylistLayout : ConstraintLayout {

    private lateinit var binding: MchatViewSongPlaylistLayoutBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
        initView(context)
    }

    var chatKaraokeManager: MChatKaraokeManager? = null

    var operateListener: OnPlaylistOperateListener? = null

    // 已点列表
    private var playAdapter: BaseQuickAdapter<MusicDetail, MChatSongPlaylistAdapter.VH>? = null

    private fun initView(context: Context) {
        val root = View.inflate(context, R.layout.mchat_view_song_playlist_layout, this)
        binding = MchatViewSongPlaylistLayoutBinding.bind(root)
        binding.rvSongPlaylist.layoutManager = LinearLayoutManager(binding.root.context)
        playAdapter = MChatSongPlaylistAdapter()
            .addOnItemChildClickListener(R.id.iv_music_switch) { adapter, view, position ->
                adapter.getItem(position)?.let {
                    deleteSong(it)
                    operateListener?.onMusicSwitch(adapter.getItem(position))
                }
            }.addOnItemChildClickListener(R.id.iv_music_top) { adapter, view, position ->
                adapter.getItem(position)?.let {
                    moveSongTop(it, position)
                    operateListener?.onMusicMovedTop(adapter.getItem(position))
                }
            }.addOnItemChildClickListener(R.id.iv_music_trashbin) { adapter, view, position ->
                adapter.getItem(position)?.let {
                    deleteSong(it)
                    operateListener?.onMusicDelete(adapter.getItem(position))
                }
            }
        binding.rvSongPlaylist.adapter = playAdapter
    }

    fun initAdapterData(){
        chatKaraokeManager?.let {
            playAdapter?.submitList(it.songListPlaylist)
        }
    }

    // 刷新播放列表
    fun refreshPlaylist() {
        playAdapter?.notifyDataSetChanged()
    }

    // 插入歌曲
    fun insertSong(musicDetail: MusicDetail, index: Int = -1) {
        chatKaraokeManager?.addPlayList(musicDetail, index)
        refreshPlaylist()
    }

    // 移除歌曲
    fun deleteSong(musicDetail: MusicDetail, resetPlay: Boolean = true) {
        chatKaraokeManager?.deletePlaylist(musicDetail, resetPlay)
        refreshPlaylist()
    }

    // 移动歌曲到index 1
    private fun moveSongTop(musicDetail: MusicDetail, index: Int) {
        if (index < 2 || index >= (binding.rvSongPlaylist.adapter?.itemCount ?: -1)) return
        if ((chatKaraokeManager?.songListPlaylist?.size ?: 0) <= 2) return
        deleteSong(musicDetail, false)
        insertSong(musicDetail, 1)
    }

    //播放中列表
    class MChatSongPlaylistAdapter() : BaseQuickAdapter<MusicDetail, MChatSongPlaylistAdapter.VH>() {
        //自定义ViewHolder类
        inner class VH constructor(
            val parent: ViewGroup,
            val binding: MchatItemSongPlaylistBinding = MchatItemSongPlaylistBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
            return VH(parent)
        }

        override fun onBindViewHolder(holder: VH, position: Int, data: MusicDetail?) {
            data ?: return
            holder.binding.tvMusicNum.text = "${position + 1}"
            var singDrawable = DeviceTools.getDrawableId(context, data.poster)
            if (singDrawable == 0) singDrawable = R.drawable.mchat_portrait0
            holder.binding.ivMusicSinger.setImageResource(singDrawable)
            holder.binding.tvSongName.text = data.name
            holder.binding.tvMusicSinger.text = data.singer
            when (position) {
                0 -> {
                    holder.binding.tvMusicSinging.isVisible = true
                    holder.binding.ivMusicSwitch.isVisible = true
                    holder.binding.ivMusicTop.isVisible = false
                    holder.binding.ivMusicTrashbin.isVisible = false
                }
                1 -> {
                    holder.binding.tvMusicSinging.isVisible = false
                    holder.binding.ivMusicSwitch.isVisible = false
                    holder.binding.ivMusicTop.isVisible = false
                    holder.binding.ivMusicTrashbin.isVisible = true
                }
                else -> {
                    holder.binding.tvMusicSinging.isVisible = false
                    holder.binding.ivMusicSwitch.isVisible = false
                    holder.binding.ivMusicTop.isVisible = true
                    holder.binding.ivMusicTrashbin.isVisible = true
                }
            }

        }
    }
}

interface OnPlaylistOperateListener {
    fun onMusicSwitch(musicDetail: MusicDetail?)

    fun onMusicDelete(musicDetail: MusicDetail?)

    fun onMusicMovedTop(musicDetail: MusicDetail?)
}