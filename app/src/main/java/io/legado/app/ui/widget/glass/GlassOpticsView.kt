package io.legado.app.ui.widget.glass

import android.content.Context
import android.graphics.Canvas
import android.graphics.RenderNode
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi

/** An isolated background layer. Never apply this effect to the surface's foreground children. */
@RequiresApi(33)
internal class GlassOpticsView(context: Context) : View(context) {
    private val node = RenderNode("Refractive glass")
    private var target: GlassBackdropTarget? = null
    private var radius = 0f
    private var strength = 70
    private var tint = 0
    private var active = false
    private var listening = false
    private val targetLocation = IntArray(2)
    private val surfaceLocation = IntArray(2)
    private var lastGeometry: List<Float>? = null
    private var lastSnapshotVersion = -1L
    private val preDraw = ViewTreeObserver.OnPreDrawListener {
        updateGeometry()
        true
    }

    init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }

    // A background must not determine the height of a wrap_content toolbar or menu.
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(resolveSize(0, widthMeasureSpec), resolveSize(0, heightMeasureSpec))
    }

    fun configure(target: GlassBackdropTarget, radius: Float, strength: Int, tint: Int) {
        this.target = target
        this.radius = radius
        this.strength = strength
        this.tint = tint
        lastGeometry = null
        lastSnapshotVersion = -1L
        updateGeometry()
        invalidate()
    }

    fun setRenderingActive(active: Boolean) {
        this.active = active
        updateListening()
    }

    private fun updateListening() {
        val shouldListen = active && isAttachedToWindow && isShown && windowVisibility == VISIBLE
        if (shouldListen == listening) return
        if (shouldListen) viewTreeObserver.addOnPreDrawListener(preDraw)
        else if (viewTreeObserver.isAlive) viewTreeObserver.removeOnPreDrawListener(preDraw)
        listening = shouldListen
    }

    private fun updateGeometry() {
        val source = target ?: return
        if (width == 0 || height == 0 || source.width == 0 || source.height == 0) return
        source.getLocationOnScreen(targetLocation)
        getLocationOnScreen(surfaceLocation)
        val x = (surfaceLocation[0] - targetLocation[0]).toFloat()
        val y = (surfaceLocation[1] - targetLocation[1]).toFloat()
        val geometry = listOf(x, y, width.toFloat(), height.toFloat(), source.width.toFloat(), source.height.toFloat())
        val geometryChanged = lastGeometry != geometry
        if (!geometryChanged && lastSnapshotVersion == source.snapshotVersion) return
        if (geometryChanged) {
            lastGeometry = geometry
            node.setPosition(0, 0, source.width, source.height)
            node.translationX = -x
            node.translationY = -y
            node.setRenderEffect(GlassLensShader.effect(x, y, width.toFloat(), height.toFloat(), radius,
                resources.displayMetrics.density, strength))
        }
        // The first geometry pass can precede the backdrop's first draw. Re-record when
        // the backdrop changes, even when the bar stays still, so scroll content stays live.
        lastSnapshotVersion = source.snapshotVersion
        val recording = node.beginRecording()
        source.drawSnapshot(recording)
        node.endRecording()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated) return
        updateGeometry()
        canvas.drawRenderNode(node)
        canvas.drawColor(tint)
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); updateListening() }
    override fun onDetachedFromWindow() {
        setRenderingActive(false)
        node.discardDisplayList()
        lastGeometry = null
        lastSnapshotVersion = -1L
        super.onDetachedFromWindow()
    }
    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        updateListening()
    }
}
