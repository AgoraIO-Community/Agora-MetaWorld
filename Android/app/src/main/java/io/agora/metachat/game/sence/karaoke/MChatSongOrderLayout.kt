package io.agora.metachat.game.sence.karaoke

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.metachat.R
import io.agora.metachat.databinding.MchatItemSongOrderBinding
import io.agora.metachat.databinding.MchatViewSongTypeLayoutBinding
import io.agora.metachat.game.model.MusicDetail
import io.agora.metachat.tools.DeviceTools

/**
 * @author create by zhangwei03
 *
 * 歌单列表
 */
class MChatSongOrderLayout : ConstraintLayout {

    private lateinit var binding: MchatViewSongTypeLayoutBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context)
    }

    var chatKaraokeManager: MChatKaraokeManager? = null
    var onMusicSelectListener: OnMusicSelectListener? = null

    private fun initView(context: Context) {
        val root = View.inflate(context, R.layout.mchat_view_song_type_layout, this)
        binding = MchatViewSongTypeLayoutBinding.bind(root)
        binding.vpSongOrder.adapter = songStylePagerAdapter
        val tabMediator = TabLayoutMediator(binding.tabSongOrder, binding.vpSongOrder) { tab, position ->
            val customView =
                LayoutInflater.from(root.context).inflate(R.layout.mchat_item_song_type_tab, tab.view, false)
            val tabText = customView.findViewById<TextView>(R.id.tv_tab_name)
            tab.customView = customView

            val songTYpe = MChatKaraokeManager.songTypeList[position]
            tabText.text = songTYpe.toTypeString(context)
            if (position == 0) {
                onTabLayoutSelected(tab)
            } else {
                onTabLayoutUnselected(tab)
            }
        }
        tabMediator.attach()
        binding.tabSongOrder.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                onTabLayoutSelected(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                onTabLayoutUnselected(tab)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
        binding.vpSongOrder.setCurrentItem(0, false)
    }

    private fun onTabLayoutSelected(tab: TabLayout.Tab) {
        tab.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.tv_tab_name)
            tabText.setTextColor(DeviceTools.getColor(resources, R.color.mchat_white))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        }
    }

    private fun onTabLayoutUnselected(tab: TabLayout.Tab) {
        tab.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.tv_tab_name)
            tabText.setTextColor(DeviceTools.getColor(resources, R.color.mchat_white_60))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        }
    }

    fun refreshSongOrder() {
        songStylePagerAdapter.notifyDataSetChanged()
    }

    private val songStylePagerAdapter = object : RecyclerView.Adapter<SongTypePagerHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongTypePagerHolder {
            return SongTypePagerHolder(
                LayoutInflater.from(context).inflate(R.layout.mchat_view_song_order_layout, parent, false)
            )
        }

        override fun onBindViewHolder(holder: SongTypePagerHolder, position: Int) {
            holder.songOrderRecycler.layoutManager = LinearLayoutManager(holder.itemView.context)
            val songOrderAdapter = MChatSongOrderAdapter()
                .addOnItemChildClickListener(R.id.iv_music_operation) { adapter, view, position ->
                    // 已点歌单是否包含
                    val data = adapter.getItem(position)
                    val playlistContains = chatKaraokeManager?.songListPlaylist?.contains(data) == true
                    onMusicSelectListener?.onMusicSelected(!playlistContains,data)
                }
            songOrderAdapter.submitList(getSongDetailsList(position))
            holder.songOrderRecycler.adapter = songOrderAdapter
        }

        private fun getSongDetailsList(position: Int): List<MusicDetail> {
            val songType = MChatKaraokeManager.songTypeList[position]
            return MChatKaraokeManager.songListMap[songType] ?: mutableListOf()
        }

        override fun getItemCount(): Int {
            return MChatKaraokeManager.songTypeList.size
        }
    }

    // 歌曲类别
    inner class SongTypePagerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songOrderRecycler = itemView.findViewById<RecyclerView>(R.id.rv_song_order)
    }

    ///歌曲类别详情
    inner class MChatSongOrderAdapter : BaseQuickAdapter<MusicDetail, MChatSongOrderAdapter.VH>() {
        //自定义ViewHolder类
        inner class VH constructor(
            val parent: ViewGroup,
            val binding: MchatItemSongOrderBinding = MchatItemSongOrderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onBindViewHolder(holder: VH, position: Int, data: MusicDetail?) {
            data ?: return
            var singDrawable = DeviceTools.getDrawableId(context, data.poster)
            if (singDrawable == 0) singDrawable = R.drawable.mchat_portrait0
            holder.binding.ivMusicSinger.setImageResource(singDrawable)
            holder.binding.tvSongName.text = data.name
            holder.binding.tvMusicSinger.text = data.singer
            holder.binding.tvMusicOriginal.isVisible = data.supportOriginal()
            holder.binding.tvMusicAccompany.isVisible = data.supportAccompany()
            holder.binding.tvMusicScore.isVisible = data.supportScore()
            if (chatKaraokeManager?.songListPlaylist?.contains(data) == true) {
                holder.binding.ivMusicOperation.setImageResource(R.drawable.mchat_ic_minus)
            } else {
                holder.binding.ivMusicOperation.setImageResource(R.drawable.mchat_ic_plus_white)
            }
            holder.binding.ivMusicOperation.setOnClickListener {
                // 已点歌单是否包含
                val playlistContains = chatKaraokeManager?.songListPlaylist?.contains(data) == true
                onMusicSelectListener?.onMusicSelected(!playlistContains, data)
            }
        }

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
            return VH(parent)
        }
    }
}

interface OnMusicSelectListener {
    fun onMusicSelected(select: Boolean, musicDetail: MusicDetail?)
}