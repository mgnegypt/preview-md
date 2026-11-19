package com.mgnegypt.previewmd

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.mgnegypt.previewmd.ui.gallery.DesignGalleryScreen
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreviewMDTheme {
                Scaffold { innerPadding ->
                    // Phase 1 shows the design-system gallery. Phase 2
                    // replaces this with the navigation shell.
                    DesignGalleryScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
