package com.iosexperience.feature.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.session.MediaController
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow

data class NotifItem(
    val key: String, val pkg: String, val appLabel: String,
    val title: String, val text: String, val whenMs: Long,
    val clearable: Boolean, val intent: PendingIntent?,
)

data class MediaInfo(val pkg: String, val title: String, val artist: String, val playing: Boolean, val controller: MediaController?)

/** Estado em memória (nada é gravado em disco nem enviado para fora do aparelho). */
object NotificationStore {
    val items = MutableStateFlow<List<NotifItem>>(emptyList())
    val media = MutableStateFlow<MediaInfo?>(null)
    val connected = MutableStateFlow(false)
    internal var service: IOSNotificationListener? = null

    fun dismiss(key: String) { service?.cancelNotification(key) }
    fun dismissAllClearable() { service?.cancelAllNotifications() }

    fun hasAccess(ctx: Context) = NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)
    fun accessSettingsIntent() = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
