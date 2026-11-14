package com.iosexperience.app

import android.app.Application
import com.iosexperience.core.data.Graph

class IOSApp : Application() {
    override fun onCreate() { super.onCreate(); Graph.init(this) }
}
