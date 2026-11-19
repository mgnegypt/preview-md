package com.mgnegypt.previewmd

import android.content.Context
import androidx.room.Room
import com.mgnegypt.previewmd.data.files.FileRepository
import com.mgnegypt.previewmd.data.recent.AppDatabase
import com.mgnegypt.previewmd.data.recent.RecentFilesRepository
import com.mgnegypt.previewmd.data.settings.SettingsRepository

/**
 * Manual dependency container, created once by [PreviewMdApp].
 * Repositories are added here phase by phase; no DI framework is used.
 */
class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext

    val settingsRepository = SettingsRepository(appContext)

    private val database = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "preview-md.db"
    ).build()

    val recentFilesRepository = RecentFilesRepository(
        recentDao = database.recentFileDao(),
        folderDao = database.folderRootDao()
    )

    val fileRepository = FileRepository(appContext)
}
