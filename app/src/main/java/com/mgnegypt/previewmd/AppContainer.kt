package com.mgnegypt.previewmd

import android.content.Context
import android.net.Uri
import androidx.room.Room
import kotlinx.coroutines.flow.MutableStateFlow
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

    /**
     * A file the user asked to open from outside the navigation graph
     * (Welcome button, a VIEW/SEND intent). The shell consumes it once and
     * navigates to the viewer.
     */
    val pendingOpen = MutableStateFlow<String?>(null)

    fun requestOpen(uri: String) {
        pendingOpen.value = uri
    }

    fun consumePendingOpen(): String? {
        val value = pendingOpen.value
        pendingOpen.value = null
        return value
    }

    /**
     * Intake for a freshly picked or externally opened document: persist
     * the permission and record it in recents. Navigation is the caller's
     * job (the pickers navigate directly; external opens go through
     * [pendingOpen], whose consumer in the shell calls this once).
     */
    suspend fun intakeOpenedFile(
        uri: Uri,
        treeUri: String? = null,
        folderLabel: String? = null
    ) {
        fileRepository.takePersistablePermission(uri)
        val name = fileRepository.displayName(uri)
        val label = folderLabel ?: fileRepository.parentLabel(uri)
        recentFilesRepository.recordOpened(
            uri = uri.toString(),
            name = name,
            folderLabel = label,
            treeUri = treeUri
        )
    }
}
