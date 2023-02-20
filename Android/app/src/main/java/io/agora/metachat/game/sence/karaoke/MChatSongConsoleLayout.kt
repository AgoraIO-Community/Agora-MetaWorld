package io.agora.metachat.game.sence.karaoke

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import io.agora.metachat.R
import io.agora.metachat.databinding.MchatItemSongScenceBinding
import io.agora.metachat.databinding.MchatViewSongConsoleLayoutBinding
import io.agora.metachat.game.model.ConsoleAudioEffect
import io.agora.metachat.tools.LogTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 */
class MChatSongConsoleLayout : ConstraintLayout {

    private lateinit var binding: MchatViewSongConsoleLayoutBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context)
    }

    private var mAudioEffect: MChatAudioEffect = MChatAudioEffect.Studio
    var chatKaraokeManager: MChatKaraokeManager? = null
    var onConsoleListener: OnConsoleListener? = null
    private var effectAdapter: BaseQuickAdapter<ConsoleAudioEffect, MChatAudioEffectAdapter.VH>? = null
    private lateinit var pitchLayout: PitchLayout

    private fun initView(context: Context) {
        val root = View.inflate(context, R.layout.mchat_view_song_console_layout, this)
        binding = MchatViewSongConsoleLayoutBinding.bind(root)
        binding.ivConsoleBack.setOnClickListener(OnIntervalClickListener(this::onClickBack))
        binding.checkboxOriginalSinging.setOnCheckedChangeListener { buttonView, isChecked ->
            LogTools.d("Original Singing isChecked:$isChecked")
            chatKaraokeManager?.enableUseOriginal(isChecked)
            onConsoleListener?.onUseOriginal(isChecked)
        }
        binding.checkboxEarphoneMonitoring.setOnCheckedChangeListener { buttonView, isChecked ->
            LogTools.d("Earphone Monitoring isChecked:$isChecked")
            chatKaraokeManager?.enableInEarMonitoring(isChecked)
            onConsoleListener?.onEarMonitoring(isChecked)
        }
        pitchLayout = PitchLayout(binding.root.findViewById(R.id.include_console_pitch_select), completion = {
            chatKaraokeManager?.setAudioPitch(it)
            onConsoleListener?.onPitchChanged(it)
        })
        binding.seekbarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                chatKaraokeManager?.adjustRecordingSignalVolume(seekBar.progress)
                onConsoleListener?.onVolumeChanged(seekBar.progress)
                LogTools.d("console volume:${seekBar.progress}")
            }
        })
        binding.seekbarAccompanimentMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                chatKaraokeManager?.adjustAccompanyVolume(seekBar.progress)
                onConsoleListener?.onAccompanyVolumeChange(seekBar.progress)
                LogTools.d("console accompany volume:${seekBar.progress}")
            }
        })
        effectAdapter = MChatAudioEffectAdapter()
        effectAdapter?.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<ConsoleAudioEffect> {
            override fun onClick(adapter: BaseQuickAdapter<ConsoleAudioEffect, *>, view: View, position: Int) {
                val audioEffect = adapter.getItem(position) ?: return

                if (audioEffect.audioEffect == mAudioEffect) return
                mAudioEffect = audioEffect.audioEffect
                adapter.getItem(position)?.let {
                    chatKaraokeManager?.setEffect(it.audioEffect)
                    onConsoleListener?.onAudioEffectChanged(it.audioEffect)
                }
                effectAdapter?.notifyDataSetChanged()
            }

        })
        binding.rvSongScene.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.rvSongScene.adapter = effectAdapter
        effectAdapter?.submitList(MChatKaraokeConstructor.buildAudioEffect(context))

        postDelayed({
            chatKaraokeManager?.let {
                binding.checkboxOriginalSinging.isChecked = it.useOriginal
                binding.checkboxEarphoneMonitoring.isChecked = it.enableEarMonitor
                pitchLayout.resetBars(it.pitchValue)
                binding.seekbarVolume.progress = it.recordingSignalVolume
                binding.seekbarAccompanimentMusic.progress = it.accompanimentVolume
                mAudioEffect = it.audioEffect
                effectAdapter?.notifyDataSetChanged()
            }
        }, 100)

    }

    // 关闭设置页面
    private fun onClickBack(view: View) {
        onConsoleListener?.onConsoleClosed()
    }

    /**room list adapter*/
    inner class MChatAudioEffectAdapter() : BaseQuickAdapter<ConsoleAudioEffect, MChatAudioEffectAdapter.VH>() {
        //自定义ViewHolder类
        inner class VH constructor(
            val parent: ViewGroup,
            val binding: MchatItemSongScenceBinding = MchatItemSongScenceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
            return VH(parent)
        }

        override fun onBindViewHolder(holder: VH, position: Int, data: ConsoleAudioEffect?) {
            data ?: return
            holder.binding.ivSongSceneBg.isVisible = data.audioEffect == mAudioEffect
            holder.binding.ivSongScene.setImageResource(data.effectBg)
            holder.binding.tvSongSceneName.text = data.effectTxt
        }

        private fun getPosOfAudioEffect(effect: MChatAudioEffect): Int {
            return when (effect) {
                MChatAudioEffect.Studio -> 0
                MChatAudioEffect.Concert -> 1
                MChatAudioEffect.KTV -> 2
                MChatAudioEffect.Spatial -> 3
                else -> -1
            }
        }
    }
}

internal class PitchLayout(
    private val layout: RelativeLayout,
    private val completion: (pitch: Int) -> Unit
) {

    private var upBtn: ImageView = layout.findViewById(R.id.pitch_select_up)
    private var downBtn: ImageView = layout.findViewById(R.id.pitch_select_down)

    var level: Int = 0

    private val barIds = arrayOf(
        R.id.pitch_minus_6,
        R.id.pitch_minus_5,
        R.id.pitch_minus_4,
        R.id.pitch_minus_3,
        R.id.pitch_minus_2,
        R.id.pitch_minus_1,
        R.id.pitch_original,
        R.id.pitch_1,
        R.id.pitch_2,
        R.id.pitch_3,
        R.id.pitch_4,
        R.id.pitch_5,
        R.id.pitch_6
    )

    private val barViews = arrayListOf<View>()

    init {
        barIds.forEach { resId ->
            barViews.add(layout.findViewById(resId))
        }
        resetBars(level)
        upBtn.setOnClickListener {
            level++
            resetBars(level)
            completion.invoke(level)
        }
        downBtn.setOnClickListener {
            level--
            resetBars(level)
            completion.invoke(level)
        }
    }

    fun resetBars(level: Int) {
        var l = if (level < -6) -6
        else if (level > 6) 6
        else level
        this.level = l
        l += 6

        barViews.forEachIndexed { index, view ->
            view.isActivated = index <= l
        }
    }
}

interface OnConsoleListener {
    fun onUseOriginal(original: Boolean)

    fun onEarMonitoring(monitor: Boolean)

    fun onPitchChanged(pitch: Int)

    fun onVolumeChanged(volume: Int)

    fun onAccompanyVolumeChange(volume: Int)

    fun onAudioEffectChanged(effect: MChatAudioEffect)

    fun onConsoleClosed()
}
