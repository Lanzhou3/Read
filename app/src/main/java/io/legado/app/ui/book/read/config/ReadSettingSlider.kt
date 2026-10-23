package io.legado.app.ui.book.read.config

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import io.legado.app.R
import io.legado.app.databinding.ViewReadSettingSliderBinding
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.ui.widget.seekbar.SeekBarChangeListener

/** Large touch targets and a visible value without changing stored setting units. */
class ReadSettingSlider @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs), SeekBarChangeListener {
    private val binding = ViewReadSettingSliderBinding.inflate(LayoutInflater.from(context), this, true)
    var onChanged: ((Int) -> Unit)? = null
    var valueFormat: ((Int) -> String)? = null
        set(value) { field = value; updateValue() }
    var progress: Int
        get() = binding.seekBar.progress
        set(value) { binding.seekBar.progress = value; updateValue() }
    var max: Int
        get() = binding.seekBar.max
        set(value) { binding.seekBar.max = value }

    init {
        val values = context.obtainStyledAttributes(attrs, R.styleable.DetailSeekBar)
        val title = values.getText(R.styleable.DetailSeekBar_title)
        binding.tvSeekTitle.text = title
        binding.seekBar.max = values.getInteger(R.styleable.DetailSeekBar_max, 100)
        values.recycle()
        val accent = ReadingPanelStyle.accent(context)
        val ink = ReadingPanelStyle.ink(context)
        binding.tvSeekTitle.setTextColor(ink)
        binding.tvSeekValue.setTextColor(accent)
        binding.tvSeekValue.background = ReadingPanelStyle.shape(context, ReadingPanelStyle.tile(context), 8f)
        val density = resources.displayMetrics.density
        binding.seekBar.apply {
            contentDescription = title
            thumb = ReadingPanelStyle.shape(context, accent, 10f).apply {
                setSize((18 * density).toInt(), (18 * density).toInt())
                setStroke((3 * density).toInt(), context.bottomBackground)
            }
            thumbTintList = null
            val track = ReadingPanelStyle.shape(context, ReadingPanelStyle.tile(context), 3f)
            val fill = ClipDrawable(ReadingPanelStyle.shape(context, accent, 3f), Gravity.START, ClipDrawable.HORIZONTAL)
            progressDrawable = LayerDrawable(arrayOf(track, fill)).apply {
                setId(0, android.R.id.background)
                setId(1, android.R.id.progress)
            }
            progressTintList = ColorStateList.valueOf(accent)
            progressBackgroundTintList = null
            setOnSeekBarChangeListener(this@ReadSettingSlider)
        }
        listOf(binding.ivSeekReduce, binding.ivSeekPlus).forEach {
            it.setColorFilter(ink)
            ReadingPanelStyle.action(it)
        }
        binding.ivSeekReduce.contentDescription = "$title · ${context.getString(R.string.reduce)}"
        binding.ivSeekPlus.contentDescription = "$title · ${context.getString(R.string.plus)}"
        binding.ivSeekReduce.setOnClickListener { changeBy(-1) }
        binding.ivSeekPlus.setOnClickListener { changeBy(1) }
        updateValue()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.seekBar.isEnabled = enabled
        binding.ivSeekReduce.isEnabled = enabled
        binding.ivSeekPlus.isEnabled = enabled
        binding.root.alpha = if (enabled) 1f else 0.45f
    }

    private fun changeBy(delta: Int) {
        val next = (progress + delta).coerceIn(0, max)
        if (next == progress) return
        progress = next
        onChanged?.invoke(next)
    }

    private fun updateValue() {
        binding.tvSeekValue.text = valueFormat?.invoke(progress) ?: progress.toString()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) = updateValue()
    override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
    override fun onStopTrackingTouch(seekBar: SeekBar) { onChanged?.invoke(progress) }
}
