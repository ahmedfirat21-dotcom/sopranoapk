package com.soprano.chat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SopranoApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
