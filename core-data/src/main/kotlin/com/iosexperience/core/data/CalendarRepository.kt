package com.iosexperience.core.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat

data class CalEvent(val title: String, val beginMs: Long, val allDay: Boolean)

object CalendarRepository {
    fun hasPermission(ctx: Context) =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    /** Próximo evento nas próximas 24h. Retorna null sem permissão ou sem eventos. */
    fun nextEvent(ctx: Context): CalEvent? {
        if (!hasPermission(ctx)) return null
        val now = System.currentTimeMillis()
        val b = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(b, now); ContentUris.appendId(b, now + 24L * 3600_000)
        return try {
            ctx.contentResolver.query(
                b.build(),
                arrayOf(CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN, CalendarContract.Instances.ALL_DAY),
                null, null, CalendarContract.Instances.BEGIN + " ASC",
            )?.use { c ->
                var found: CalEvent? = null
                while (found == null && c.moveToNext()) {
                    val allDay = c.getInt(2) == 1
                    if (c.getLong(1) >= now || allDay) found = CalEvent(c.getString(0) ?: "(sem título)", c.getLong(1), allDay)
                }
                found
            }
        } catch (e: SecurityException) { null }
    }
}
