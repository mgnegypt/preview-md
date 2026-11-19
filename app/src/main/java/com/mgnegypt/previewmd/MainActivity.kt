package com.mgnegypt.previewmd

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.mgnegypt.previewmd.ui.PreviewMdRoot

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PreviewMdApp).container
        setContent {
            PreviewMdRoot(container = container)
        }
    }
}
