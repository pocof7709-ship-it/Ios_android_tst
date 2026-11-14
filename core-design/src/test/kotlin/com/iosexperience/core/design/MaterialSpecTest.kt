package com.iosexperience.core.design

import com.iosexperience.core.design.theme.MaterialLevel
import com.iosexperience.core.design.theme.PerformanceProfile
import com.iosexperience.core.design.theme.materialSpec
import org.junit.Assert.*
import org.junit.Test

class MaterialSpecTest {
    @Test fun tintTableMatchesSpec() {
        assertEquals(0.35f, materialSpec(MaterialLevel.Thin, false, PerformanceProfile.BALANCED).tintAlpha, 0f)
        assertEquals(0.30f, materialSpec(MaterialLevel.Thin, true, PerformanceProfile.BALANCED).tintAlpha, 0f)
        assertEquals(0.55f, materialSpec(MaterialLevel.Regular, false, PerformanceProfile.ADVANCED).tintAlpha, 0f)
        assertEquals(0.45f, materialSpec(MaterialLevel.Regular, true, PerformanceProfile.ADVANCED).tintAlpha, 0f)
        assertEquals(0.75f, materialSpec(MaterialLevel.Thick, false, PerformanceProfile.BALANCED).tintAlpha, 0f)
        assertEquals(0.65f, materialSpec(MaterialLevel.Thick, true, PerformanceProfile.BALANCED).tintAlpha, 0f)
    }
    @Test fun saturationIs180OutsideEconomic() = assertEquals(1.8f, materialSpec(MaterialLevel.Regular, true, PerformanceProfile.BALANCED).saturation, 0f)
    @Test fun economicHasNoBackdropNoShadowAndMoreOpaque() {
        val e = materialSpec(MaterialLevel.Thick, false, PerformanceProfile.ECONOMIC)
        assertFalse(e.backdrop); assertFalse(e.shadow); assertEquals(0.95f, e.tintAlpha, 0f); assertEquals(1f, e.saturation, 0f)
    }
    @Test fun economicNeverLessOpaqueThanOthers() {
        MaterialLevel.values().forEach { l ->
            assertTrue(materialSpec(l, true, PerformanceProfile.ECONOMIC).tintAlpha > materialSpec(l, true, PerformanceProfile.BALANCED).tintAlpha)
        }
    }
}
