package com.iosexperience.core.data

/** Entradas da tela inicial. App.key = "pacote/atividade". */
sealed interface HomeEntry {
    val id: String
    data class App(val key: String) : HomeEntry { override val id get() = key }
    data class Folder(override val id: String, val name: String, val keys: List<String>) : HomeEntry
}

/** Lógica pura (sem Android) de organização da tela inicial — totalmente testável em JVM. */
object LayoutLogic {
    fun keysOf(e: HomeEntry): List<String> = when (e) {
        is HomeEntry.App -> listOf(e.key)
        is HomeEntry.Folder -> e.keys
    }

    /** Remove apps desinstalados/dock, dissolve pastas com <2 itens e acrescenta apps novos ao fim. */
    fun sync(entries: List<HomeEntry>, installedOrdered: List<String>, excluded: Set<String>): List<HomeEntry> {
        val valid = installedOrdered.filter { it !in excluded }.toSet()
        val cleaned = entries.mapNotNull { e ->
            when (e) {
                is HomeEntry.App -> e.takeIf { it.key in valid }
                is HomeEntry.Folder -> {
                    val k = e.keys.filter { it in valid }.distinct()
                    when { k.isEmpty() -> null; k.size == 1 -> HomeEntry.App(k[0]); else -> e.copy(keys = k) }
                }
            }
        }
        val present = cleaned.flatMap { keysOf(it) }.toSet()
        return cleaned + installedOrdered.filter { it in valid && it !in present }.map { HomeEntry.App(it) }
    }

    fun reorder(entries: List<HomeEntry>, from: Int, to: Int): List<HomeEntry> {
        if (from !in entries.indices || to !in entries.indices || from == to) return entries
        return entries.toMutableList().apply { add(to, removeAt(from)) }
    }

    /** Solta [from] sobre [to]: cria pasta (app sobre app) ou adiciona à pasta existente. */
    fun mergeInto(entries: List<HomeEntry>, from: Int, to: Int, newId: String, defaultName: String = "Pasta"): List<HomeEntry> {
        if (from !in entries.indices || to !in entries.indices || from == to) return entries
        val src = entries[from]; val dst = entries[to]
        if (src !is HomeEntry.App) return reorder(entries, from, to)
        val result = entries.toMutableList()
        when (dst) {
            is HomeEntry.App -> {
                result[to] = HomeEntry.Folder(newId, defaultName, listOf(dst.key, src.key))
                result.removeAt(from)
            }
            is HomeEntry.Folder -> {
                result[to] = dst.copy(keys = dst.keys + src.key)
                result.removeAt(from)
            }
        }
        return result
    }

    fun removeFromFolder(entries: List<HomeEntry>, folderId: String, key: String): List<HomeEntry> {
        val idx = entries.indexOfFirst { it.id == folderId }
        val f = entries.getOrNull(idx) as? HomeEntry.Folder ?: return entries
        if (key !in f.keys) return entries
        val left = f.keys - key
        val result = entries.toMutableList()
        if (left.size >= 2) {
            result[idx] = f.copy(keys = left)
            result.add(idx + 1, HomeEntry.App(key))
        } else {
            result.removeAt(idx)
            result.addAll(idx, (left + key).map { HomeEntry.App(it) })
        }
        return result
    }

    fun rename(entries: List<HomeEntry>, folderId: String, name: String): List<HomeEntry> =
        entries.map { if (it is HomeEntry.Folder && it.id == folderId) it.copy(name = name.trim().ifEmpty { "Pasta" }.take(24)) else it }
}
