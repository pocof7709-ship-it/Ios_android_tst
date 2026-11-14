package com.iosexperience.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.iosexperience.app.ui.HomeScreen
import com.iosexperience.core.data.Graph
import com.iosexperience.core.data.Perf
import com.iosexperience.core.data.ThemeMode
import com.iosexperience.core.design.theme.AccentPalette
import com.iosexperience.core.design.theme.IOSTheme
import com.iosexperience.core.design.theme.PerformanceProfile
import com.iosexperience.feature.ai.AiViewModel
import com.iosexperience.feature.launcher.LauncherViewModel
import kotlinx.coroutines.launch

class LauncherActivity : ComponentActivity() {
    private val vm: LauncherViewModel by viewModels()
    private val aiVm: AiViewModel by viewModels()
    private var homeSignal by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            val s by Graph.settings.state.collectAsState()
            val dark = when (s.themeMode) { ThemeMode.DARK -> true; ThemeMode.LIGHT -> false; ThemeMode.SYSTEM -> isSystemInDarkTheme() }
            IOSTheme(darkTheme = dark, profile = s.perf.toProfile(), accent = AccentPalette[s.accentIndex].second) {
                HomeScreen(vm, aiVm, homeSignal)
            }
        }
        // 120 Hz no POCO F7: pede taxa alta quando o perfil permite; o sistema pode ignorar (modo economia do HyperOS).
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Graph.settings.state.collect { s ->
                    window.attributes = window.attributes.also { it.preferredRefreshRate = if (s.perf == Perf.ECONOMIC) 60f else 120f }
                    val bars = WindowCompat.getInsetsController(window, window.decorView)
                    if (s.fakeStatusBar) {
                        bars.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        bars.hide(WindowInsetsCompat.Type.statusBars())
                    } else bars.show(WindowInsetsCompat.Type.statusBars())
                }
            }
        }
    }

    // Botão Home pressionado com o launcher já aberto.
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); homeSignal++ }
}

fun Perf.toProfile() = when (this) { Perf.ECONOMIC -> PerformanceProfile.ECONOMIC; Perf.BALANCED -> PerformanceProfile.BALANCED; Perf.ADVANCED -> PerformanceProfile.ADVANCED }
