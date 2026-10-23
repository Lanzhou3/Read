package io.legado.app.ui.book.read.config

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.constant.EventBus
import io.legado.app.databinding.DialogReadPaddingBinding
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.utils.postEvent
import io.legado.app.utils.viewbindingdelegate.viewBinding

class PaddingConfigDialog : BaseDialogFragment(R.layout.dialog_read_padding) {

    private val binding by viewBinding(DialogReadPaddingBinding::bind)

    private var section = 0

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("paddingSection", section)
    }

    override fun onStart() {
        super.onStart()
        ReadingPanelStyle.show(this, 0.72f)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        ReadingPanelStyle.prepare(view) { dismissAllowingStateLoss() }
        val tabs = listOf(binding.tabBody, binding.tabHeader, binding.tabFooter)
        fun selectSection(index: Int) {
            section = index
            ReadingPanelStyle.tabs(tabs, index)
            binding.sectionBody.isVisible = index == 0
            binding.sectionHeader.isVisible = index == 1
            binding.sectionFooter.isVisible = index == 2
            binding.marginPreview.section = index
            binding.panelScroll.scrollTo(0, 0)
        }
        tabs.forEachIndexed { index, tab -> tab.setOnClickListener { selectSection(index) } }
        selectSection(savedInstanceState?.getInt("paddingSection") ?: 0)
        io.legado.app.lib.prefs.ModernPreferenceStyle.bindSwitch(requireContext(), binding.cbShowTopLine)
        io.legado.app.lib.prefs.ModernPreferenceStyle.bindSwitch(requireContext(), binding.cbShowBottomLine)
        initData()
        initView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        ReadBookConfig.save()
    }

    private fun initData() = binding.run {
        //正文
        dsbPaddingTop.progress = ReadBookConfig.paddingTop
        dsbPaddingBottom.progress = ReadBookConfig.paddingBottom
        dsbPaddingLeft.progress = ReadBookConfig.paddingLeft
        dsbPaddingRight.progress = ReadBookConfig.paddingRight
        //页眉
        dsbHeaderPaddingTop.progress = ReadBookConfig.headerPaddingTop
        dsbHeaderPaddingBottom.progress = ReadBookConfig.headerPaddingBottom
        dsbHeaderPaddingLeft.progress = ReadBookConfig.headerPaddingLeft
        dsbHeaderPaddingRight.progress = ReadBookConfig.headerPaddingRight
        //页脚
        dsbFooterPaddingTop.progress = ReadBookConfig.footerPaddingTop
        dsbFooterPaddingBottom.progress = ReadBookConfig.footerPaddingBottom
        dsbFooterPaddingLeft.progress = ReadBookConfig.footerPaddingLeft
        dsbFooterPaddingRight.progress = ReadBookConfig.footerPaddingRight
        cbShowTopLine.isChecked = ReadBookConfig.showHeaderLine
        cbShowBottomLine.isChecked = ReadBookConfig.showFooterLine
    }

    private fun initView() = binding.run {
        //正文
        dsbPaddingTop.onChanged = {
            ReadBookConfig.paddingTop = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(10, 5))
        }
        dsbPaddingBottom.onChanged = {
            ReadBookConfig.paddingBottom = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(10, 5))
        }
        dsbPaddingLeft.onChanged = {
            ReadBookConfig.paddingLeft = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(10, 5))
        }
        dsbPaddingRight.onChanged = {
            ReadBookConfig.paddingRight = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(10, 5))
        }
        //页眉
        dsbHeaderPaddingTop.onChanged = {
            ReadBookConfig.headerPaddingTop = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(2))
        }
        dsbHeaderPaddingBottom.onChanged = {
            ReadBookConfig.headerPaddingBottom = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(2))
        }
        dsbHeaderPaddingLeft.onChanged = {
            ReadBookConfig.headerPaddingLeft = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(2))
        }
        dsbHeaderPaddingRight.onChanged = {
            ReadBookConfig.headerPaddingRight = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(2))
        }
        //页脚
        dsbFooterPaddingTop.onChanged = {
            ReadBookConfig.footerPaddingTop = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(2))
        }
        dsbFooterPaddingBottom.onChanged = {
            ReadBookConfig.footerPaddingBottom = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(2))
        }
        dsbFooterPaddingLeft.onChanged = {
            ReadBookConfig.footerPaddingLeft = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(2))
        }
        dsbFooterPaddingRight.onChanged = {
            ReadBookConfig.footerPaddingRight = it
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(2))
        }
        cbShowTopLine.setOnCheckedChangeListener { _, isChecked ->
            ReadBookConfig.showHeaderLine = isChecked
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(2))
        }
        cbShowBottomLine.setOnCheckedChangeListener { _, isChecked ->
            ReadBookConfig.showFooterLine = isChecked
            marginPreview.invalidate()
            postEvent(EventBus.UP_CONFIG, arrayListOf(2))
        }
    }

}
