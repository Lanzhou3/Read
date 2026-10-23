package io.legado.app

import io.legado.app.ui.widget.glass.GlassEffectPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassEffectPolicyTest {

    @Test
    fun hardwareBlurRequiresAndroid12() {
        assertFalse(GlassEffectPolicy.useHardwareBlur(30, true, false, false))
        assertTrue(GlassEffectPolicy.useHardwareBlur(31, true, false, false))
    }

    @Test
    fun userChoiceEInkAndPowerSavingDisableBackgroundWork() {
        assertFalse(GlassEffectPolicy.useHardwareBlur(36, false, false, false))
        assertFalse(GlassEffectPolicy.useHardwareBlur(36, true, true, false))
        assertFalse(GlassEffectPolicy.useHardwareBlur(36, true, false, true))
    }

    @Test
    fun refractionRequiresAndroid13AndHonorsRenderingRestrictions() {
        assertFalse(GlassEffectPolicy.useRefraction(32, true, false, false))
        assertTrue(GlassEffectPolicy.useRefraction(33, true, false, false))
        assertFalse(GlassEffectPolicy.useRefraction(37, false, false, false))
        assertFalse(GlassEffectPolicy.useRefraction(37, true, true, false))
        assertFalse(GlassEffectPolicy.useRefraction(37, true, false, true))
    }
}
