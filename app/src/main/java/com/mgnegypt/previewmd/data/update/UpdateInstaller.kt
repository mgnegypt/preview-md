package com.mgnegypt.previewmd.data.update

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.core.content.IntentCompat
import java.io.File
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Events reported by [InstallResultReceiver] from the installer. */
sealed interface InstallEvent {
    /** The system confirmation UI was launched for the user. */
    data object ConfirmationLaunched : InstallEvent

    data object Success : InstallEvent

    data class Failed(val message: String?) : InstallEvent
}

object InstallEvents {
    private val _events = MutableSharedFlow<InstallEvent>(
        extraBufferCapacity = 4
    )
    val events: SharedFlow<InstallEvent> = _events.asSharedFlow()

    fun publish(event: InstallEvent) {
        _events.tryEmit(event)
    }
}

/**
 * Receives the PackageInstaller session result. On
 * STATUS_PENDING_USER_ACTION it launches the system confirmation
 * intent; the final status is forwarded to [InstallEvents].
 */
class InstallResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (
            val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        ) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = IntentCompat.getParcelableExtra(
                    intent,
                    Intent.EXTRA_INTENT,
                    Intent::class.java
                )
                if (confirm != null) {
                    confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirm)
                }
                InstallEvents.publish(InstallEvent.ConfirmationLaunched)
            }

            PackageInstaller.STATUS_SUCCESS ->
                InstallEvents.publish(InstallEvent.Success)

            else -> InstallEvents.publish(
                InstallEvent.Failed(
                    intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                        ?: "status $status"
                )
            )
        }
    }
}

/**
 * Installs a downloaded, checksum-verified APK through a
 * PackageInstaller session. The system always shows its own install
 * confirmation; nothing is installed silently.
 */
class UpdateInstaller {

    fun canInstall(context: Context): Boolean =
        context.packageManager.canRequestPackageInstalls()

    fun install(context: Context, apkFile: File) {
        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(
            PackageInstaller.SessionParams.MODE_FULL_INSTALL
        )
        val sessionId = packageInstaller.createSession(params)
        packageInstaller.openSession(sessionId).use { session ->
            apkFile.inputStream().use { input ->
                session.openWrite(SESSION_WRITE_NAME, 0, apkFile.length())
                    .use { output ->
                        input.copyTo(output)
                        session.fsync(output)
                    }
            }
            val resultIntent = Intent(
                context,
                InstallResultReceiver::class.java
            ).setAction(ACTION_INSTALL_RESULT)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            session.commit(pendingIntent.intentSender)
        }
    }

    companion object {
        const val ACTION_INSTALL_RESULT =
            "com.mgnegypt.previewmd.INSTALL_RESULT"
        private const val SESSION_WRITE_NAME = "preview-md-update"
    }
}
