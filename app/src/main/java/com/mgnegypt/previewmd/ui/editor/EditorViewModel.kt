package com.mgnegypt.previewmd.ui.editor

import android.net.Uri
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mgnegypt.previewmd.data.files.DraftStore
import com.mgnegypt.previewmd.data.files.FileAccessException
import com.mgnegypt.previewmd.data.files.FileErrorKind
import com.mgnegypt.previewmd.data.files.FileRepository
import com.mgnegypt.previewmd.data.settings.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorUiState(
    val loading: Boolean = true,
    val fileName: String = "",
    val dirty: Boolean = false,
    val saving: Boolean = false,
    val highlightEnabled: Boolean = true,
    val restoredDraft: Boolean = false,
    val loadError: FileErrorKind? = null,
    val saveError: FileErrorKind? = null,
    val externalChange: Boolean = false
)

/** How a save attempt ended; the screen acts on the setup case. */
enum class EditorSaveOutcome {
    SAVED,
    FAILED,

    /** The app folder is not set up yet; the screen must run setup. */
    NEEDS_FOLDER_SETUP
}

/**
 * Editor state: the text lives in a [TextFieldState] held here so it
 * survives rotation; a debounced draft in app-private storage survives
 * process death. Saving writes with truncation and then verifies by
 * re-reading and comparing, so a silent partial write is reported as an
 * error instead of losing the user's work.
 */
class EditorViewModel(
    private val files: FileRepository,
    private val drafts: DraftStore,
    private val settings: SettingsRepository,
    uriString: String
) : ViewModel() {

    /** The document being edited; retargeted to the app-folder copy on save. */
    private var uri: Uri = Uri.parse(uriString)

    val textState = TextFieldState()

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var originalText: String = ""
    private var fingerprint: Pair<Long, Long>? = null
    private var loaded = false
    private var draftJob: Job? = null

    init {
        load()
    }

    fun retryLoad() {
        _uiState.value = EditorUiState(loading = true)
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val disk = files.readText(uri)
                originalText = disk
                fingerprint = files.documentFingerprint(uri)
                val draft = drafts.read(uri.toString())
                val initial = draft ?: disk
                textState.setTextAndPlaceCursorAtEnd(initial)
                textState.edit { selection = TextRange.Zero }
                loaded = true
                _uiState.value = EditorUiState(
                    loading = false,
                    fileName = files.displayName(uri),
                    restoredDraft = draft != null && draft != disk,
                    highlightEnabled = initial.length <= HIGHLIGHT_LIMIT
                )
                observeChanges()
            } catch (e: FileAccessException) {
                _uiState.value = EditorUiState(
                    loading = false,
                    loadError = e.kind
                )
            }
        }
    }

    private fun observeChanges() {
        viewModelScope.launch {
            snapshotFlow { textState.text.toString() }.collect { current ->
                val isDirty = current != originalText
                _uiState.update {
                    it.copy(
                        dirty = isDirty,
                        highlightEnabled =
                            current.length <= HIGHLIGHT_LIMIT
                    )
                }
                draftJob?.cancel()
                if (isDirty) {
                    draftJob = launch {
                        delay(DRAFT_DEBOUNCE_MS)
                        drafts.write(uri.toString(), current)
                    }
                } else {
                    drafts.delete(uri.toString())
                }
            }
        }
    }

    // ---- toolbar ------------------------------------------------------

    private fun applyOp(op: (String, Int, Int) -> EditResult) {
        val current = textState.text.toString()
        val sel = textState.selection
        val result = op(current, sel.start, sel.end)
        if (result.text != current) {
            textState.edit {
                replace(0, length, result.text)
                selection = TextRange(result.selectionStart, result.selectionEnd)
            }
        }
    }

    fun bold() = applyOp { t, s, e ->
        MarkdownEditOps.toggleWrap(t, s, e, "**")
    }

    fun italic() = applyOp { t, s, e ->
        MarkdownEditOps.toggleWrap(t, s, e, "*")
    }

    fun heading() = applyOp { t, s, e ->
        MarkdownEditOps.toggleHeading(t, s, e, level = 1)
    }

    fun link() = applyOp { t, s, e ->
        MarkdownEditOps.insertLink(t, s, e)
    }

    fun code() {
        val current = textState.text.toString()
        val selection = textState.selection
        val selected = current.substring(selection.start, selection.end)
        if (selected.contains('\n')) {
            applyOp { t, s, e -> MarkdownEditOps.toggleCodeBlock(t, s, e) }
        } else {
            applyOp { t, s, e -> MarkdownEditOps.toggleInlineCode(t, s, e) }
        }
    }

    fun list() = applyOp { t, s, e ->
        MarkdownEditOps.toggleList(t, s, e)
    }

    // ---- saving -------------------------------------------------------

    /**
     * Saves into the app folder: a file already inside it is written in
     * place; a file from anywhere else is copied there under the same
     * name (the original is never touched) and the editor retargets to
     * the copy, so later saves and the preview work on the copy. Without
     * a configured app folder nothing is written and the screen is told
     * to run the one-time setup first.
     */
    fun save(onResult: (EditorSaveOutcome) -> Unit) {
        if (_uiState.value.saving) return
        val current = textState.text.toString()
        _uiState.update { it.copy(saving = true, saveError = null) }
        viewModelScope.launch {
            val treeString = settings.settings.first().appFolderTreeUri
            if (treeString == null) {
                _uiState.update { it.copy(saving = false) }
                onResult(EditorSaveOutcome.NEEDS_FOLDER_SETUP)
                return@launch
            }
            try {
                val folder = files.appFolderDocument(Uri.parse(treeString))
                    ?: throw FileAccessException(FileErrorKind.IO_FAILURE)
                val name = _uiState.value.fileName
                    .ifBlank { files.displayName(uri) }
                val target = if (files.isInsideFolder(uri, folder)) {
                    uri
                } else {
                    files.findInFolder(folder, name)
                        ?: files.createInFolder(folder, name)
                        ?: throw FileAccessException(FileErrorKind.IO_FAILURE)
                }
                val previousUri = uri
                files.writeText(target, current)
                val readBack = files.readText(target)
                if (readBack != current) {
                    throw FileAccessException(FileErrorKind.IO_FAILURE)
                }
                uri = target
                originalText = current
                fingerprint = files.documentFingerprint(target)
                drafts.delete(previousUri.toString())
                if (target != previousUri) drafts.delete(target.toString())
                _uiState.update { it.copy(saving = false, dirty = false) }
                onResult(EditorSaveOutcome.SAVED)
            } catch (e: FileAccessException) {
                _uiState.update { it.copy(saving = false, saveError = e.kind) }
                onResult(EditorSaveOutcome.FAILED)
            }
        }
    }

    /** The document the editor is currently bound to. */
    fun currentUriString(): String = uri.toString()

    /**
     * Persists a freshly granted tree as the app folder's parent and
     * retries the save that asked for the setup.
     */
    fun onAppFolderPicked(treeUri: Uri, onResult: (EditorSaveOutcome) -> Unit) {
        files.takePersistablePermission(treeUri)
        viewModelScope.launch {
            settings.setAppFolderTreeUri(treeUri.toString())
            save(onResult)
        }
    }

    fun clearSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    fun discardAndForget() {
        viewModelScope.launch { drafts.delete(uri.toString()) }
    }

    // ---- external modification ----------------------------------------

    /** Called when the editor returns to the foreground. */
    fun checkExternalChange() {
        if (!loaded || _uiState.value.externalChange) return
        viewModelScope.launch {
            val base = fingerprint ?: return@launch
            val now = files.documentFingerprint(uri) ?: return@launch
            if (now != base) {
                val diskNow = try {
                    files.readText(uri)
                } catch (e: FileAccessException) {
                    return@launch
                }
                if (diskNow != originalText) {
                    _uiState.update { it.copy(externalChange = true) }
                } else {
                    fingerprint = now
                }
            }
        }
    }

    fun reloadFromDisk() {
        viewModelScope.launch {
            try {
                val disk = files.readText(uri)
                originalText = disk
                fingerprint = files.documentFingerprint(uri)
                drafts.delete(uri.toString())
                textState.setTextAndPlaceCursorAtEnd(disk)
                textState.edit { selection = TextRange.Zero }
                _uiState.update {
                    it.copy(
                        externalChange = false,
                        dirty = false,
                        restoredDraft = false
                    )
                }
            } catch (e: FileAccessException) {
                _uiState.update {
                    it.copy(externalChange = false, loadError = e.kind)
                }
            }
        }
    }

    fun keepMineDespiteExternalChange() {
        // Stop warning for this session; the next save overwrites.
        fingerprint = null
        _uiState.update { it.copy(externalChange = false) }
    }

    class Factory(
        private val files: FileRepository,
        private val drafts: DraftStore,
        private val settings: SettingsRepository,
        private val uriString: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditorViewModel(files, drafts, settings, uriString) as T
        }
    }

    private companion object {
        const val DRAFT_DEBOUNCE_MS = 800L
    }
}
