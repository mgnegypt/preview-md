package com.mgnegypt.previewmd

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.mgnegypt.previewmd.ui.PreviewMdRoot

class MainActivity : AppCompatActivity() {

    private val container: AppContainer
        get() = (application as PreviewMdApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only a genuinely fresh launch consumes the incoming file intent;
        // recreations (rotation, language change) must not reopen it.
        if (savedInstanceState == null) {
            handleOpenIntent(intent)
        }
        setContent {
            PreviewMdRoot(container = container)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOpenIntent(intent)
    }

    /** Queues a VIEW or SEND file for the shell to intake and display. */
    private fun handleOpenIntent(intent: Intent?) {
        val uri: Uri? = when (intent?.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(
                intent,
                Intent.EXTRA_STREAM,
                Uri::class.java
            )
            else -> null
        }
        if (uri != null) {
            container.requestOpen(uri.toString())
        }
    }
}
