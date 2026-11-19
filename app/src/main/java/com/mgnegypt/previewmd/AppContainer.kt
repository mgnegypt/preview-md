package com.mgnegypt.previewmd

import android.content.Context
import com.mgnegypt.previewmd.data.settings.SettingsRepository

/**
 * Manual dependency container, created once by [PreviewMdApp].
 * Repositories are added here phase by phase; no DI framework is used.
 */
class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext

    val settingsRepository = SettingsRepository(appContext)
}
