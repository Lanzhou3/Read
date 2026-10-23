package io.legado.app.ui.widget

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu
import io.legado.app.R
import io.legado.app.databinding.ViewSelectActionBarBinding
import io.legado.app.lib.theme.TintHelper
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.elevation
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.lib.theme.getSecondaryDisabledTextColor
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.applyNavigationBarPadding
import io.legado.app.utils.visible


@Suppress("unused")
class SelectActionBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val bgIsLight = ColorUtils.isColorLight(context.bottomBackground)
    private val primaryTextColor = context.getPrimaryTextColor(bgIsLight)
    private val disabledColor = context.getSecondaryDisabledTextColor(bgIsLight)

    private var callBack: CallBack? = null
    private var selMenu: PopupMenu? = null
    private val binding = ViewSelectActionBarBinding
        .inflate(LayoutInflater.from(context), this, true)

    init {
        if (!isInEditMode) {
            background = ModernUi.shape(context, context.bottomBackground, 24)
            clipToOutline = true
            ModernUi.button(binding.btnRevertSelection)
            ModernUi.button(binding.btnSelectActionMain)
            elevation = context.elevation
            binding.cbSelectedAll.setTextColor(primaryTextColor)
            TintHelper.setTint(binding.cbSelectedAll, context.accentColor, !bgIsLight)
            binding.ivMenuMore.setColorFilter(disabledColor, PorterDuff.Mode.SRC_IN)
            binding.cbSelectedAll.setOnUserCheckedChangeListener { isChecked ->
                callBack?.selectAll(isChecked)
            }
            binding.btnRevertSelection.setOnClickListener { callBack?.revertSelection() }
            binding.btnSelectActionMain.setOnClickListener { callBack?.onClickSelectBarMainAction() }
            binding.ivMenuMore.setOnClickListener { selMenu?.show() }
            applyNavigationBarPadding()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
            val gap = ModernUi.dp(context, 12)
            params.marginStart = maxOf(params.marginStart, gap)
            params.marginEnd = maxOf(params.marginEnd, gap)
            params.bottomMargin = maxOf(params.bottomMargin, ModernUi.dp(context, 8))
            layoutParams = params
        }
    }

    fun setMainActionText(text: String) = binding.run {
        btnSelectActionMain.text = text
        btnSelectActionMain.visible()
    }

    fun setMainActionText(@StringRes id: Int) = binding.run {
        btnSelectActionMain.setText(id)
        btnSelectActionMain.visible()
    }

    fun inflateMenu(@MenuRes resId: Int): Menu? {
        selMenu = PopupMenu(context, binding.ivMenuMore)
        selMenu?.inflate(resId)
        binding.ivMenuMore.visible()
        return selMenu?.menu
    }

    fun setCallBack(callBack: CallBack) {
        this.callBack = callBack
    }

    fun setOnMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener) {
        selMenu?.setOnMenuItemClickListener(listener)
    }

    fun upCountView(selectCount: Int, allCount: Int) = binding.run {
        this@SelectActionBar.isVisible = allCount > 0
        if (selectCount == 0) {
            cbSelectedAll.isChecked = false
        } else {
            cbSelectedAll.isChecked = selectCount >= allCount
        }

        //重置全选的文字
        if (cbSelectedAll.isChecked) {
            cbSelectedAll.text = context.getString(
                R.string.select_cancel_count,
                selectCount,
                allCount
            )
        } else {
            cbSelectedAll.text = context.getString(
                R.string.select_all_count,
                selectCount,
                allCount
            )
        }
        setMenuClickable(selectCount > 0)
    }

    private fun setMenuClickable(isClickable: Boolean) = binding.run {
        btnRevertSelection.alpha = if (isClickable) 1f else 0.45f
        btnSelectActionMain.alpha = if (isClickable) 1f else 0.45f
        btnRevertSelection.isEnabled = isClickable
        btnRevertSelection.isClickable = isClickable
        btnSelectActionMain.isEnabled = isClickable
        btnSelectActionMain.isClickable = isClickable
        if (isClickable) {
            ivMenuMore.setColorFilter(primaryTextColor, PorterDuff.Mode.SRC_IN)
        } else {
            ivMenuMore.setColorFilter(disabledColor, PorterDuff.Mode.SRC_IN)
        }
        ivMenuMore.isEnabled = isClickable
        ivMenuMore.isClickable = isClickable
    }

    interface CallBack {

        fun selectAll(selectAll: Boolean)

        fun revertSelection()

        fun onClickSelectBarMainAction() {}
    }
}