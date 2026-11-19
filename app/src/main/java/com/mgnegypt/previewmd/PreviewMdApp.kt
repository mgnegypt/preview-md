package com.mgnegypt.previewmd

import android.app.Application

class PreviewMdApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
