package com.iosexperience.feature.ai

import com.iosexperience.core.data.Perf
import com.iosexperience.core.data.ThemeMode
import com.iosexperience.core.data.UiSettings
import org.junit.Assert.*
import org.junit.Test

class LocalRulesProviderTest {
    private val p = LocalRulesProvider()

    @Test fun darkAndPurple() {
        val r = p.interpret("Crie um tema escuro elegante")
        val s = r.actions.fold(UiSettings(themeMode = ThemeMode.LIGHT)) { a, x -> x.applyTo(a) }
        assertEquals(ThemeMode.DARK, s.themeMode); assertEquals(3, s.accentIndex)
    }
    @Test fun economyDisablesAnimations() {
        val s = p.interpret("modo economia de bateria").actions.fold(UiSettings()) { a, x -> x.applyTo(a) }
        assertEquals(Perf.ECONOMIC, s.perf); assertFalse(s.animations)
    }
    @Test fun gridParsing() {
        val s = p.interpret("quero 5 colunas e 7 linhas").actions.fold(UiSettings()) { a, x -> x.applyTo(a) }
        assertEquals(5, s.gridColumns); assertEquals(7, s.gridRows)
    }
    @Test fun unsupportedIsHonest() {
        val r = p.interpret("organize meus apps em categorias")
        assertTrue(r.actions.isEmpty()); assertTrue(r.text.contains("ainda não existe"))
    }
    @Test fun unknownGivesHelp() = assertTrue(p.interpret("blablabla").actions.isEmpty())
}
