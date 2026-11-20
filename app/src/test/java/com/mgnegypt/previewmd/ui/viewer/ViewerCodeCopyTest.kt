package com.mgnegypt.previewmd.ui.viewer

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A fenced code block in the viewer carries the copy button, and
 * tapping it puts the block's code on the clipboard.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h1400dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ViewerCodeCopyTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun copyButtonCopiesTheFenceCode() {
        composeRule.setContent {
            PreviewMDTheme {
                Markdown(
                    content = "```bash\necho hello\n```",
                    colors = viewerMarkdownColors(),
                    typography = viewerMarkdownTypography(),
                    components = viewerMarkdownComponents(emptyMap()),
                    imageTransformer = Coil3ImageTransformerImpl
                )
            }
        }

        composeRule.onNodeWithContentDescription("Copy code")
            .assertIsDisplayed()
            .performClick()

        val clipboard = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val copied = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        assertEquals("echo hello", copied?.trim())
    }
}
