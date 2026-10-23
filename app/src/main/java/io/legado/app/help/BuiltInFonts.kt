package io.legado.app.help

import android.content.Context
import android.graphics.Typeface
import io.legado.app.R
import splitties.init.appCtx

/** Stable references keep bundled fonts portable across installs and style backups. */
object BuiltInFonts {
    const val WENKAI = "builtin:lxgw-wenkai"
    const val WENKAI_LICENSE = "licenses/lxgw-wenkai.txt"

    private val wenKai by lazy {
        Typeface.createFromAsset(appCtx.assets, "font/LXGWWenKai-Regular.ttf")
    }

    fun contains(reference: String): Boolean = reference == WENKAI

    fun typeface(reference: String): Typeface? = when (reference) {
        WENKAI -> wenKai
        else -> null
    }

    fun displayName(context: Context, reference: String): String? = when (reference) {
        WENKAI -> context.getString(R.string.reading_font_wenkai)
        else -> null
    }
}
