package io.legado.app.ui.widget.glass

import android.content.Context
import android.graphics.Canvas
import android.graphics.RenderNode
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import eightbitlab.com.blurview.BlurTarget

/** Public display-list snapshot; no reflection into BlurView and no CPU bitmap readback. */
class GlassBackdropTarget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    BlurTarget(context, attrs) {

    private val snapshot = if (Build.VERSION.SDK_INT >= 33) Snapshot() else null

    internal val snapshotVersion: Long
        get() = if (Build.VERSION.SDK_INT >= 33) snapshot?.version ?: 0L else 0L

    @RequiresApi(33)
    fun drawSnapshot(canvas: Canvas) {
        snapshot?.node?.let(canvas::drawRenderNode)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val capture = snapshot
        if (Build.VERSION.SDK_INT >= 33 && canvas.isHardwareAccelerated && capture != null) {
            capture.node.setPosition(0, 0, width, height)
            val recording = capture.node.beginRecording()
            // BlurTarget records children only. Include the page's actual background as well.
            background?.draw(recording)
            super.dispatchDraw(recording)
            capture.node.endRecording()
            capture.version++
            canvas.drawRenderNode(capture.node)
        } else {
            super.dispatchDraw(canvas)
        }
    }

    @RequiresApi(33)
    private class Snapshot {
        val node = RenderNode("Glass background")
        var version = 0L
    }
}
