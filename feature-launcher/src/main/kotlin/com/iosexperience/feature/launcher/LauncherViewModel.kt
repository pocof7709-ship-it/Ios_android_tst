package com.iosexperience.feature.launcher

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iosexperience.core.data.Graph
import com.iosexperience.core.data.HomeEntry
import com.iosexperience.core.data.LayoutLogic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class LauncherViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app)
    private val layout = Graph.layout

    private val _installed = MutableStateFlow<Map<String, LauncherApp>>(emptyMap())
    val installed: StateFlow<Map<String, LauncherApp>> = _installed
    private val _entries = MutableStateFlow<List<HomeEntry>>(emptyList())
    val entries: StateFlow<List<HomeEntry>> = _entries
    private val _dock = MutableStateFlow<List<String>>(emptyList())
    val dock: StateFlow<List<String>> = _dock
    private val _recents = MutableStateFlow<List<String>>(emptyList())
    val recents: StateFlow<List<String>> = _recents
    private val _shortcuts = MutableStateFlow(layout.loadShortcuts())
    val shortcuts: StateFlow<List<String>> = _shortcuts

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) { repo.loadApps() }
            val map = apps.associateBy { it.key }
            var d = layout.loadDock()
            if (d.isEmpty()) d = withContext(Dispatchers.IO) { repo.defaultDock() }.also { layout.saveDock(it) }
            d = d.filter { it in map }.take(4)
            _installed.value = map
            _dock.value = d
            _entries.value = LayoutLogic.sync(layout.loadEntries(), apps.map { it.key }, d.toSet()).also { layout.saveEntries(it) }
            _recents.value = layout.loadRecents().filter { it in map }
        }
    }

    private fun setEntries(l: List<HomeEntry>) { _entries.value = l; layout.saveEntries(l) }

    fun drop(from: Int, to: Int, merge: Boolean) {
        val cur = _entries.value
        setEntries(if (merge) LayoutLogic.mergeInto(cur, from, to, UUID.randomUUID().toString()) else LayoutLogic.reorder(cur, from, to))
    }
    fun removeFromFolder(folderId: String, key: String) = setEntries(LayoutLogic.removeFromFolder(_entries.value, folderId, key))
    fun renameFolder(folderId: String, name: String) = setEntries(LayoutLogic.rename(_entries.value, folderId, name))

    /** Troca o app do dock na posição [slot] por [newKey] (o antigo volta à tela inicial no próximo sync). */
    fun setDockApp(slot: Int, newKey: String) {
        val d = _dock.value.toMutableList().filter { it != newKey }.toMutableList()
        if (slot in d.indices) d[slot.coerceAtMost(d.lastIndex)] = newKey else d.add(newKey)
        layout.saveDock(d.take(4)); refresh()
    }

    fun setShortcut(slot: Int, value: String) {
        val l = _shortcuts.value.toMutableList(); if (slot in l.indices) l[slot] = value
        _shortcuts.value = l; layout.saveShortcuts(l)
    }

    fun open(key: String, options: Bundle?): Boolean {
        val ok = repo.launch(key, options)
        if (ok) { val r = (listOf(key) + _recents.value.filter { it != key }).take(12); _recents.value = r; layout.saveRecents(r) }
        return ok
    }
    fun removeRecent(key: String) { val r = _recents.value - key; _recents.value = r; layout.saveRecents(r) }
    fun clearRecents() { _recents.value = emptyList(); layout.saveRecents(emptyList()) }
    fun resetHome() { layout.resetHome(); refresh() }
}
