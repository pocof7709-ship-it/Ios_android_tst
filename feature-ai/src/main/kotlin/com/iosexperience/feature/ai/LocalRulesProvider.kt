package com.iosexperience.feature.ai

import com.iosexperience.core.data.Perf
import com.iosexperience.core.data.ThemeMode
import java.text.Normalizer

/**
 * Provedor 100% offline: interpreta comandos em português por regras (NÃO é um modelo de IA generativa).
 * Nada sai do aparelho. Serve também de contrato de teste para provedores futuros.
 */
class LocalRulesProvider : AIProvider {
    override val id = "local"
    override val displayName = "Local (regras offline)"
    override val requiresApiKey = false
    override val implemented = true
    override val models = listOf("regras-v1")

    private val colors = listOf("azul" to 0, "verde" to 1, "rosa" to 2, "roxo" to 3, "laranja" to 4, "vermelho" to 5, "turquesa" to 6, "cinza" to 7, "grafite" to 7)
    private val names = listOf("Azul", "Verde", "Rosa", "Roxo", "Laranja", "Vermelho", "Turquesa", "Grafite")

    private fun norm(s: String) = Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")

    override suspend fun respond(prompt: String, history: List<AiMessage>): AiReply = interpret(prompt)

    fun interpret(prompt: String): AiReply {
        val t = norm(prompt)
        val a = mutableListOf<AiAction>()
        val notes = mutableListOf<String>()

        if ("organiz" in t && ("categoria" in t || "pasta" in t)) notes += "Organizar apps em categorias automaticamente ainda não existe nesta versão; você pode criar pastas arrastando um ícone sobre outro."
        if ("widget" in t) notes += "Widgets são configurados na página “Hoje” (à esquerda da tela inicial); personalização por IA ainda não existe."

        if ("minimal" in t) { a += AiAction.Theme(ThemeMode.DARK); a += AiAction.IconSize(52); a += AiAction.IconCorner(30); a += AiAction.Accent(7, "Grafite"); a += AiAction.Animations(false) }
        if (Regex("\\bios\\b").containsMatchIn(t) || "iphone" in t) { a += AiAction.IconCorner(22); a += AiAction.IconSize(60); a += AiAction.Accent(0, "Azul"); a += AiAction.Grid(6, 4); a += AiAction.Animations(true) }
        if ("elegante" in t) { a += AiAction.Theme(ThemeMode.DARK); a += AiAction.Accent(3, "Roxo") }
        if ("escuro" in t || "dark" in t || "noite" in t) a += AiAction.Theme(ThemeMode.DARK)
        else if ("claro" in t) a += AiAction.Theme(ThemeMode.LIGHT)
        colors.firstOrNull { it.first in t }?.let { (_, i) -> a += AiAction.Accent(i, names[i]) }
        if ("econom" in t || "bateria" in t) { a += AiAction.Performance(Perf.ECONOMIC); a += AiAction.Animations(false) }
        else if ("avancad" in t || "fluid" in t || "desempenho maximo" in t) a += AiAction.Performance(Perf.ADVANCED)
        if (Regex("icones? (maiores|grandes)").containsMatchIn(t)) a += AiAction.IconSize(68)
        if (Regex("icones? (menores|pequenos)").containsMatchIn(t)) a += AiAction.IconSize(52)
        if ("arredondad" in t) a += AiAction.IconCorner(40)
        if ("quadrad" in t) a += AiAction.IconCorner(12)
        val cols = Regex("(\\d)\\s*colunas?").find(t)?.groupValues?.get(1)?.toInt()
        val rows = Regex("(\\d)\\s*linhas?").find(t)?.groupValues?.get(1)?.toInt()
        if (cols != null || rows != null) a += AiAction.Grid(rows, cols)

        val actions = a.distinctBy { it::class to (it as? AiAction.Accent)?.index }.let { list ->
            // Se houver conflito do mesmo tipo, vale a última regra citada.
            list.groupBy { it::class }.map { it.value.last() }
        }
        return when {
            actions.isNotEmpty() -> AiReply("Entendi. Posso aplicar:" + if (notes.isEmpty()) "" else "\n\n" + notes.joinToString("\n"), actions)
            notes.isNotEmpty() -> AiReply(notes.joinToString("\n"))
            else -> AiReply("Não entendi esse comando. Tente: “tema escuro com destaque roxo”, “deixe minimalista”, “ícones maiores”, “grade 5 colunas”, “modo economia de bateria”.")
        }
    }
}
