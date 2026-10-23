package io.legado.app.ui.widget.image

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable

/** Stable color families for built-in placeholders; real and user-supplied covers are untouched. */
internal class ModernBookCoverDrawable : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var key: Triple<Int, Boolean, Boolean>? = null
    private var gradient: LinearGradient? = null
    private var drawableAlpha = 255

    fun bind(name: String?, night: Boolean, eInk: Boolean) {
        val next = Triple((name.orEmpty().hashCode() and Int.MAX_VALUE) % DAY.size, night, eInk)
        if (next == key) return
        key = next
        rebuildGradient()
    }

    override fun onBoundsChange(bounds: Rect) { rebuildGradient() }

    private fun rebuildGradient() {
        val (index, night, eInk) = key ?: return
        val colors = if (eInk) intArrayOf(Color.WHITE, Color.WHITE) else (if (night) NIGHT else DAY)[index]
        gradient = LinearGradient(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat(),
            colors, null, Shader.TileMode.CLAMP)
    }

    override fun draw(canvas: Canvas) {
        val (_, night, eInk) = key ?: return
        // Decorations leave a translucent paint color behind. Reset it before every fill.
        paint.color = Color.WHITE
        paint.alpha = drawableAlpha
        paint.shader = gradient
        canvas.drawRect(bounds, paint)
        paint.shader = null
        if (!eInk) {
            paint.color = if (night) 0x0DFFFFFF else 0x55FFFFFF
            paint.alpha = Color.alpha(paint.color) * drawableAlpha / 255
            canvas.drawCircle(bounds.width() * 0.9f, bounds.height() * 0.72f, bounds.width() * 0.63f, paint)
            paint.color = if (night) 0x22000000 else 0x120B3761
            paint.alpha = Color.alpha(paint.color) * drawableAlpha / 255
            canvas.drawRect(0f, 0f, bounds.width() * 0.045f, bounds.height().toFloat(), paint)
        }
    }

    override fun setAlpha(alpha: Int) { drawableAlpha = alpha.coerceIn(0, 255); invalidateSelf() }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = if (drawableAlpha == 255) PixelFormat.OPAQUE else PixelFormat.TRANSLUCENT

    companion object {
        private val DAY = arrayOf(
            intArrayOf(0xFF90C8FF.toInt(), 0xFFC5ECFF.toInt()),
            intArrayOf(0xFF85DEC9.toInt(), 0xFFD0F5DE.toInt()),
            intArrayOf(0xFFFFAE9C.toInt(), 0xFFFFDDC6.toInt()),
            intArrayOf(0xFFB5A6F4.toInt(), 0xFFE5DCFF.toInt()),
            intArrayOf(0xFFF5CC75.toInt(), 0xFFFFEBC5.toInt()),
            intArrayOf(0xFF8AD7EA.toInt(), 0xFFD4F3FF.toInt())
        )
        private val NIGHT = arrayOf(
            intArrayOf(0xFF174674.toInt(), 0xFF286A99.toInt()),
            intArrayOf(0xFF15554F.toInt(), 0xFF26766A.toInt()),
            intArrayOf(0xFF673C4A.toInt(), 0xFF905659.toInt()),
            intArrayOf(0xFF40366D.toInt(), 0xFF66538F.toInt()),
            intArrayOf(0xFF604C30.toInt(), 0xFF806945.toInt()),
            intArrayOf(0xFF194A62.toInt(), 0xFF28768B.toInt())
        )
    }
}
