package com.mgnegypt.previewmd.data.update

import android.content.Context
import com.mgnegypt.previewmd.data.settings.ReleaseChannel
import com.mgnegypt.previewmd.data.settings.SettingsRepository
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Everything the Settings update card can be showing. */
sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data object UpToDate : UpdateUiState
    data class Available(val info: ReleaseInfo) : UpdateUiState
    data class Downloading(
        val info: ReleaseInfo,
        val fraction: Float
    ) : UpdateUiState

    data class Verifying(val info: ReleaseInfo) : UpdateUiState
    data class ReadyToInstall(
        val info: ReleaseInfo,
        val file: File
    ) : UpdateUiState

    data object Installing : UpdateUiState
    data object Installed : UpdateUiState
    data class Error(val kind: UpdateErrorKind) : UpdateUiState
}

/**
 * App-scoped update coordinator: manual checks from Settings and the
 * automatic check on start (at most once per 24 hours, and only when
 * the user left automatic updates on) share one state machine. The
 * system always confirms installs; nothing here installs silently.
 */
class UpdateManager(
    context: Context,
    private val repository: UpdateRepository,
    private val installer: UpdateInstaller,
    private val settings: SettingsRepository
) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private val _state = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    private var busy = false

    init {
        scope.launch {
            InstallEvents.events.collect { event ->
                when (event) {
                    InstallEvent.Success ->
                        _state.value = UpdateUiState.Installed

                    is InstallEvent.Failed ->
                        _state.value = UpdateUiState.Error(
                            UpdateErrorKind.INSTALL_FAILED
                        )

                    InstallEvent.ConfirmationLaunched -> Unit
                }
            }
        }
    }

    fun canInstall(context: Context): Boolean =
        installer.canInstall(context)

    fun currentVersion(): SemVer? = try {
        appContext.packageManager
            .getPackageInfo(appContext.packageName, 0)
            .versionName
            ?.let { SemVer.parse(it) }
    } catch (e: Exception) {
        null
    }

    /** The start-of-app automatic check, throttled to once per day. */
    fun autoCheckIfDue() {
        scope.launch {
            val prefs = settings.settings.first()
            if (!prefs.autoUpdate) return@launch
            val now = System.currentTimeMillis()
            if (now - prefs.lastUpdateCheckAt < DAY_MS) return@launch
            check(manual = false)
        }
    }

    fun check(manual: Boolean) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                _state.value = UpdateUiState.Checking
                settings.setLastUpdateCheckAt(System.currentTimeMillis())
                val prefs = settings.settings.first()
                val channel = if (prefs.channel == ReleaseChannel.BETA) {
                    UpdateChannel.BETA
                } else {
                    UpdateChannel.STABLE
                }
                val candidate = repository.fetchCandidate(channel)
                val current = currentVersion()
                _state.value = when {
                    candidate == null -> UpdateUiState.UpToDate
                    current != null &&
                        !candidate.version.isNewerThan(current) ->
                        UpdateUiState.UpToDate

                    else -> UpdateUiState.Available(candidate)
                }
            } catch (e: UpdateException) {
                _state.value = if (manual) {
                    UpdateUiState.Error(e.kind)
                } else {
                    UpdateUiState.Idle
                }
            } finally {
                busy = false
            }
        }
    }

    fun download(info: ReleaseInfo) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                repository.downloadAndVerify(info).collect { progress ->
                    _state.value = when (progress) {
                        is UpdateProgress.Downloading ->
                            UpdateUiState.Downloading(
                                info,
                                progress.fraction
                            )

                        UpdateProgress.Verifying ->
                            UpdateUiState.Verifying(info)

                        is UpdateProgress.Ready ->
                            UpdateUiState.ReadyToInstall(info, progress.file)
                    }
                }
            } catch (e: UpdateException) {
                _state.value = UpdateUiState.Error(e.kind)
            } finally {
                busy = false
            }
        }
    }

    fun install(file: File) {
        installer.install(appContext, file)
        _state.value = UpdateUiState.Installing
    }

    private companion object {
        const val DAY_MS = 24L * 60 * 60 * 1000
    }
}
