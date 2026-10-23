package io.legado.app.ui.widget.glass

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import eightbitlab.com.blurview.BlurTarget
import io.legado.app.R
import io.legado.app.lib.theme.backgroundColor
import io.legado.app.ui.widget.TitleBar
import java.util.WeakHashMap

/** Main-page content scrolls behind chrome while its first and last rows remain reachable. */
class GlassPageLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val originalPadding = WeakHashMap<View, Rect>()
    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener { updateContentInsets() }
    private var lifecycleOwner: LifecycleOwner? = null
    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) = setRenderingActive(true)
        override fun onPause(owner: LifecycleOwner) = setRenderingActive(false)
    }

    private fun setRenderingActive(active: Boolean) {
        findViewById<GlassSurfaceView>(R.id.title_glass)?.setRenderingActive(active)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        post {
            if (!isAttachedToWindow) return@post
            val content = findViewById<BlurTarget>(R.id.main_page_content)
            content.background = rootView.background?.constantState?.newDrawable()?.mutate()
                ?: ColorDrawable(context.backgroundColor)
            findViewById<TitleBar>(R.id.title_bar).bindGlassBackdrop(content)
            lifecycleOwner = findViewTreeLifecycleOwner()?.also {
                it.lifecycle.addObserver(lifecycleObserver)
                setRenderingActive(it.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
            }
            updateContentInsets()
        }
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        lifecycleOwner?.lifecycle?.removeObserver(lifecycleObserver)
        lifecycleOwner = null
        super.onDetachedFromWindow()
    }

    private fun updateContentInsets() {
        val target = findViewById<BlurTarget>(R.id.main_page_content) ?: return
        val header = findViewById<TitleBar>(R.id.title_bar) ?: return
        val navigation = rootView.findViewById<GlassSurfaceView>(R.id.bottom_glass)
        val gap = resources.getDimensionPixelSize(R.dimen.glass_content_gap)
        val navMargin = (navigation?.layoutParams as? MarginLayoutParams)?.bottomMargin ?: 0
        val systemBottom = ViewCompat.getRootWindowInsets(this)
            ?.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())?.bottom ?: 0
        val bottom = if (navigation?.visibility == VISIBLE) navigation.height + navMargin + gap else systemBottom + gap
        // Insets belong to the scrolling list. Padding its host clips rows before the glass.
        updateScrollViews(target, header.height + gap, bottom)
    }

    private fun updateScrollViews(view: View, top: Int, bottom: Int) {
        if (view is RecyclerView) {
            val firstAdjustment = !originalPadding.containsKey(view)
            val wasAtTop = !view.canScrollVertically(-1)
            val padding = originalPadding.getOrPut(view) {
                Rect(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)
            }
            if (view.paddingTop != padding.top + top || view.paddingBottom != padding.bottom + bottom) {
                view.clipToPadding = false
                view.setPadding(padding.left, padding.top + top, padding.right, padding.bottom + bottom)
                // A list already laid out without the overlay inset preserves its old anchor.
                // Keep an initially top-aligned page below the header instead.
                if (firstAdjustment && view.isLaidOut && wasAtTop) {
                    (view.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(0, 0)
                }
            }
        } else if (view is ViewGroup) {
            if (view is SwipeRefreshLayout) {
                val end = top + (64 * resources.displayMetrics.density).toInt()
                if (view.progressViewStartOffset != top) view.setProgressViewOffset(false, top, end)
            }
            view.children.forEach { updateScrollViews(it, top, bottom) }
        }
    }
}
