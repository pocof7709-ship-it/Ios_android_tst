package com.iosexperience.feature.ai

import com.iosexperience.core.data.Perf
import com.iosexperience.core.data.ThemeMode
import com.iosexperience.core.data.UiSettings

/** Ações estruturadas que a IA PROPÕE. Nunca são aplicadas sem o usuário tocar em "Aplicar". */
sealed interface AiAction {
    val label: String
    fun applyTo(s: UiSettings): UiSettings

    data class Theme(val mode: ThemeMode) : AiAction {
        override val label get() = "Tema " + when (mode) { ThemeMode.DARK -> "escuro"; ThemeMode.LIGHT -> "claro"; ThemeMode.SYSTEM -> "do sistema" }
        override fun applyTo(s: UiSettings) = s.copy(themeMode = mode)
    }
    data class Accent(val index: Int, val name: String) : AiAction {
        override val label get() = "Cor de destaque: $name"
        override fun applyTo(s: UiSettings) = s.copy(accentIndex = index)
    }
    data class IconCorner(val percent: Int) : AiAction {
        override val label get() = "Arredondamento dos ícones: $percent%"
        override fun applyTo(s: UiSettings) = s.copy(iconCorner = percent)
    }
    data class IconSize(val dp: Int) : AiAction {
        override val label get() = "Tamanho dos ícones: ${dp}dp"
        override fun applyTo(s: UiSettings) = s.copy(iconSizeDp = dp)
    }
    data class Grid(val rows: Int?, val cols: Int?) : AiAction {
        override val label get() = "Grade: ${rows ?: "—"} linhas × ${cols ?: "—"} colunas"
        override fun applyTo(s: UiSettings) = s.copy(gridRows = rows ?: s.gridRows, gridColumns = cols ?: s.gridColumns)
    }
    data class Performance(val perf: Perf) : AiAction {
        override val label get() = "Perfil de desempenho: " + when (perf) { Perf.ECONOMIC -> "Econômico"; Perf.BALANCED -> "Equilibrado"; Perf.ADVANCED -> "Visual avançado" }
        override fun applyTo(s: UiSettings) = s.copy(perf = perf)
    }
    data class Animations(val on: Boolean) : AiAction {
        override val label get() = if (on) "Ativar animações" else "Desativar animações"
        override fun applyTo(s: UiSettings) = s.copy(animations = on)
    }
}

data class AiMessage(val id: Long, val fromUser: Boolean, val text: String, val actions: List<AiAction> = emptyList(), val applied: Boolean = false)
data class AiReply(val text: String, val actions: List<AiAction> = emptyList())

/** Contrato independente de provedor. Implementações remotas ainda NÃO existem (ver docs/LIMITATIONS.md). */
interface AIProvider {
    val id: String
    val displayName: String
    val requiresApiKey: Boolean
    /** false = aparece na lista mas não pode ser usado nesta versão. */
    val implemented: Boolean
    val models: List<String>
    suspend fun respond(prompt: String, history: List<AiMessage>): AiReply
}
