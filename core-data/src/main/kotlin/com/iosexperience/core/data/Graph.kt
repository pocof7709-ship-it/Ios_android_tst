package com.iosexperience.core.data

import android.app.Application

/** Contêiner de dependências manual (Hilt não se justifica com <6 singletons). Inicializado em Application.onCreate. */
object Graph {
    private lateinit var app: Application
    fun init(a: Application) { app = a }
    val settings by lazy { SettingsStore(app) }
    val layout by lazy { LayoutStore(app) }
    val keys by lazy { SecureKeyStore(app) }
    val icons by lazy { AppIcons(app) }
}
