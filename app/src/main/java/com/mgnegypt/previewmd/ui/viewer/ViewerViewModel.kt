package com.mgnegypt.previewmd.ui.viewer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mgnegypt.previewmd.data.files.FileAccessException
import com.mgnegypt.previewmd.data.files.FileErrorKind
import com.mgnegypt.previewmd.data.files.FileRepository
import com.mgnegypt.previewmd.data.files.findRelativeImageLinks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ViewerUiState {
    data object Loading : ViewerUiState

    data class Content(
        val fileName: String,
        val markdown: String,
        /** Relative image link -> resolved document URI string. */
        val imageLinks: Map<String, String>
    ) : ViewerUiState

    data class Error(val kind: FileErrorKind) : ViewerUiState
}

class ViewerViewModel(
    private val files: FileRepository,
    uriString: String
) : ViewModel() {

    private val uri: Uri = Uri.parse(uriString)

    private val _state = MutableStateFlow<ViewerUiState>(ViewerUiState.Loading)
    val state: StateFlow<ViewerUiState> = _state.asStateFlow()

    private var hasLoaded = false

    init {
        load()
    }

    fun load() {
        _state.value = ViewerUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                buildContent().also { hasLoaded = true }
            } catch (e: FileAccessException) {
                ViewerUiState.Error(e.kind)
            }
        }
    }

    /**
     * Silent reload when the viewer returns to the foreground, so edits
     * made in the editor (or by another app) show without a spinner.
     */
    fun refresh() {
        if (!hasLoaded) return
        viewModelScope.launch {
            try {
                _state.value = buildContent()
            } catch (e: FileAccessException) {
                _state.value = ViewerUiState.Error(e.kind)
            }
        }
    }

    private suspend fun buildContent(): ViewerUiState.Content {
        val text = files.readText(uri)
        val resolved = findRelativeImageLinks(text).mapNotNull { link ->
            files.resolveSibling(uri, link)?.let { link to it }
        }.toMap()
        return ViewerUiState.Content(
            fileName = files.displayName(uri),
            markdown = text,
            imageLinks = resolved
        )
    }

    /** Resolves a clicked relative link to a document URI, if reachable. */
    suspend fun resolveLink(link: String): String? =
        files.resolveSibling(uri, link)

    class Factory(
        private val files: FileRepository,
        private val uriString: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewerViewModel(files, uriString) as T
        }
    }
}
