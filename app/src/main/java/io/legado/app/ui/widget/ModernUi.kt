package io.legado.app.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.getPrimaryTextColor

/** Shared surfaces for navigation helpers and management tools. */
object ModernUi {
    fun dp(context: Context, value: Int) = (value * context.resources.displayMetrics.density).toInt()
    fun ink(context: Context) = context.getPrimaryTextColor(
        ColorUtils.calculateLuminance(context.bottomBackground) > 0.5
    )
    fun accent(context: Context): Int {
        if (AppConfig.isEInkMode) return ink(context)
        var color = context.accentColor
        repeat(10) {
            if (ColorUtils.calculateContrast(color, context.bottomBackground) >= 4.5) return color
            color = ColorUtils.blendARGB(color, ink(context), 0.18f)
        }
        return color
    }
    fun shape(context: Context, color: Int, radius: Int = 20) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = if (AppConfig.isEInkMode) 0f else dp(context, radius).toFloat()
    }
    fun button(view: View, primary: Boolean = false) {
        val context = view.context
        val accent = accent(context)
        val color = if (primary) accent else ColorUtils.blendARGB(context.bottomBackground, accent, 0.09f)
        view.background = RippleDrawable(ColorStateList.valueOf(ColorUtils.setAlphaComponent(ink(context), 25)),
            shape(context, color, 16), shape(context, Color.WHITE, 16))
        if (view is TextView) view.setTextColor(if (primary) {
            if (ColorUtils.calculateContrast(Color.WHITE, color) >= 4.5) Color.WHITE else Color.BLACK
        } else accent)
    }
}
