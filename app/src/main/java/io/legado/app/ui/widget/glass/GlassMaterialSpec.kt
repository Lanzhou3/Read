package io.legado.app.ui.widget.glass

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.ColorUtils as AndroidColorUtils
import io.legado.app.constant.PreferKey
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.accentColor
import io.legado.app.utils.getPrefInt

object GlassMaterialSpec {
    fun strength(context: Context): Int = context.getPrefInt(PreferKey.glassStrength, 70).coerceIn(0, 100)

    fun tintOpacity(context: Context, surface: Int = context.bottomBackground): Float {
        val initial = 0.50f + strength(context) * 0.0015f
        for (step in 0..20) {
            val opacity = initial + (1f - initial) * step / 20f
            if (listOf(null, context.accentColor).all { indicator ->
                val backgrounds = backdropColors(surface, opacity, indicator)
                listOf(Color.BLACK, Color.WHITE).any { color ->
                    backgrounds.all { AndroidColorUtils.calculateContrast(color, it) >= 4.5 }
                }
            }) return opacity
        }
        return 1f
    }

    /** Use the worst contrasting background allowed through the tint, without reading GPU pixels. */
    fun readableForeground(context: Context, preferred: Int, indicator: Int? = null): Int {
        val surface = AndroidColorUtils.setAlphaComponent(context.bottomBackground, 255)
        val backgrounds = backdropColors(surface, tintOpacity(context), indicator)
        return readableForeground(preferred, backgrounds)
    }

    /** The checked capsule stabilizes the backdrop independently of the rest of the glass. */
    fun readableForegroundOnSelection(context: Context, preferred: Int, selection: Int, opacity: Float): Int {
        val backgrounds = backdropColors(context.bottomBackground, tintOpacity(context), null)
            .map { AndroidColorUtils.blendARGB(it, selection, opacity) }
        return readableForeground(preferred, backgrounds)
    }

    private fun readableForeground(preferred: Int, backgrounds: List<Int>): Int {
        val foreground = AndroidColorUtils.setAlphaComponent(preferred, 255)
        val blackContrast = backgrounds.minOf { AndroidColorUtils.calculateContrast(Color.BLACK, it) }
        val whiteContrast = backgrounds.minOf { AndroidColorUtils.calculateContrast(Color.WHITE, it) }
        val target = if (blackContrast >= whiteContrast) Color.BLACK else Color.WHITE
        for (step in 0..20) {
            val candidate = AndroidColorUtils.blendARGB(foreground, target, step / 20f)
            if (backgrounds.all { AndroidColorUtils.calculateContrast(candidate, it) >= 4.5 }) return candidate
        }
        return target
    }

    private fun backdropColors(surface: Int, opacity: Float, indicator: Int?): List<Int> =
        listOf(Color.BLACK, Color.WHITE).map { underlay ->
            val background = AndroidColorUtils.blendARGB(underlay, surface, opacity)
            if (indicator == null) background else AndroidColorUtils.blendARGB(background, indicator, 0.12f)
        }
}
