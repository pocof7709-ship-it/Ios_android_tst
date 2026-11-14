package com.iosexperience.feature.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.net.Uri
import com.iosexperience.core.data.appKey

data class LauncherApp(val label: String, val packageName: String, val activityName: String) {
    val key get() = appKey(packageName, activityName)
}

/** Lista apps com ícone no launcher (CATEGORY_LAUNCHER). Não requer QUERY_ALL_PACKAGES. */
class AppRepository(private val context: Context) {
    fun loadApps(): List<LauncherApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .filter { it.activityInfo.packageName != context.packageName }
            .map { LauncherApp(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.activityInfo.name) }
            .sortedBy { it.label.lowercase() }
    }

    /** Dock inicial: apps padrão do usuário para telefone, mensagens, navegador e câmera (se resolvíveis). */
    fun defaultDock(): List<String> {
        val pm = context.packageManager
        val intents = listOf(
            Intent(Intent.ACTION_DIAL),
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com")),
            Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
        )
        return intents.mapNotNull { i ->
            pm.resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo
                ?.takeIf { it.packageName != "android" }
                ?.let { appKey(it.packageName, it.name) }
        }.distinct()
    }

    /** Abre via Intent oficial; [options] carrega a animação de escala (ActivityOptions). */
    fun launch(key: String, options: Bundle?): Boolean {
        val (pkg, act) = key.split("/", limit = 2)
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setClassName(pkg, act)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return try { context.startActivity(i, options); true } catch (e: Exception) { false }
    }
}
