package io.legado.app.lib.theme.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.legado.app.databinding.ViewNavigationBadgeBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.Selector
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.ui.widget.glass.GlassMaterialSpec
import io.legado.app.ui.widget.text.BadgeView
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx
import androidx.core.graphics.ColorUtils as AndroidColorUtils

class ThemeBottomNavigationVIew(context: Context, attrs: AttributeSet) :
    BottomNavigationView(context, attrs) {

    init {
        val bgColor = context.bottomBackground
        backgroundTintList = null
        setBackgroundColor(Color.TRANSPARENT)
        elevation = 0f
        isItemHorizontalTranslationEnabled = false
        minimumHeight = (64f + (resources.configuration.fontScale - 1f).coerceAtLeast(0f) * 24f)
            .dpToPx().toInt()
        val textIsDark = ColorUtils.isColorLight(bgColor)
        val neutral = AndroidColorUtils.blendARGB(context.getPrimaryTextColor(textIsDark), bgColor, 0.18f)
        val textColor = GlassMaterialSpec.readableForeground(context, neutral)
        val accent = ThemeStore.accentColor(context)
        val selectionColor = AndroidColorUtils.blendARGB(
            bgColor,
            if (textIsDark) Color.BLACK else Color.WHITE,
            if (textIsDark) 0.07f else 0.10f
        )
        val selectionOpacity = 0.94f
        val colorStateList = Selector.colorBuild()
            .setDefaultColor(textColor)
            .setSelectedColor(
                GlassMaterialSpec.readableForegroundOnSelection(
                    context, accent, selectionColor, selectionOpacity
                )
            ).create()
        itemIconTintList = colorStateList
        itemTextColor = colorStateList
        // A navigation item is one unit: its checked surface includes both icon and label.
        isItemActiveIndicatorEnabled = false
        setItemTextAppearanceActiveBoldEnabled(false)
        itemRippleColor = null
        val inset = 4.dpToPx()
        fun capsule(color: Int) = GradientDrawable().apply {
            cornerRadius = 1000f.dpToPx()
            setColor(color)
        }
        val selected = capsule(
            if (AppConfig.isEInkMode) Color.WHITE
            else ColorUtils.withAlpha(selectionColor, selectionOpacity)
        ).apply {
            if (AppConfig.isEInkMode) setStroke(1.dpToPx(), Color.BLACK)
        }
        val states = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_checked), InsetDrawable(selected, inset))
            addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))
        }
        itemBackground = if (AppConfig.isEInkMode) states else RippleDrawable(
            ColorStateList.valueOf(ColorUtils.withAlpha(accent, 0.10f)), states,
            InsetDrawable(capsule(Color.WHITE), inset)
        )

        ViewCompat.setOnApplyWindowInsetsListener(this, null)
    }

    fun addBadgeView(index: Int): BadgeView {
        //获取底部菜单view
        val menuView = getChildAt(0) as ViewGroup
        //获取第index个itemView
        val itemView = menuView.getChildAt(index) as ViewGroup
        val badgeBinding = ViewNavigationBadgeBinding.inflate(LayoutInflater.from(context))
        itemView.addView(badgeBinding.root)
        return badgeBinding.viewBadge
    }

}
