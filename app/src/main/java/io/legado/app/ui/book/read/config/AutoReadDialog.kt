package io.legado.app.ui.book.read.config

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.databinding.DialogAutoReadBinding
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.model.ReadBook
import io.legado.app.ui.book.read.BaseReadBookActivity
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.utils.viewbindingdelegate.viewBinding
import java.util.Locale

class AutoReadDialog : BaseDialogFragment(R.layout.dialog_auto_read) {
    private val binding by viewBinding(DialogAutoReadBinding::bind)
    private val callBack: CallBack? get() = activity as? CallBack

    override fun onStart() {
        super.onStart()
        ReadingPanelStyle.show(this, 0.40f)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? ReadBookActivity)?.let { it.bottomDialog-- }
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        val openDialogs = (activity as ReadBookActivity).bottomDialog++
        if (openDialogs > 0) { dismiss(); return@run }
        ReadingPanelStyle.prepare(root) { dismissAllowingStateLoss() }
        listOf(llCatalog, llMainMenu, llAutoPageStop, llSetting).forEach { ReadingPanelStyle.action(it) }
        ReadingPanelStyle.action(llAutoPageStop, true)
        speedSlider.valueFormat = { String.format(Locale.ROOT, "%ds", it + 1) }
        speedSlider.progress = ReadBookConfig.autoReadSpeed.coerceIn(1, 120) - 1
        speedSlider.onChanged = { ReadBookConfig.autoReadSpeed = it + 1 }
        llMainMenu.setOnClickListener { dismissAllowingStateLoss(); callBack?.showMenuBar() }
        llSetting.setOnClickListener {
            (activity as BaseReadBookActivity).showPageAnimConfig {
                (activity as ReadBookActivity).upPageAnim()
                ReadBook.loadContent(false)
            }
        }
        llCatalog.setOnClickListener { dismissAllowingStateLoss(); callBack?.openChapterList() }
        llAutoPageStop.setOnClickListener { callBack?.autoPageStop(); dismissAllowingStateLoss() }
    }

    interface CallBack {
        fun showMenuBar()
        fun openChapterList()
        fun autoPageStop()
    }
}
