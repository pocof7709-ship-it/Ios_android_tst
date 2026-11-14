package com.iosexperience.core.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** SharedPreferences simples: leitura síncrona na inicialização (sem flash de tema) e StateFlow para a UI. */
class SettingsStore(ctx: Context) {
    private val p = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<UiSettings> = _state

    private fun <E : Enum<E>> enumOf(key: String, def: E, values: Array<E>): E =
        values.firstOrNull { it.name == p.getString(key, null) } ?: def

    private fun read(): UiSettings {
        val d = UiSettings()
        return UiSettings(
            themeMode = enumOf("themeMode", d.themeMode, ThemeMode.values()),
            accentIndex = p.getInt("accentIndex", d.accentIndex),
            iconCorner = p.getInt("iconCorner", d.iconCorner),
            iconSizeDp = p.getInt("iconSizeDp", d.iconSizeDp),
            gridRows = p.getInt("gridRows", d.gridRows),
            gridColumns = p.getInt("gridColumns", d.gridColumns),
            perf = enumOf("perf", d.perf, Perf.values()),
            animations = p.getBoolean("animations", d.animations),
            clockStyle = p.getInt("clockStyle", d.clockStyle),
            showLockPreviews = p.getBoolean("showLockPreviews", d.showLockPreviews),
            showCalendar = p.getBoolean("showCalendar", d.showCalendar),
            aiEnabled = p.getBoolean("aiEnabled", d.aiEnabled),
            aiHistory = p.getBoolean("aiHistory", d.aiHistory),
            aiProvider = p.getString("aiProvider", d.aiProvider) ?: d.aiProvider,
            aiModel = p.getString("aiModel", d.aiModel) ?: d.aiModel,
            wallpaper = p.getInt("wallpaper", d.wallpaper),
            useSystemWallpaper = p.getBoolean("useSystemWallpaper", d.useSystemWallpaper),
            fakeStatusBar = p.getBoolean("fakeStatusBar", d.fakeStatusBar),
        ).sanitized()
    }

    fun update(block: (UiSettings) -> UiSettings) {
        val s = block(_state.value).sanitized()
        _state.value = s
        p.edit()
            .putString("themeMode", s.themeMode.name).putInt("accentIndex", s.accentIndex)
            .putInt("iconCorner", s.iconCorner).putInt("iconSizeDp", s.iconSizeDp)
            .putInt("gridRows", s.gridRows).putInt("gridColumns", s.gridColumns)
            .putString("perf", s.perf.name).putBoolean("animations", s.animations)
            .putInt("clockStyle", s.clockStyle).putBoolean("showLockPreviews", s.showLockPreviews)
            .putBoolean("showCalendar", s.showCalendar).putBoolean("aiEnabled", s.aiEnabled)
            .putBoolean("aiHistory", s.aiHistory).putString("aiProvider", s.aiProvider)
            .putString("aiModel", s.aiModel).putInt("wallpaper", s.wallpaper)
            .putBoolean("useSystemWallpaper", s.useSystemWallpaper).putBoolean("fakeStatusBar", s.fakeStatusBar).apply()
    }

    fun reset() = update { UiSettings() }
}
