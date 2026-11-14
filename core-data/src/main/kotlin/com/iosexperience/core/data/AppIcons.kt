package com.iosexperience.core.data

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap

fun appKey(pkg: String, activity: String) = "$pkg/$activity"

/** Ícones reais dos apps (via PackageManager), com cache LRU para memória controlada. */
class AppIcons(private val ctx: Context) {
    private val cache = LruCache<String, Bitmap>(140)
    private val size = 168

    fun peek(key: String): Bitmap? = cache.get(key)

    /** [key] = "pacote/atividade" ou "pkg:<pacote>" para ícone do app (usado em notificações). */
    fun get(key: String): Bitmap? {
        cache.get(key)?.let { return it }
        val pm = ctx.packageManager
        val d = try {
            if (key.startsWith("pkg:")) pm.getApplicationIcon(key.removePrefix("pkg:"))
            else {
                val (pkg, act) = key.split("/", limit = 2)
                try { pm.getActivityIcon(ComponentName(pkg, act)) } catch (e: Exception) { pm.getApplicationIcon(pkg) }
            }
        } catch (e: Exception) { return null }
        return d.toBitmap(size, size).also { cache.put(key, it) }
    }
}
