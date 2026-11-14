package com.iosexperience.core.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Persistência da tela inicial, dock, recentes do launcher e atalhos da central de controle. */
class LayoutStore(ctx: Context) {
    private val p = ctx.getSharedPreferences("layout", Context.MODE_PRIVATE)

    fun loadEntries(): List<HomeEntry> = try {
        val arr = JSONArray(p.getString("entries", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            if (o.getString("t") == "f") {
                val ks = o.getJSONArray("k"); HomeEntry.Folder(o.getString("id"), o.getString("n"), (0 until ks.length()).map { ks.getString(it) })
            } else HomeEntry.App(o.getString("k"))
        }
    } catch (e: Exception) { emptyList() }

    fun saveEntries(list: List<HomeEntry>) {
        val arr = JSONArray()
        list.forEach { e ->
            arr.put(when (e) {
                is HomeEntry.App -> JSONObject().put("t", "a").put("k", e.key)
                is HomeEntry.Folder -> JSONObject().put("t", "f").put("id", e.id).put("n", e.name).put("k", JSONArray(e.keys))
            })
        }
        p.edit().putString("entries", arr.toString()).apply()
    }

    private fun list(key: String, def: List<String> = emptyList()): List<String> =
        p.getString(key, null)?.split("\n")?.filter { it.isNotBlank() } ?: def
    private fun save(key: String, v: List<String>) = p.edit().putString(key, v.joinToString("\n")).apply()

    fun loadDock() = list("dock"); fun saveDock(v: List<String>) = save("dock", v)
    fun loadRecents() = list("recents"); fun saveRecents(v: List<String>) = save("recents", v.take(12))
    /** "builtin:torch|timer|calc|camera" ou chave de app. */
    fun loadShortcuts() = list("shortcuts", listOf("builtin:torch", "builtin:timer", "builtin:calc", "builtin:camera"))
    fun saveShortcuts(v: List<String>) = save("shortcuts", v)
    fun resetHome() = p.edit().remove("entries").remove("dock").apply()
}
