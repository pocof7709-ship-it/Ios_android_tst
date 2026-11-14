package com.iosexperience.core.data

import org.junit.Assert.*
import org.junit.Test

class UiSettingsTest {
    @Test fun wallpaperIsClamped() { assertEquals(3, UiSettings(wallpaper = 9).sanitized().wallpaper); assertEquals(0, UiSettings(wallpaper = -1).sanitized().wallpaper) }
    @Test fun defaultsAreIosLike() { val d = UiSettings(); assertEquals(22, d.iconCorner); assertFalse(d.fakeStatusBar); assertFalse(d.useSystemWallpaper) }
}
