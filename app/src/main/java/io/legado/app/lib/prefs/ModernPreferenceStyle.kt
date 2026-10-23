package io.legado.app.lib.prefs

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceViewHolder
import io.legado.app.R
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.theme.accentColor
import io.legado.app.lib.theme.backgroundColor
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.utils.ColorUtils
import io.legado.app.utils.getCompatDrawable
import androidx.core.graphics.ColorUtils as AndroidColorUtils

/** Shared settings surfaces: semantic, vivid icons with quiet, readable controls. */
internal object ModernPreferenceStyle {
    private data class Palette(val ink: Int, val tile: Int, val nightInk: Int, val nightTile: Int)

    private val palettes = listOf(
        Palette(0xFF0866E8.toInt(), 0xFFE3EFFF.toInt(), 0xFFA7CBFF.toInt(), 0xFF203B5C.toInt()),
        Palette(0xFF00836B.toInt(), 0xFFDDF8EB.toInt(), 0xFF7BE7C5.toInt(), 0xFF173D36.toInt()),
        Palette(0xFFD64925.toInt(), 0xFFFFE6D8.toInt(), 0xFFFFB79A.toInt(), 0xFF493028.toInt()),
        Palette(0xFF7942D9.toInt(), 0xFFEEE3FF.toInt(), 0xFFD0B2FF.toInt(), 0xFF362A50.toInt()),
        Palette(0xFFC62F7D.toInt(), 0xFFFFE2EE.toInt(), 0xFFFFAAD1.toInt(), 0xFF482B40.toInt()),
        Palette(0xFF9A6500.toInt(), 0xFFFFF1BE.toInt(), 0xFFF7D66F.toInt(), 0xFF413B24.toInt()),
        Palette(0xFF007F92.toInt(), 0xFFD9F6FA.toInt(), 0xFF80E1EE.toInt(), 0xFF183D47.toInt())
    )

    private fun family(key: String?): Int = when (key) {
        "themeMode", "theme_setting", "themeList", "launcherIcon", "fontScale", "language" -> 3
        "bookmark", "colorAccent", "colorAccentNight", "saveDayTheme", "saveNightTheme" -> 4
        "web_dav_setting", "web_dav_backup", "web_dav_restore", "syncBookProgress", "syncBookProgressPlus", "auto_refresh" -> 1
        "txtTocRuleManage", "welcomeStyle", "coverConfig", "defaultToRead", "defaultCover", "defaultCoverDark", "exit", "cleanCache" -> 2
        "readRecord", "fileManage", "defaultBookTreeUri", "backupUri", "webDavDir", "webDavDeviceName" -> 5
        "glassEffects", "glassStrength", "webService", "userAgent", "web_dav_url", "Cronet" -> 6
        "bookSourceManage", "showDiscovery", "showRss", "defaultHomePage", "about" -> 0
        else -> (key?.hashCode()?.ushr(1) ?: 0) % palettes.size
    }

    private fun symbol(key: String?): Int = when (key) {
        "bookSourceManage", "defaultHomePage", "defaultToRead", "syncBookProgress", "syncBookProgressPlus" -> R.drawable.ic_bottom_books_e
        "txtTocRuleManage", "sourceEditMaxLine", "coverRule" -> R.drawable.ic_chapter_list
        "replaceManage", "replaceEnableDefault" -> R.drawable.ic_cfg_replace
        "dictRuleManage", "language", "process_text" -> R.drawable.ic_translate
        "themeMode", "theme_setting", "themeList", "saveDayTheme", "saveNightTheme", "colorPrimary", "colorAccent", "colorBackground", "colorBottomBackground", "colorPrimaryNight", "colorAccentNight", "colorBackgroundNight", "colorBottomBackgroundNight" -> R.drawable.ic_cfg_theme
        "launcherIcon", "showMangaUi" -> R.drawable.ic_view_quilt
        "welcomeStyle", "backgroundImage", "backgroundImageNight", "coverConfig", "defaultCover", "defaultCoverDark", "useDefaultCover", "bitmapCacheSize", "imageRetainNum", "coverShowName", "coverShowAuthor", "coverShowNameN", "coverShowAuthorN" -> R.drawable.ic_image
        "glassEffects", "glassStrength", "barElevation" -> R.drawable.ic_bubble_chart
        "fontScale" -> R.drawable.ic_interface_setting
        "transparentStatusBar", "immNavigationBar", "webServiceWakeLock" -> R.drawable.ic_brightness_auto
        "webService", "web_dav_url", "userAgent", "webPort", "Cronet", "loadCoverOnlyWifi" -> R.drawable.ic_network_check
        "web_dav_setting", "web_dav_backup", "onlyLatestBackup", "autoCheckNewBackup" -> R.drawable.ic_cfg_backup
        "web_dav_restore", "import_old", "restoreIgnore" -> R.drawable.ic_restore
        "web_dav_password", "localPassword" -> R.drawable.ic_lock_outline
        "web_dav_account", "webDavDeviceName" -> R.drawable.ic_author
        "fileManage", "defaultBookTreeUri", "backupUri", "webDavDir" -> R.drawable.ic_folder_outline
        "auto_refresh" -> R.drawable.ic_refresh_black_24dp
        "showDiscovery" -> R.drawable.ic_bottom_explore_e
        "showRss" -> R.drawable.ic_bottom_rss_feed_e
        "bookmark" -> R.drawable.ic_bookmark
        "readRecord" -> R.drawable.ic_history
        "about" -> R.drawable.ic_cfg_about
        "exit" -> R.drawable.ic_exit
        "checkSource" -> R.drawable.ic_check_source
        "uploadRule" -> R.drawable.ic_export
        "preDownloadNum" -> R.drawable.ic_download
        "cleanCache", "clearWebViewData", "autoClearExpired", "shrinkDatabase" -> R.drawable.ic_clear_all
        "mediaButtonOnExit", "readAloudByMediaButton", "ignoreAudioFocus" -> R.drawable.ic_read_aloud
        "recordLog", "recordHeapDump" -> R.drawable.ic_bug_report
        "contributors" -> R.drawable.ic_author
        "update_log", "license", "disclaimer" -> R.drawable.ic_chapter_list
        "check_update" -> R.drawable.ic_update
        "privacyPolicy" -> R.drawable.ic_lock_outline
        "crashLog", "saveLog", "createHeapDump" -> R.drawable.ic_bug_report
        else -> R.drawable.ic_cfg_other
    }

    fun bind(context: Context, holder: PreferenceViewHolder, icon: Drawable?, key: String?, hasWidget: Boolean) {
        val row = holder.itemView
        if (row.isInEditMode) return
        val ink = context.getPrimaryTextColor(ColorUtils.isColorLight(context.backgroundColor))
        val surface = if (AppConfig.isEInkMode) Color.WHITE else ColorUtils.withAlpha(
            AndroidColorUtils.blendARGB(context.backgroundColor, Color.WHITE,
                if (AppConfig.isNightTheme) 0.05f else 0.82f), 0.91f
        )
        val shape = rounded(context, surface, 18f)
        if (AppConfig.isEInkMode) shape.setStroke(context.dp(1), Color.BLACK)
        val rowBackground = if (AppConfig.isEInkMode) shape else RippleDrawable(
            ColorStateList.valueOf(ColorUtils.withAlpha(context.accentColor, 0.10f)), shape,
            rounded(context, Color.WHITE, 18f)
        )
        row.background = InsetDrawable(rowBackground, context.dp(16), context.dp(4), context.dp(16), context.dp(4))
        // InsetDrawable supplies padding of its own; retain breathing room for long summaries.
        row.setPaddingRelative(context.dp(28), context.dp(16), context.dp(28), context.dp(16))
        (holder.findViewById(R.id.preference_chevron) as? ImageView)?.apply {
            isVisible = !hasWidget && key != "exit"
            setColorFilter(ColorUtils.withAlpha(ink, 0.40f))
        }
        (holder.findViewById(R.id.preference_icon) as? ImageView)?.apply {
            val palette = palettes[family(key)]
            val foreground = if (AppConfig.isEInkMode) Color.BLACK else if (AppConfig.isNightTheme) palette.nightInk else palette.ink
            val tile = if (AppConfig.isEInkMode) Color.WHITE else if (AppConfig.isNightTheme) palette.nightTile else palette.tile
            isVisible = true
            setImageDrawable((icon?.constantState?.newDrawable() ?: context.getCompatDrawable(symbol(key)))?.mutate())
            setColorFilter(foreground)
            background = rounded(context, tile, 13f)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
    }

    fun bindValue(context: Context, value: TextView) {
        value.setTextColor(if (AppConfig.isEInkMode) Color.BLACK else context.accentColor)
        value.textSize = 12f
        value.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        value.maxWidth = context.dp(112)
        value.minHeight = context.dp(30)
        value.setPadding(context.dp(10), context.dp(5), context.dp(10), context.dp(5))
        value.background = rounded(context, if (AppConfig.isEInkMode) Color.WHITE
            else ColorUtils.withAlpha(context.accentColor, 0.09f), 10f)
    }

    fun bindSwitch(context: Context, switch: SwitchCompat) {
        val on = if (AppConfig.isEInkMode) Color.BLACK else context.accentColor
        val off = if (AppConfig.isNightTheme) 0xFF456074.toInt() else 0xFFC4D2E0.toInt()
        val thumb = rounded(context, Color.WHITE, 20f).apply { setSize(context.dp(22), context.dp(22)) }
        switch.thumbDrawable = InsetDrawable(thumb, context.dp(2))
        switch.trackDrawable = rounded(context, Color.WHITE, 20f).apply { setSize(context.dp(46), context.dp(26)) }
        switch.thumbTintList = ColorStateList.valueOf(Color.WHITE)
        switch.trackTintList = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(ColorUtils.withAlpha(off, 0.4f), on, off)
        )
        switch.switchMinWidth = context.dp(46)
        switch.splitTrack = false
        switch.minWidth = context.dp(48)
        switch.minHeight = context.dp(48)
    }

    private fun rounded(context: Context, color: Int, radius: Float) = GradientDrawable().apply {
        cornerRadius = if (AppConfig.isEInkMode) 0f else radius * context.resources.displayMetrics.density
        setColor(color)
    }

    private fun Context.dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
