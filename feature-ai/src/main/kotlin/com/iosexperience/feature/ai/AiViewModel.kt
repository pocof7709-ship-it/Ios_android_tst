package com.iosexperience.feature.ai

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iosexperience.core.data.Graph
import com.iosexperience.core.data.UiSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

object ProviderRegistry {
    val all: List<AIProvider> = listOf(LocalRulesProvider(), RemotePlaceholder)
    fun byId(id: String) = all.firstOrNull { it.id == id && it.implemented } ?: all.first()
}

/** Marcador honesto: aparece nas configurações, mas NÃO faz chamadas de rede nesta versão. */
object RemotePlaceholder : AIProvider {
    override val id = "remote"
    override val displayName = "Remoto (API) — não implementado"
    override val requiresApiKey = true
    override val implemented = false
    override val models = emptyList<String>()
    override suspend fun respond(prompt: String, history: List<AiMessage>) = AiReply("Provedor remoto não implementado nesta versão.")
}

class AiViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("ai", Context.MODE_PRIVATE)
    private var nextId = 1L
    private val _messages = MutableStateFlow(loadHistory())
    val messages: StateFlow<List<AiMessage>> = _messages
    val loading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    private var undo: UiSettings? = null
    val canUndo = MutableStateFlow(false)

    /** Limite simples de uso: 30 comandos por hora. */
    private fun withinLimit(): Boolean {
        val now = System.currentTimeMillis()
        val stamps = (prefs.getString("stamps", "") ?: "").split(",").mapNotNull { it.toLongOrNull() }.filter { now - it < 3_600_000 }
        if (stamps.size >= 30) return false
        prefs.edit().putString("stamps", (stamps + now).joinToString(",")).apply(); return true
    }

    fun send(text: String) {
        val s = Graph.settings.state.value
        if (!s.aiEnabled || text.isBlank() || loading.value) return
        if (!withinLimit()) { error.value = "Limite de 30 comandos por hora atingido."; return }
        error.value = null
        add(AiMessage(nextId++, true, text.trim()))
        loading.value = true
        viewModelScope.launch {
            try { val r = ProviderRegistry.byId(s.aiProvider).respond(text, _messages.value); add(AiMessage(nextId++, false, r.text, r.actions)) }
            catch (e: Exception) { error.value = "Falha ao processar o comando." }
            finally { loading.value = false }
        }
    }

    /** Chamado só após o toque do usuário em "Aplicar" (confirmação explícita). */
    fun apply(msgId: Long) {
        val m = _messages.value.firstOrNull { it.id == msgId } ?: return
        undo = Graph.settings.state.value; canUndo.value = true
        Graph.settings.update { s -> m.actions.fold(s) { acc, act -> act.applyTo(acc) } }
        _messages.value = _messages.value.map { if (it.id == msgId) it.copy(applied = true) else it }
        persist()
    }

    fun undoLast() { undo?.let { u -> Graph.settings.update { u } }; undo = null; canUndo.value = false }

    fun clearHistory() { _messages.value = emptyList(); prefs.edit().remove("history").apply() }

    private fun add(m: AiMessage) { _messages.value = (_messages.value + m).takeLast(60); persist() }

    private fun persist() {
        if (!Graph.settings.state.value.aiHistory) { prefs.edit().remove("history").apply(); return }
        val arr = JSONArray()
        _messages.value.forEach { arr.put(JSONObject().put("u", it.fromUser).put("t", it.text)) } // ações não são persistidas
        prefs.edit().putString("history", arr.toString()).apply()
    }

    private fun loadHistory(): List<AiMessage> {
        if (!Graph.settings.state.value.aiHistory) return emptyList()
        return try {
            val arr = JSONArray(prefs.getString("history", "[]"))
            (0 until arr.length()).map { i -> arr.getJSONObject(i).let { AiMessage(nextId++, it.getBoolean("u"), it.getString("t")) } }
        } catch (e: Exception) { emptyList() }
    }
}
