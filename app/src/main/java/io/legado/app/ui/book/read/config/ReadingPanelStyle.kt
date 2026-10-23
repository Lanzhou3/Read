package io.legado.app.ui.book.read.config

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.children
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import io.legado.app.R
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.getPrimaryTextColor

/** Reading controls stay opaque and calm so the book remains the visual preview. */
internal object ReadingPanelStyle {
    fun ink(context: Context): Int = context.getPrimaryTextColor(
        ColorUtils.calculateLuminance(context.bottomBackground) > 0.5
    )

    fun accent(context: Context): Int {
        if (AppConfig.isEInkMode) return ink(context)
        var color = context.accentColor
        val light = ColorUtils.calculateLuminance(context.bottomBackground) > 0.5
        repeat(8) {
            if (ColorUtils.calculateContrast(color, context.bottomBackground) >= 4.5) return color
            color = ColorUtils.blendARGB(color, if (light) Color.BLACK else Color.WHITE, 0.15f)
        }
        return color
    }

    fun shape(context: Context, color: Int, radius: Float = 16f) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = if (AppConfig.isEInkMode) 0f else radius * context.resources.displayMetrics.density
    }

    fun tile(context: Context): Int = ColorUtils.blendARGB(context.bottomBackground, ink(context), 0.045f)

    fun action(view: View, vivid: Boolean = false) {
        val context = view.context
        val color = if (vivid) ColorUtils.blendARGB(context.bottomBackground, accent(context), 0.10f)
            else tile(context)
        val bg = shape(context, color)
        view.background = RippleDrawable(ColorStateList.valueOf(
            ColorUtils.setAlphaComponent(accent(context), 35)), bg, shape(context, Color.WHITE))
        (view as? TextView)?.setTextColor(if (vivid) accent(context) else ink(context))
    }

    fun prepare(root: View, onDone: () -> Unit) {
        val context = root.context
        root.background = shape(context, context.bottomBackground, 30f)
        root.clipToOutline = true
        root.findViewById<View>(R.id.panel_scroll)?.let { scroll ->
            val bottomPadding = scroll.paddingBottom
            ViewCompat.setOnApplyWindowInsetsListener(scroll) { target, insets ->
                val windowInsets = ViewCompat.getRootWindowInsets(root) ?: insets
                val safe = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()
                    or WindowInsetsCompat.Type.systemGestures() or WindowInsetsCompat.Type.displayCutout())
                val bottom = bottomPadding + safe.bottom
                if (target.paddingBottom != bottom) {
                    target.setPadding(target.paddingLeft, target.paddingTop, target.paddingRight, bottom)
                }
                insets
            }
        }
        fun visit(view: View) {
            if (view is TextView) {
                view.setTextColor(if (view.id == R.id.tv_seek_value) accent(context) else ink(context))
                if (view.textSize / context.resources.displayMetrics.scaledDensity <= 12f) view.alpha = 0.66f
            }
            if (view is ViewGroup) view.children.forEach(::visit)
        }
        visit(root)
        root.findViewById<TextView>(R.id.panel_done)?.let {
            action(it, true)
            it.setOnClickListener { onDone() }
        }
        root.findViewById<View>(R.id.panel_tabs)?.background = shape(context, tile(context), 18f)
    }

    fun tabs(tabs: List<TextView>, selected: Int) {
        tabs.forEachIndexed { index, tab ->
            tab.isSelected = index == selected
            tab.background = shape(tab.context,
                if (index == selected) ColorUtils.blendARGB(tab.context.bottomBackground, accent(tab.context), 0.12f)
                else Color.TRANSPARENT, 14f)
            tab.setTextColor(if (index == selected) accent(tab.context) else ink(tab.context))
            tab.typeface = android.graphics.Typeface.create("sans-serif", if (index == selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }
    }

    fun icon(view: ImageView, color: Int) {
        var iconInk = color
        repeat(8) {
            if (ColorUtils.calculateContrast(iconInk, view.context.bottomBackground) < 3f) {
                iconInk = ColorUtils.blendARGB(iconInk, ink(view.context), 0.2f)
            }
        }
        view.setColorFilter(iconInk)
        view.background = shape(view.context, ColorUtils.blendARGB(view.context.bottomBackground, iconInk, 0.12f), 13f)
    }

    fun radio(view: TextView) {
        val context = view.context
        view.background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_checked), shape(context,
                ColorUtils.blendARGB(context.bottomBackground, accent(context), 0.12f), 12f))
            addState(intArrayOf(), shape(context, tile(context), 12f))
        }
        view.setTextColor(ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(accent(context), ink(context))))
    }

    fun show(dialog: DialogFragment, fraction: Float = 0.68f) {
        dialog.dialog?.window?.run {
            if (decorView.getTag(R.id.reading_panel_height) == null) {
                var orientation = dialog.resources.configuration.orientation
                decorView.addOnLayoutChangeListener { decor, _, _, _, _, _, _, _, _ ->
                    val next = dialog.resources.configuration.orientation
                    if (next != orientation) {
                        orientation = next
                        decor.post {
                            if (dialog.isAdded && dialog.dialog?.isShowing == true) {
                                show(dialog, decor.getTag(R.id.reading_panel_height) as Float)
                            }
                        }
                    }
                }
            }
            decorView.setTag(R.id.reading_panel_height, fraction)
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setBackgroundDrawableResource(android.R.color.transparent)
            decorView.setPadding(0, 0, 0, 0)
            attributes = attributes.apply { gravity = Gravity.BOTTOM; dimAmount = 0f }
            val metrics = dialog.resources.displayMetrics
            val landscape = metrics.widthPixels > metrics.heightPixels
            val width = if (landscape || metrics.widthPixels / metrics.density > 600) (560 * metrics.density).toInt()
                .coerceAtMost(metrics.widthPixels) else ViewGroup.LayoutParams.MATCH_PARENT
            setLayout(width, (metrics.heightPixels * if (landscape) 0.92f else fraction).toInt())
            dialog.view?.let { ViewCompat.requestApplyInsets(it) }
        }
    }
}
