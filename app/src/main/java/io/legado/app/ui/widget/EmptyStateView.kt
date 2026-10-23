package io.legado.app.ui.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ScrollView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import io.legado.app.R
import io.legado.app.lib.theme.bottomBackground

/** An empty page always explains the state and offers a useful next step. */
class EmptyStateView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ScrollView(context, attrs) {
    private val content = LinearLayout(context)
    private val icon = ImageView(context)
    private val title = TextView(context)
    private val message = TextView(context)
    private val action = TextView(context)
    private val secondary = TextView(context)

    init {
        isVerticalScrollBarEnabled = false
        content.orientation = LinearLayout.VERTICAL
        content.gravity = Gravity.CENTER
        addView(content, LayoutParams(-1, -2))
        val padding = ModernUi.dp(context, 24)
        content.setPadding(padding, padding, padding, padding)
        background = ModernUi.shape(context, context.bottomBackground, 28)
        clipToOutline = true
        icon.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        icon.setColorFilter(ModernUi.accent(context))
        content.addView(icon, LinearLayout.LayoutParams(ModernUi.dp(context, 44), ModernUi.dp(context, 44)))
        title.textSize = 20f
        title.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        title.setTextColor(ModernUi.ink(context))
        title.gravity = Gravity.CENTER
        content.addView(title, LinearLayout.LayoutParams(-1, -2).apply { topMargin = ModernUi.dp(context, 20) })
        message.textSize = 14f
        message.setTextColor(ModernUi.ink(context))
        message.alpha = 0.72f
        message.gravity = Gravity.CENTER
        message.setLineSpacing(ModernUi.dp(context, 3).toFloat(), 1f)
        content.addView(message, LinearLayout.LayoutParams(-1, -2).apply { topMargin = ModernUi.dp(context, 10) })
        listOf(action, secondary).forEachIndexed { index, text ->
            text.textSize = 15f
            text.gravity = Gravity.CENTER
            text.isClickable = true
            text.isFocusable = true
            text.id = if (index == 0) R.id.empty_state_action else R.id.empty_state_secondary
            text.minHeight = ModernUi.dp(context, 48)
            text.setPadding(ModernUi.dp(context, 12), ModernUi.dp(context, 8), ModernUi.dp(context, 12), ModernUi.dp(context, 8))
            ModernUi.button(text, index == 0)
            content.addView(text, LinearLayout.LayoutParams(-1, -2).apply { topMargin = ModernUi.dp(context, if (index == 0) 22 else 10) })
        }
    }

    fun render(@StringRes heading: Int, @StringRes description: Int,
               @StringRes primary: Int, @DrawableRes symbol: Int = R.drawable.ic_cfg_source,
               @StringRes alternative: Int = 0, onPrimary: () -> Unit, onAlternative: (() -> Unit)? = null) {
        title.setText(heading)
        message.setText(description)
        icon.setImageResource(symbol)
        action.setText(primary)
        action.setOnClickListener { onPrimary() }
        secondary.isVisible = alternative != 0 && onAlternative != null
        if (alternative != 0) secondary.setText(alternative)
        secondary.setOnClickListener { onAlternative?.invoke() }
    }
}
