package io.legado.app.ui.book.read.config

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isVisible
import com.github.liuyueyi.quick.transfer.constants.TransType
import io.legado.app.R
import io.legado.app.help.BuiltInFonts
import io.legado.app.base.BaseDialogFragment
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.constant.EventBus
import io.legado.app.databinding.DialogReadBookStyleBinding
import io.legado.app.databinding.ItemReadStyleBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.lib.dialogs.selector
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.model.ReadBook
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.ui.font.FontSelectDialog
import io.legado.app.utils.ChineseUtils
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getIndexById
import io.legado.app.utils.postEvent
import io.legado.app.utils.viewbindingdelegate.viewBinding
import java.net.URLDecoder

class ReadStyleDialog : BaseDialogFragment(R.layout.dialog_read_book_style),
    FontSelectDialog.CallBack {

    private val binding by viewBinding(DialogReadBookStyleBinding::bind)
    private val callBack get() = activity as? ReadBookActivity
    private lateinit var styleAdapter: StyleAdapter
    private var savedTab = 0

    private fun panelHeight() = when (savedTab) { 1 -> 0.48f; 2 -> 0.43f; else -> 0.68f }

    override fun onStart() {
        super.onStart()
        ReadingPanelStyle.show(this, panelHeight())
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        (activity as ReadBookActivity).bottomDialog++
        savedTab = savedInstanceState?.getInt("appearanceTab") ?: 0
        initView()
        initData()
        initViewEvent()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("appearanceTab", savedTab)
    }

    private fun updateFontLabel() {
        binding.tvTextFontTitle.text = BuiltInFonts.displayName(
            requireContext(), ReadBookConfig.textFont
        ) ?: if (ReadBookConfig.textFont.isBlank()) {
            resources.getStringArray(R.array.system_typefaces).getOrElse(AppConfig.systemTypefaces) { getString(R.string.reading_font_default) }
        } else {
            runCatching { URLDecoder.decode(ReadBookConfig.textFont, "UTF-8") }.getOrDefault(ReadBookConfig.textFont)
                .substringAfterLast('/').substringBeforeLast('.')
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        ReadBookConfig.save()
        (activity as ReadBookActivity).bottomDialog--
    }

    private fun initView() = binding.run {
        val bg = requireContext().bottomBackground
        val isLight = ColorUtils.isColorLight(bg)
        val textColor = requireContext().getPrimaryTextColor(isLight)
        ReadingPanelStyle.prepare(root) { dismissAllowingStateLoss() }
        val tabs = listOf(tabType, tabPage, tabTheme)
        fun selectTab(index: Int) {
            savedTab = index
            ReadingPanelStyle.tabs(tabs, index)
            sectionType.isVisible = index == 0
            sectionPage.isVisible = index == 1
            sectionTheme.isVisible = index == 2
            panelScroll.scrollTo(0, 0)
            if (isResumed) ReadingPanelStyle.show(this@ReadStyleDialog, panelHeight())
        }
        tabs.forEachIndexed { index, tab -> tab.setOnClickListener { selectTab(index) } }
        selectTab(savedTab)
        listOf(tvTextFont, tvPadding, tvTip, shareLayoutRow).forEach { ReadingPanelStyle.action(it) }
        ReadingPanelStyle.icon(tvTextFontIcon, 0xFF7942D9.toInt())
        ReadingPanelStyle.icon(tvPaddingIcon, 0xFF00836B.toInt())
        ReadingPanelStyle.icon(tvTipIcon, 0xFFD64925.toInt())
        listOf(textFontWeightConverter, tvTextIndent, chineseConverter).forEach { ReadingPanelStyle.action(it) }
        rgPageAnim.children.filterIsInstance<TextView>().forEach { ReadingPanelStyle.radio(it) }
        shareLayoutRow.setOnClickListener { cbShareLayout.isChecked = !cbShareLayout.isChecked }
        io.legado.app.lib.prefs.ModernPreferenceStyle.bindSwitch(requireContext(), cbShareLayout)
        updateFontLabel()
        dsbTextSize.valueFormat = {
            (it + 5).toString()
        }
        dsbTextLetterSpacing.valueFormat = {
            ((it - 50) / 100f).toString()
        }
        dsbLineSize.valueFormat = { ((it - 10) / 10f).toString() }
        dsbParagraphSpacing.valueFormat = { (it / 10f).toString() }
        styleAdapter = StyleAdapter()
        rvStyle.adapter = styleAdapter
        styleAdapter.addFooterView {
            ItemReadStyleBinding.inflate(layoutInflater, it, false).apply {
                ivStyle.setPadding(6.dpToPx(), 6.dpToPx(), 6.dpToPx(), 6.dpToPx())
                styleName.setText(R.string.add)
                styleName.setTextColor(textColor)
                ivStyle.setText(null)
                ivStyle.setColorFilter(textColor)
                ivStyle.borderColor = textColor
                ivStyle.setImageResource(R.drawable.ic_add)
                root.setOnClickListener {
                    ReadBookConfig.configList.add(ReadBookConfig.Config())
                    showBgTextConfig(ReadBookConfig.configList.lastIndex)
                }
            }
        }
    }

    private fun initData() {
        binding.cbShareLayout.isChecked = ReadBookConfig.shareLayout
        upView()
        styleAdapter.setItems(ReadBookConfig.configList)
    }

    private fun initViewEvent() = binding.run {
        chineseConverter.onChanged {
            ChineseUtils.unLoad(*TransType.entries.toTypedArray())
            postEvent(EventBus.UP_CONFIG, arrayListOf(5))
        }
        textFontWeightConverter.onChanged {
            postEvent(EventBus.UP_CONFIG, arrayListOf(8, 9, 6))
        }
        tvTextFont.setOnClickListener {
            FontSelectDialog().show(childFragmentManager, "readingFont")
        }
        tvTextIndent.setOnClickListener {
            context?.selector(
                title = getString(R.string.text_indent),
                items = resources.getStringArray(R.array.indent).toList()
            ) { _, index ->
                ReadBookConfig.paragraphIndent = "　".repeat(index)
                postEvent(EventBus.UP_CONFIG, arrayListOf(8, 5))
            }
        }
        tvPadding.setOnClickListener {
            dismissAllowingStateLoss()
            callBack?.showPaddingConfig()
        }
        tvTip.setOnClickListener {
            TipConfigDialog().show(childFragmentManager, "tipConfigDialog")
        }
        rgPageAnim.setOnCheckedChangeListener { _, checkedId ->
            ReadBook.book?.setPageAnim(-1)
            ReadBookConfig.pageAnim = binding.rgPageAnim.getIndexById(checkedId)
            callBack?.upPageAnim()
            ReadBook.loadContent(false)
        }
        cbShareLayout.setOnCheckedChangeListener { _, isChecked ->
            ReadBookConfig.shareLayout = isChecked
            upView()
            postEvent(EventBus.UP_CONFIG, arrayListOf(1, 2, 5))
        }
        dsbTextSize.onChanged = {
            ReadBookConfig.textSize = it + 5
            postEvent(EventBus.UP_CONFIG, arrayListOf(8, 5))
        }
        dsbTextLetterSpacing.onChanged = {
            ReadBookConfig.letterSpacing = (it - 50) / 100f
            postEvent(EventBus.UP_CONFIG, arrayListOf(8, 5))
        }
        dsbLineSize.onChanged = {
            ReadBookConfig.lineSpacingExtra = it
            postEvent(EventBus.UP_CONFIG, arrayListOf(8, 5))
        }
        dsbParagraphSpacing.onChanged = {
            ReadBookConfig.paragraphSpacing = it
            postEvent(EventBus.UP_CONFIG, arrayListOf(8, 5))
        }
    }

    private fun changeBgTextConfig(index: Int) {
        val oldIndex = ReadBookConfig.styleSelect
        if (index != oldIndex) {
            ReadBookConfig.styleSelect = index
            upView()
            styleAdapter.notifyItemChanged(oldIndex)
            styleAdapter.notifyItemChanged(index)
            postEvent(EventBus.UP_CONFIG, arrayListOf(1, 2, 5))
            if (AppConfig.readBarStyleFollowPage) {
                postEvent(EventBus.UPDATE_READ_ACTION_BAR, true)
            }
        }
    }

    private fun showBgTextConfig(index: Int): Boolean {
        dismissAllowingStateLoss()
        changeBgTextConfig(index)
        callBack?.showBgTextConfig()
        return true
    }

    private fun upView() = binding.run {
        textFontWeightConverter.upUi(ReadBookConfig.textBold)
        ReadBook.pageAnim().let {
            if (it >= 0 && it < rgPageAnim.childCount) {
                rgPageAnim.check(rgPageAnim[it].id)
            }
        }
        ReadBookConfig.let {
            dsbTextSize.progress = it.textSize - 5
            dsbTextLetterSpacing.progress = (it.letterSpacing * 100).toInt() + 50
            dsbLineSize.progress = it.lineSpacingExtra
            dsbParagraphSpacing.progress = it.paragraphSpacing
        }
    }

    override val curFontPath: String
        get() = ReadBookConfig.textFont

    override fun selectFont(path: String) {
        if (path != ReadBookConfig.textFont || path.isEmpty()) {
            ReadBookConfig.textFont = path
            binding.root.post { if (isAdded) updateFontLabel() }
            postEvent(EventBus.UP_CONFIG, arrayListOf(2, 5))
        }
    }

    inner class StyleAdapter :
        RecyclerAdapter<ReadBookConfig.Config, ItemReadStyleBinding>(requireContext()) {

        override fun getViewBinding(parent: ViewGroup): ItemReadStyleBinding {
            return ItemReadStyleBinding.inflate(inflater, parent, false)
        }

        override fun convert(
            holder: ItemViewHolder,
            binding: ItemReadStyleBinding,
            item: ReadBookConfig.Config,
            payloads: MutableList<Any>
        ) {
            binding.apply {
                ivStyle.setText("Aa")
                styleName.text = item.name.ifBlank { getString(R.string.text) }
                styleName.setTextColor(ReadingPanelStyle.ink(context))
                root.contentDescription = styleName.text
                root.isSelected = ReadBookConfig.styleSelect == holder.layoutPosition
                ivStyle.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                ivStyle.setTextColor(item.curTextColor())
                ivStyle.setImageDrawable(item.curBgDrawable(100, 150))
                if (ReadBookConfig.styleSelect == holder.layoutPosition) {
                    ivStyle.borderColor = accentColor
                    ivStyle.setTextBold(true)
                } else {
                    ivStyle.borderColor = ColorUtils.withAlpha(item.curTextColor(), 0.18f)
                    ivStyle.setTextBold(false)
                }
            }
        }

        override fun registerListener(holder: ItemViewHolder, binding: ItemReadStyleBinding) {
            binding.apply {
                listOf(root, ivStyle).forEach { target ->
                    target.setOnClickListener { changeBgTextConfig(holder.layoutPosition) }
                    target.setOnLongClickListener { showBgTextConfig(holder.layoutPosition) }
                }
            }
        }

    }
}
