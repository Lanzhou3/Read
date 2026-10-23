package io.legado.app.ui.book.read.config

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import io.legado.app.R
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.lib.theme.bottomBackground

/** A schematic guide; the actual book behind the panel is the precise preview. */
class ReadMarginPreview(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var section: Int = 0
        set(value) { field = value; invalidate() }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val density = resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val ink = ReadingPanelStyle.ink(context)
        val accent = ReadingPanelStyle.accent(context)
        val d = density
        paint.color = ReadingPanelStyle.tile(context)
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, 18 * d, 18 * d, paint)
        val values = ReadBookConfig.let {
            when (section) {
                1 -> listOf(it.headerPaddingTop, it.headerPaddingBottom, it.headerPaddingLeft, it.headerPaddingRight)
                2 -> listOf(it.footerPaddingTop, it.footerPaddingBottom, it.footerPaddingLeft, it.footerPaddingRight)
                else -> listOf(it.paddingTop, it.paddingBottom, it.paddingLeft, it.paddingRight)
            }
        }
        paint.color = ink
        paint.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        paint.textSize = 15 * resources.displayMetrics.scaledDensity
        canvas.drawText(context.getString(when (section) { 1 -> R.string.header; 2 -> R.string.footer; else -> R.string.main_body }), 18 * d, 34 * d, paint)
        paint.textSize = 11 * resources.displayMetrics.scaledDensity
        paint.color = ColorUtils.setAlphaComponent(ink, 180)
        canvas.drawText("${context.getString(R.string.padding_top)} ${values[0]}  ·  ${context.getString(R.string.padding_bottom)} ${values[1]}", 18 * d, 60 * d, paint)
        canvas.drawText("${context.getString(R.string.padding_left)} ${values[2]}  ·  ${context.getString(R.string.padding_right)} ${values[3]}", 18 * d, 82 * d, paint)
        val right = width - 18 * d
        val left = right - 96 * d
        rect.set(left, 10 * d, right, height - 10 * d)
        paint.color = context.bottomBackground
        canvas.drawRoundRect(rect, 8 * d, 8 * d, paint)
        val top = when (section) { 1 -> 15 * d; 2 -> height - 34 * d; else -> 29 * d }
        val bottom = when (section) { 1 -> 34 * d; 2 -> height - 15 * d; else -> height - 29 * d }
        rect.set(left + 5 * d, top, right - 5 * d, bottom)
        paint.color = ColorUtils.blendARGB(context.bottomBackground, accent, 0.18f)
        canvas.drawRoundRect(rect, 3 * d, 3 * d, paint)
        val insetLeft = (4 + values[2] * 0.14f).coerceAtMost(26f) * d
        val insetRight = (4 + values[3] * 0.14f).coerceAtMost(26f) * d
        val insetTop = (2 + values[0] * 0.06f).coerceAtMost(9f) * d
        val insetBottom = (2 + values[1] * 0.06f).coerceAtMost(9f) * d
        rect.set(rect.left + insetLeft, rect.top + insetTop, rect.right - insetRight, (rect.bottom - insetBottom).coerceAtLeast(rect.top + insetTop + d))
        paint.color = context.bottomBackground
        canvas.drawRoundRect(rect, 2 * d, 2 * d, paint)
        paint.strokeWidth = 1.6f * d
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = ColorUtils.setAlphaComponent(accent, 160)
        var y = rect.top + 3 * d
        while (y < rect.bottom - d) {
            canvas.drawLine(rect.left + 3 * d, y, rect.right - 3 * d, y, paint)
            y += 5 * d
        }
    }
}
