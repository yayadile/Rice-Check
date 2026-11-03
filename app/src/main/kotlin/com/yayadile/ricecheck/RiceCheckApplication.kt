package com.yayadile.ricecheck

import android.app.Application
import com.yayadile.ricecheck.di.AppContainer

class RiceCheckApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
