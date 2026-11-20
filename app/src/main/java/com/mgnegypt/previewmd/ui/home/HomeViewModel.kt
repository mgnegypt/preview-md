package com.mgnegypt.previewmd.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mgnegypt.previewmd.data.files.FileRepository
import com.mgnegypt.previewmd.data.files.TreeChild
import com.mgnegypt.previewmd.data.recent.FolderRoot
import com.mgnegypt.previewmd.data.recent.RecentFile
import com.mgnegypt.previewmd.data.recent.RecentFilesRepository
import com.mgnegypt.previewmd.data.settings.SettingsRepository
import com.mgnegypt.previewmd.data.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HomeTab {
    RECENT,
    FOLDERS
}

data class FolderSection(
    val root: FolderRoot,
    val files: List<TreeChild>
)

data class HomeUiState(
    val query: String = "",
    val tab: HomeTab = HomeTab.RECENT,
    val recents: List<RecentFile> = emptyList(),
    val folders: List<FolderSection> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

/** One-shot feedback for the swipe actions, shown as a snackbar. */
sealed interface HomeMessage {
    data object Deleted : HomeMessage
    data object DeleteFailed : HomeMessage
    data object Copied : HomeMessage
    data object CopiedToAppFolder : HomeMessage
    data object CopyFailed : HomeMessage
    data object CopyNeedsAppFolder : HomeMessage
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val recentFiles: RecentFilesRepository,
    private val files: FileRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val tabFlow = MutableStateFlow(HomeTab.RECENT)
    private val folderSections = MutableStateFlow<List<FolderSection>>(emptyList())

    private val recentsFlow = queryFlow.flatMapLatest { query ->
        recentFiles.searchRecents(query)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        queryFlow,
        tabFlow,
        recentsFlow,
        folderSections,
        settings.settings
    ) { query, tab, recents, folders, settingsState ->
        HomeUiState(
            query = query,
            tab = tab,
            recents = recents,
            folders = folders.map { section ->
                if (query.isBlank()) {
                    section
                } else {
                    section.copy(
                        files = section.files.filter {
                            it.name.contains(query, ignoreCase = true)
                        }
                    )
                }
            },
            themeMode = settingsState.themeMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    private val _message = MutableStateFlow<HomeMessage?>(null)
    val message: StateFlow<HomeMessage?> = _message.asStateFlow()

    fun onMessageShown() {
        _message.value = null
    }

    init {
        viewModelScope.launch {
            // Drop recents whose permission was revoked since last run.
            recentFiles.pruneInaccessible { uri ->
                files.isAccessible(Uri.parse(uri))
            }
        }
        viewModelScope.launch {
            recentFiles.folderRoots.collect { refreshFolderSections() }
        }
    }

    private suspend fun refreshFolderSections() {
        val roots = recentFiles.folderRoots.first()
        folderSections.value = roots.map { root ->
            val children = runCatching {
                files.listMarkdownFiles(Uri.parse(root.uri))
            }.getOrDefault(emptyList())
            FolderSection(root = root, files = children)
        }
    }

    fun onQueryChange(query: String) {
        queryFlow.value = query
    }

    fun onTabChange(tab: HomeTab) {
        tabFlow.value = tab
    }

    /** The header theme button flips between light and dark directly. */
    fun onToggleTheme(currentlyDark: Boolean) {
        viewModelScope.launch {
            settings.setThemeMode(
                if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK
            )
        }
    }

    fun onRecentOpened(recent: RecentFile) {
        viewModelScope.launch {
            recentFiles.recordOpened(
                uri = recent.uri,
                name = recent.name,
                folderLabel = recent.folderLabel,
                treeUri = recent.treeUri
            )
        }
    }

    fun onFolderFileOpened(child: TreeChild, root: FolderRoot) {
        viewModelScope.launch {
            recentFiles.recordOpened(
                uri = child.uri.toString(),
                name = child.name,
                folderLabel = root.name,
                treeUri = root.uri
            )
        }
    }

    // ---- swipe actions --------------------------------------------------

    /** Deletes the actual file behind a recents entry, then its entry. */
    fun onDeleteRecent(recent: RecentFile) {
        viewModelScope.launch {
            if (files.deleteDocument(Uri.parse(recent.uri))) {
                recentFiles.removeRecent(recent.uri)
                refreshFolderSections()
                _message.value = HomeMessage.Deleted
            } else {
                _message.value = HomeMessage.DeleteFailed
            }
        }
    }

    /** Deletes a file shown under a granted folder. */
    fun onDeleteFolderFile(child: TreeChild) {
        viewModelScope.launch {
            if (files.deleteDocument(child.uri)) {
                recentFiles.removeRecent(child.uri.toString())
                refreshFolderSections()
                _message.value = HomeMessage.Deleted
            } else {
                _message.value = HomeMessage.DeleteFailed
            }
        }
    }

    fun onCopyRecent(recent: RecentFile) {
        copy(
            uri = Uri.parse(recent.uri),
            folderLabel = recent.folderLabel,
            treeUri = recent.treeUri
        )
    }

    fun onCopyFolderFile(child: TreeChild, root: FolderRoot) {
        copy(uri = child.uri, folderLabel = root.name, treeUri = root.uri)
    }

    /**
     * Duplicates beside the original when its folder is writable;
     * otherwise into the app folder, which is the app's own writable
     * home. The copy is recorded so it shows in recents.
     */
    private fun copy(uri: Uri, folderLabel: String, treeUri: String?) {
        viewModelScope.launch {
            val beside = files.duplicateBeside(uri)
            if (beside != null) {
                recentFiles.recordOpened(
                    uri = beside.toString(),
                    name = files.displayName(beside),
                    folderLabel = folderLabel,
                    treeUri = treeUri
                )
                refreshFolderSections()
                _message.value = HomeMessage.Copied
                return@launch
            }
            val treeString = settings.settings.first().appFolderTreeUri
            val folder = treeString?.let {
                files.appFolderDocument(Uri.parse(it))
            }
            if (folder == null) {
                _message.value = HomeMessage.CopyNeedsAppFolder
                return@launch
            }
            val copied = files.duplicateIntoFolder(uri, folder)
            if (copied != null) {
                recentFiles.recordOpened(
                    uri = copied.toString(),
                    name = files.displayName(copied),
                    folderLabel = FileRepository.APP_FOLDER_NAME,
                    treeUri = treeString
                )
                _message.value = HomeMessage.CopiedToAppFolder
            } else {
                _message.value = HomeMessage.CopyFailed
            }
        }
    }

    class Factory(
        private val recentFiles: RecentFilesRepository,
        private val files: FileRepository,
        private val settings: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return HomeViewModel(recentFiles, files, settings) as T
        }
    }
}
