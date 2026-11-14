package com.iosexperience.core.data

enum class ThemeMode { SYSTEM, DARK, LIGHT }
enum class Perf { ECONOMIC, BALANCED, ADVANCED }

/** Todas as preferências visuais/funcionais persistidas. Valores sempre passam por [sanitized]. */
data class UiSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentIndex: Int = 0,
    val iconCorner: Int = 22,          // % de arredondamento do ícone (10..50)
    val iconSizeDp: Int = 60,          // 48..72
    val gridRows: Int = 6,             // 3..8
    val gridColumns: Int = 4,          // 3..6
    val perf: Perf = Perf.BALANCED,
    val animations: Boolean = true,
    val clockStyle: Int = 0,           // 0..3
    val showLockPreviews: Boolean = true,
    val showCalendar: Boolean = false,
    val aiEnabled: Boolean = true,
    val aiHistory: Boolean = true,
    val aiProvider: String = "local",
    val aiModel: String = "",
    val wallpaper: Int = 0,               // 0..3: wallpapers próprios
    val useSystemWallpaper: Boolean = false,
    val fakeStatusBar: Boolean = false,   // experimental: esconde a barra do sistema e desenha a do app
) {
    fun sanitized() = copy(
        accentIndex = accentIndex.coerceIn(0, 7),
        iconCorner = iconCorner.coerceIn(10, 50),
        iconSizeDp = iconSizeDp.coerceIn(48, 72),
        gridRows = gridRows.coerceIn(3, 8),
        gridColumns = gridColumns.coerceIn(3, 6),
        clockStyle = clockStyle.coerceIn(0, 3),
        wallpaper = wallpaper.coerceIn(0, 3),
    )
}
