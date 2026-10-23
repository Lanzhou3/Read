package io.legado.app.ui.widget.glass

/** Keep expensive rendering out of compatibility, E Ink and power-saving modes. */
object GlassEffectPolicy {
    fun useHardwareBlur(
        sdk: Int,
        enabled: Boolean,
        eInk: Boolean,
        powerSave: Boolean
    ): Boolean = sdk >= 31 && enabled && !eInk && !powerSave

    fun useRefraction(sdk: Int, enabled: Boolean, eInk: Boolean, powerSave: Boolean): Boolean =
        sdk >= 33 && useHardwareBlur(sdk, enabled, eInk, powerSave)
}
