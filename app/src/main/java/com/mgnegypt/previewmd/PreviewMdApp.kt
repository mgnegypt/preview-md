package com.mgnegypt.previewmd

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PreviewMdApp : Application(), SingletonImageLoader.Factory {

    /** Coil 3 image loader: network fetching plus SVG (shields.io badges). */
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
                add(SvgDecoder.Factory())
            }
            .build()
    }
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applyStoredLanguage()
    }

    /**
     * Applies the language the user picked in Settings, if any. On API 33+
     * AppCompat delegates to the system per-app language storage; below
     * that, autoStoreLocales persists it (see the manifest service entry).
     */
    private fun applyStoredLanguage() {
        CoroutineScope(Dispatchers.Main).launch {
            val tag = container.settingsRepository.settings.first().languageTag
                ?: return@launch
            val current = AppCompatDelegate.getApplicationLocales()
                .get(0)?.toLanguageTag()
            if (current != tag) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(tag)
                )
            }
        }
    }
}
