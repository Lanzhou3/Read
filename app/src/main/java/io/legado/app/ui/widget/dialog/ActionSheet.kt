package io.legado.app.ui.widget.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.legado.app.R
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.ui.widget.ModernUi

/** A small, scrollable task chooser; selecting an action closes it before navigation. */
object ActionSheet {
    data class Action(@StringRes val title: Int, @StringRes val description: Int,
                      @DrawableRes val icon: Int, val run: () -> Unit)

    fun show(context: Context, @StringRes title: Int, actions: List<Action>) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ModernUi.dp(context, 16), ModernUi.dp(context, 16), ModernUi.dp(context, 16), ModernUi.dp(context, 16))
            background = ModernUi.shape(context, context.bottomBackground, 28)
        }
        val header = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(context).apply {
            setText(title)
            textSize = 23f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(ModernUi.ink(context))
        }, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(TextView(context).apply {
            setText(R.string.cancel)
            textSize = 14f
            gravity = Gravity.CENTER
            ModernUi.button(this)
            setOnClickListener { dialog.dismiss() }
        }, LinearLayout.LayoutParams(ModernUi.dp(context, 64), ModernUi.dp(context, 48)))
        body.addView(header, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = ModernUi.dp(context, 16) })
        val rows = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        actions.forEach { action ->
            val row = LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                minimumHeight = ModernUi.dp(context, 76)
                setPadding(ModernUi.dp(context, 14), ModernUi.dp(context, 12), ModernUi.dp(context, 14), ModernUi.dp(context, 12))
                ModernUi.button(this)
                isFocusable = true
                setOnClickListener { dialog.dismiss(); action.run() }
            }
            row.addView(ImageView(context).apply {
                setImageResource(action.icon)
                setColorFilter(ModernUi.accent(context))
                importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }, LinearLayout.LayoutParams(ModernUi.dp(context, 28), ModernUi.dp(context, 28)))
            val labels = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            labels.addView(TextView(context).apply {
                setText(action.title)
                textSize = 16f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                setTextColor(ModernUi.ink(context))
            })
            labels.addView(TextView(context).apply {
                setText(action.description)
                textSize = 13f
                setTextColor(ColorUtils.setAlphaComponent(ModernUi.ink(context), 180))
            }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = ModernUi.dp(context, 4) })
            row.addView(labels, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = ModernUi.dp(context, 14) })
            rows.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = ModernUi.dp(context, 10) })
        }
        val scroll = ScrollView(context).apply { isFillViewport = false; addView(rows) }
        body.addView(scroll, LinearLayout.LayoutParams(-1, -2))
        dialog.setContentView(body)
        dialog.setCanceledOnTouchOutside(true)
        val owner = context as? LifecycleOwner
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) { dialog.dismiss() }
        }
        owner?.lifecycle?.addObserver(observer)
        dialog.setOnDismissListener { owner?.lifecycle?.removeObserver(observer) }
        ViewCompat.setOnApplyWindowInsetsListener(body) { view, insets ->
            val safe = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(ModernUi.dp(context, 16), ModernUi.dp(context, 16), ModernUi.dp(context, 16), ModernUi.dp(context, 16) + safe.bottom)
            insets
        }
        fun fitWindow() {
            val metrics = context.resources.displayMetrics
            dialog.window?.setLayout(
                (metrics.widthPixels - ModernUi.dp(context, 24)).coerceAtMost(ModernUi.dp(context, 560)),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            scroll.layoutParams = (scroll.layoutParams as LinearLayout.LayoutParams).apply { height = ViewGroup.LayoutParams.WRAP_CONTENT }
            body.post {
                val maximum = (metrics.heightPixels * 0.78f).toInt()
                if (body.height > maximum) {
                    scroll.layoutParams = (scroll.layoutParams as LinearLayout.LayoutParams).apply {
                        height = (maximum - header.height - body.paddingTop - body.paddingBottom - ModernUi.dp(context, 16))
                            .coerceAtLeast(ModernUi.dp(context, 48))
                    }
                }
            }
        }
        var screenWidth = context.resources.displayMetrics.widthPixels
        var screenHeight = context.resources.displayMetrics.heightPixels
        body.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val metrics = context.resources.displayMetrics
            if (screenWidth != metrics.widthPixels || screenHeight != metrics.heightPixels) {
                screenWidth = metrics.widthPixels
                screenHeight = metrics.heightPixels
                fitWindow()
            }
        }
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setDimAmount(0.25f)
            setGravity(Gravity.BOTTOM)
        }
        fitWindow()
        ViewCompat.requestApplyInsets(body)
    }
}
