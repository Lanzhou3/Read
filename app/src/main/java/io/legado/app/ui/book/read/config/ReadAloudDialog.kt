package io.legado.app.ui.book.read.config

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.constant.EventBus
import io.legado.app.databinding.DialogReadAloudBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.dialogs.selector
import io.legado.app.model.ReadAloud
import io.legado.app.model.ReadBook
import io.legado.app.service.BaseReadAloudService
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.utils.observeEvent
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.viewbindingdelegate.viewBinding

class ReadAloudDialog : BaseDialogFragment(R.layout.dialog_read_aloud) {
    private val callBack: CallBack? get() = activity as? CallBack
    private val binding by viewBinding(DialogReadAloudBinding::bind)

    override fun onStart() {
        super.onStart()
        ReadingPanelStyle.show(this, 0.72f)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? ReadBookActivity)?.let { it.bottomDialog-- }
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        val openDialogs = (activity as ReadBookActivity).bottomDialog++
        if (openDialogs > 0) {
            dismiss()
            return@run
        }
        ReadingPanelStyle.prepare(root) { dismissAllowingStateLoss() }
        listOf(tvPre, tvNext, timerPresets, timerSave, llCatalog, llMainMenu, llToBackstage, llSetting).forEach {
            ReadingPanelStyle.action(it)
        }
        listOf(ivPlayPrev, ivPlayNext, ivStop).forEach {
            ReadingPanelStyle.action(it)
            it.setColorFilter(ReadingPanelStyle.ink(requireContext()))
        }
        ReadingPanelStyle.action(ivPlayPause, true)
        cbTtsFollowSys.setTextColor(ReadingPanelStyle.ink(requireContext()))
        timerSlider.valueFormat = { if (it == 0) getString(R.string.ui_timer_off) else getString(R.string.timer_m, it) }
        timerSlider.progress = if (BaseReadAloudService.isRun) BaseReadAloudService.timeMinute.coerceAtLeast(0) else AppConfig.ttsTimer
        speechSlider.valueFormat = { getString(R.string.ui_playback_speed, ((it + 5) / 10f).toString()) }
        speechSlider.progress = AppConfig.ttsSpeechRate
        cbTtsFollowSys.isChecked = AppConfig.ttsFlowSys
        speechSlider.isEnabled = !cbTtsFollowSys.isChecked
        upPlayState()
        llMainMenu.setOnClickListener { dismissAllowingStateLoss(); callBack?.showMenuBar() }
        llSetting.setOnClickListener { ReadAloudConfigDialog().show(childFragmentManager, "readAloudConfigDialog") }
        tvPre.setOnClickListener { ReadBook.moveToPrevChapter(upContent = true, toLast = false) }
        tvNext.setOnClickListener { ReadBook.moveToNextChapter(true) }
        ivStop.setOnClickListener { ReadAloud.stop(requireContext()); dismissAllowingStateLoss() }
        ivPlayPause.setOnClickListener { callBack?.onClickReadAloud() }
        ivPlayPrev.setOnClickListener { ReadAloud.prevParagraph(requireContext()) }
        ivPlayNext.setOnClickListener { ReadAloud.nextParagraph(requireContext()) }
        llCatalog.setOnClickListener { dismissAllowingStateLoss(); callBack?.openChapterList() }
        llToBackstage.setOnClickListener { callBack?.finish() }
        cbTtsFollowSys.setOnCheckedChangeListener { _, checked ->
            AppConfig.ttsFlowSys = checked
            speechSlider.isEnabled = !checked
            updateSpeechRate()
        }
        speechSlider.onChanged = { AppConfig.ttsSpeechRate = it; updateSpeechRate() }
        timerSlider.onChanged = { value ->
            if (BaseReadAloudService.isRun) ReadAloud.setTimer(requireContext(), value)
            else AppConfig.ttsTimer = value
        }
        timerSave.setOnClickListener {
            AppConfig.ttsTimer = timerSlider.progress
            toastOnUi(getString(R.string.ui_save_timer))
        }
        timerPresets.setOnClickListener {
            val times = intArrayOf(0, 5, 10, 15, 30, 60, 90, 180)
            val labels = times.map { if (it == 0) getString(R.string.ui_timer_off) else getString(R.string.timer_m, it) }
            requireContext().selector(getString(R.string.ui_sleep_timer), labels) { _, index ->
                timerSlider.progress = times[index]
                timerSlider.onChanged?.invoke(times[index])
            }
        }
    }

    private fun upPlayState() {
        val playing = BaseReadAloudService.isRun && !BaseReadAloudService.pause
        binding.ivPlayPause.setImageResource(if (playing) R.drawable.ic_pause_24dp else R.drawable.ic_play_24dp)
        binding.ivPlayPause.contentDescription = getString(if (playing) R.string.pause else R.string.audio_play)
        binding.ivPlayPause.setColorFilter(ReadingPanelStyle.accent(requireContext()))
    }

    private fun updateSpeechRate() {
        ReadAloud.upTtsSpeechRate(requireContext())
        if (BaseReadAloudService.isRun && !BaseReadAloudService.pause) {
            ReadAloud.pause(requireContext())
            ReadAloud.resume(requireContext())
        }
    }

    override fun observeLiveBus() {
        observeEvent<Int>(EventBus.ALOUD_STATE) { upPlayState() }
        observeEvent<Int>(EventBus.READ_ALOUD_DS) { binding.timerSlider.progress = it.coerceAtLeast(0) }
    }

    interface CallBack {
        fun showMenuBar()
        fun openChapterList()
        fun onClickReadAloud()
        fun finish()
    }
}
