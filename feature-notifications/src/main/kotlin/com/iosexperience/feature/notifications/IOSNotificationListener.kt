package com.iosexperience.feature.notifications

import android.app.Notification
import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class IOSNotificationListener : NotificationListenerService() {
    private var msm: MediaSessionManager? = null
    private var controller: MediaController? = null
    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { updateSessions(it) }
    private val cb = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) = publishMedia()
        override fun onMetadataChanged(metadata: MediaMetadata?) = publishMedia()
    }

    override fun onListenerConnected() {
        NotificationStore.service = this
        NotificationStore.connected.value = true
        refresh()
        try {
            val m = getSystemService(MediaSessionManager::class.java)
            val cn = ComponentName(this, IOSNotificationListener::class.java)
            msm = m
            m.addOnActiveSessionsChangedListener(sessionsListener, cn)
            updateSessions(m.getActiveSessions(cn))
        } catch (e: SecurityException) { /* sem acesso a sessões de mídia */ }
    }

    override fun onListenerDisconnected() {
        NotificationStore.service = null
        NotificationStore.connected.value = false
        NotificationStore.items.value = emptyList()
        NotificationStore.media.value = null
        msm?.removeOnActiveSessionsChangedListener(sessionsListener)
        controller?.unregisterCallback(cb); controller = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) = refresh()
    override fun onNotificationRemoved(sbn: StatusBarNotification?) = refresh()

    private fun refresh() {
        val active = try { activeNotifications } catch (e: SecurityException) { null } ?: return
        val pm = packageManager
        NotificationStore.items.value = active.asSequence()
            .filter { it.packageName != packageName }
            .filter { it.notification.flags and Notification.FLAG_GROUP_SUMMARY == 0 }
            .filter { !it.isOngoing }
            .mapNotNull { sbn ->
                val ex = sbn.notification.extras
                val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
                val text = (ex.getCharSequence(Notification.EXTRA_TEXT) ?: ex.getCharSequence(Notification.EXTRA_BIG_TEXT))?.toString().orEmpty()
                if (title.isBlank() && text.isBlank()) return@mapNotNull null
                val label = try { pm.getApplicationInfo(sbn.packageName, 0).loadLabel(pm).toString() } catch (e: Exception) { sbn.packageName }
                NotifItem(sbn.key, sbn.packageName, label, title, text, sbn.postTime, sbn.isClearable, sbn.notification.contentIntent)
            }
            .sortedByDescending { it.whenMs }.toList()
    }

    private fun updateSessions(list: List<MediaController>?) {
        controller?.unregisterCallback(cb)
        controller = list?.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: list?.firstOrNull()
        controller?.registerCallback(cb)
        publishMedia()
    }

    private fun publishMedia() {
        val c = controller
        val md = c?.metadata
        val title = md?.getString(MediaMetadata.METADATA_KEY_TITLE)
        NotificationStore.media.value = if (c == null || title.isNullOrBlank()) null else MediaInfo(
            pkg = c.packageName, title = title,
            artist = md.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
            playing = c.playbackState?.state == PlaybackState.STATE_PLAYING, controller = c,
        )
    }
}
