package com.talos.forge

import android.app.Application
import com.talos.forge.data.ApiClient
import com.talos.forge.data.SessionManager

class TalosApp : Application() {
    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        ApiClient.init(sessionManager)
    }
}
