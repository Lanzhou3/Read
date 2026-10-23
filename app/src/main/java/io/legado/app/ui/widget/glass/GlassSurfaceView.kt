package io.legado.app.ui.widget.glass

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.PowerManager
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.annotation.RequiresApi
import eightbitlab.com.blurview.BlurTarget
import eightbitlab.com.blurview.BlurView
import io.legado.app.R
import io.legado.app.constant.PreferKey
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.backgroundColor
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.getPrefBoolean

/** A background-only effect: children retain sharp text, icons and normal touch semantics. */
class GlassSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var blurLayer: BlurView? = null
    private var opticsLayer: GlassOpticsView? = null
    private var edgeDrawable: Drawable? = null
    private var backdrop: BlurTarget? = null
    private var configuredBackdrop: BlurTarget? = null
    private var receiverRegistered = false
    private var renderingActive = true
    private var surfaceColor = context.bottomBackground
    private val radius: Float
    private val surfaceElevation: Float
    private val edgeOpacity: Float
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = refreshEffect()
    }

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.GlassSurfaceView)
        val configuredRadius = attributes.getDimension(
            R.styleable.GlassSurfaceView_glassCornerRadius,
            resources.getDimension(R.dimen.glass_toolbar_radius)
        )
        radius = if (AppConfig.isEInkMode) 0f else configuredRadius
        surfaceElevation = attributes.getDimension(
            R.styleable.GlassSurfaceView_glassElevation,
            3f * resources.displayMetrics.density
        )
        edgeOpacity = attributes.getFloat(R.styleable.GlassSurfaceView_glassEdgeOpacity, 1f).coerceIn(0f, 1f)
        attributes.recycle()
        clipToOutline = true
        blurLayer = BlurView(context).also {
            it.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
            addView(it, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }
        if (Build.VERSION.SDK_INT >= 33) {
            // Compile once up front, so a device shader-driver failure can safely use blur.
            try {
                GlassLensShader.effect(0f, 0f, 1f, 1f, 0f, 1f, 70)
                opticsLayer = GlassOpticsView(context).also {
                    addView(it, 1, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                }
            } catch (error: IllegalArgumentException) {
                Log.w("LiquidGlass", "AGSL unavailable; using hardware blur", error)
            }
        }
        edgeDrawable = GlassEdgeDrawable(radius, resources.displayMetrics.density, edgeOpacity)
        refreshEffect()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        edgeDrawable?.setBounds(0, 0, w, h)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        edgeDrawable?.draw(canvas)
    }

    /** The target must not contain this surface; otherwise a recursive snapshot is created. */
    fun bindBackdrop(target: BlurTarget) {
        require(!isAncestor(target)) { "A glass surface must be outside its own BlurTarget" }
        backdrop = target
        refreshEffect()
    }

    fun setSurfaceColor(color: Int) {
        surfaceColor = color
        refreshEffect()
    }

    fun setRenderingActive(active: Boolean) {
        renderingActive = active
        updateBackgroundWork()
    }

    private fun isAncestor(target: BlurTarget): Boolean {
        var current = parent
        while (current != null) {
            if (current === target) return true
            current = current.parent
        }
        return false
    }

    private fun refreshEffect() {
        val layer = blurLayer ?: return
        val enabled = GlassEffectPolicy.useHardwareBlur(
            Build.VERSION.SDK_INT,
            context.getPrefBoolean(PreferKey.glassEffects, true),
            AppConfig.isEInkMode,
            powerManager.isPowerSaveMode
        ) && backdrop != null && isHardwareAccelerated
        background = GradientDrawable().apply {
            cornerRadius = radius
            setColor(if (AppConfig.isEInkMode) Color.WHITE else surfaceColor)
        }
        elevation = if (AppConfig.isEInkMode) 0f else surfaceElevation
        val useOptics = Build.VERSION.SDK_INT >= 33 && enabled && opticsLayer != null &&
            backdrop is GlassBackdropTarget && GlassEffectPolicy.useRefraction(
                Build.VERSION.SDK_INT, true, AppConfig.isEInkMode, powerManager.isPowerSaveMode)
        layer.visibility = if (enabled && !useOptics) VISIBLE else GONE
        layer.setBlurEnabled(enabled && !useOptics)
        opticsLayer?.visibility = if (useOptics) VISIBLE else GONE
        if (useOptics && Build.VERSION.SDK_INT >= 33) {
            opticsLayer?.configure(backdrop as GlassBackdropTarget, radius, GlassMaterialSpec.strength(context),
                ColorUtils.withAlpha(surfaceColor, GlassMaterialSpec.tintOpacity(context, surfaceColor)))
        } else if (enabled) {
            val target = backdrop!!
            if (configuredBackdrop !== target) {
                layer.setupWith(target, 3f, true)
                    .setFrameClearDrawable(
                        target.background?.constantState?.newDrawable()?.mutate()
                            ?: ColorDrawable(context.backgroundColor)
                    )
                configuredBackdrop = target
            }
            val strength = GlassMaterialSpec.strength(context)
            layer.setBlurRadius(8f + strength * 0.08f)
            layer.setOverlayColor(ColorUtils.withAlpha(surfaceColor, GlassMaterialSpec.tintOpacity(context, surfaceColor)))
        }
        updateBackgroundWork()
    }

    private fun updateBackgroundWork() {
        blurLayer?.let {
            it.setBlurAutoUpdate(renderingActive && it.visibility == VISIBLE && isShown && windowVisibility == VISIBLE)
        }
        if (Build.VERSION.SDK_INT >= 33) opticsLayer?.let {
            it.setRenderingActive(renderingActive && it.visibility == VISIBLE && isShown && windowVisibility == VISIBLE)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ContextCompat.registerReceiver(
            context, powerReceiver, IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        receiverRegistered = true
        refreshEffect()
    }

    override fun onDetachedFromWindow() {
        blurLayer?.setBlurAutoUpdate(false)
        if (Build.VERSION.SDK_INT >= 33) opticsLayer?.setRenderingActive(false)
        if (receiverRegistered) {
            context.unregisterReceiver(powerReceiver)
            receiverRegistered = false
        }
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        updateBackgroundWork()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        updateBackgroundWork()
    }

    private class GlassEdgeDrawable(
        private val radius: Float,
        private val density: Float,
        private val opacity: Float
    ) : Drawable() {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = density
        }
        private val rect = RectF()

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            rect.set(bounds)
            rect.inset(density / 2f, density / 2f)
            paint.shader = if (AppConfig.isEInkMode) null else LinearGradient(
                0f, 0f, rect.width(), rect.height(),
                intArrayOf(0xA6FFFFFF.toInt(), 0x18FFFFFF, 0x18000000),
                null, Shader.TileMode.CLAMP
            )
            paint.color = if (AppConfig.isEInkMode) Color.BLACK else Color.WHITE
            paint.alpha = if (AppConfig.isEInkMode) 255 else (255 * opacity).toInt()
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(rect, radius, radius, paint)
        }

        override fun setAlpha(alpha: Int) { paint.alpha = alpha }
        override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
        @Deprecated("Deprecated in Java")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
}
