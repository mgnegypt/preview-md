package com.mgnegypt.previewmd.ui.about

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.ui.components.RoundIconButton
import com.mgnegypt.previewmd.ui.theme.AccentBlue
import com.mgnegypt.previewmd.ui.theme.AccentGreen
import com.mgnegypt.previewmd.ui.theme.AccentPink
import com.mgnegypt.previewmd.ui.theme.AccentYellow
import kotlin.math.PI
import kotlin.math.sin

/**
 * About (design screen 6): the developer card with the pink avatar
 * circle, the name and role, the four social squares, and the app
 * version. Behind the card's content, the design's accent shapes are
 * alive: they drift slowly and randomly around the header, sliding
 * out from behind the photo and passing behind the social squares —
 * always behind the content, never over it. The avatar is Mahmoud's
 * own photo and the URLs live in [DeveloperInfo].
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val versionName = remember(context) {
        runCatching {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
        }.getOrNull() ?: ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            RoundIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            FloatingShapes(modifier = Modifier.matchParentSize())
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AvatarBlock()
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.about_developer_name),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.about_role),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(28.dp))
                SocialRow()
                Spacer(modifier = Modifier.height(36.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        R.string.settings_version,
                        versionName
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Mahmoud's photo in the design's pink ring. */
@Composable
private fun AvatarBlock() {
    Box(
        modifier = Modifier.size(176.dp),
        contentAlignment = Alignment.Center
    ) {
        // Mahmoud's own photo (supplied 2026-11-19), in the design's
        // pink circle as a ring around it.
        Image(
            painter = painterResource(R.drawable.developer_avatar),
            contentDescription = stringResource(
                R.string.about_developer_name
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .border(5.dp, AccentPink, CircleShape)
        )
    }
}

/**
 * One wandering accent shape. [homeX]/[homeY] are its resting centre
 * as fractions of the layer, the amplitudes how far it roams (also
 * fractions), and the frequencies/phases make every path different:
 * layered sines with whole-number frequencies over one shared 90
 * second cycle, so the motion is smooth, endless and never repeats
 * the same route twice in a row.
 */
private data class FloatingShape(
    val color: Color,
    val size: Dp,
    val circle: Boolean,
    val corner: Dp,
    val homeX: Float,
    val homeY: Float,
    val ampX: Float,
    val ampY: Float,
    val freqX1: Int,
    val freqX2: Int,
    val freqY1: Int,
    val freqY2: Int,
    val phaseX: Float,
    val phaseY: Float,
    val baseRotation: Float,
    val rotationAmp: Float,
    val freqR: Int,
    val phaseR: Float
)

private val FloatingShapesSpec = listOf(
    // The design's three shapes, now alive.
    FloatingShape(
        color = AccentYellow, size = 26.dp, circle = true, corner = 0.dp,
        homeX = 0.29f, homeY = 0.05f, ampX = 0.13f, ampY = 0.06f,
        freqX1 = 1, freqX2 = 2, freqY1 = 1, freqY2 = 3,
        phaseX = 0.4f, phaseY = 1.7f,
        baseRotation = 0f, rotationAmp = 0f, freqR = 1, phaseR = 0f
    ),
    FloatingShape(
        color = AccentBlue, size = 24.dp, circle = false, corner = 7.dp,
        homeX = 0.71f, homeY = 0.38f, ampX = 0.11f, ampY = 0.07f,
        freqX1 = 1, freqX2 = 3, freqY1 = 2, freqY2 = 1,
        phaseX = 2.3f, phaseY = 0.6f,
        baseRotation = 18f, rotationAmp = 22f, freqR = 1, phaseR = 1.1f
    ),
    FloatingShape(
        color = AccentGreen, size = 16.dp, circle = false, corner = 5.dp,
        homeX = 0.72f, homeY = 0.05f, ampX = 0.10f, ampY = 0.05f,
        freqX1 = 2, freqX2 = 1, freqY1 = 1, freqY2 = 2,
        phaseX = 4.0f, phaseY = 2.9f,
        baseRotation = 24f, rotationAmp = 26f, freqR = 2, phaseR = 0.3f
    ),
    // More shapes in the same language, spread over the whole card so
    // some slide out from behind the photo and others wander behind
    // the name and the social row.
    FloatingShape(
        color = AccentPink, size = 18.dp, circle = false, corner = 6.dp,
        homeX = 0.20f, homeY = 0.21f, ampX = 0.14f, ampY = 0.08f,
        freqX1 = 1, freqX2 = 2, freqY1 = 2, freqY2 = 3,
        phaseX = 5.1f, phaseY = 0.9f,
        baseRotation = 12f, rotationAmp = 20f, freqR = 1, phaseR = 2.2f
    ),
    FloatingShape(
        color = AccentYellow, size = 14.dp, circle = false, corner = 5.dp,
        homeX = 0.83f, homeY = 0.24f, ampX = 0.12f, ampY = 0.08f,
        freqX1 = 2, freqX2 = 3, freqY1 = 1, freqY2 = 2,
        phaseX = 1.2f, phaseY = 3.8f,
        baseRotation = 30f, rotationAmp = 24f, freqR = 2, phaseR = 4.4f
    ),
    FloatingShape(
        color = AccentBlue, size = 12.dp, circle = true, corner = 0.dp,
        homeX = 0.08f, homeY = 0.78f, ampX = 0.04f, ampY = 0.05f,
        freqX1 = 1, freqX2 = 3, freqY1 = 2, freqY2 = 1,
        phaseX = 3.3f, phaseY = 5.5f,
        baseRotation = 0f, rotationAmp = 0f, freqR = 1, phaseR = 0f
    ),
    FloatingShape(
        color = AccentPink, size = 20.dp, circle = true, corner = 0.dp,
        homeX = 0.92f, homeY = 0.76f, ampX = 0.04f, ampY = 0.06f,
        freqX1 = 2, freqX2 = 1, freqY1 = 1, freqY2 = 3,
        phaseX = 0.8f, phaseY = 2.0f,
        baseRotation = 0f, rotationAmp = 0f, freqR = 1, phaseR = 0f
    ),
    FloatingShape(
        color = AccentGreen, size = 10.dp, circle = true, corner = 0.dp,
        homeX = 0.15f, homeY = 0.55f, ampX = 0.08f, ampY = 0.06f,
        freqX1 = 1, freqX2 = 2, freqY1 = 3, freqY2 = 1,
        phaseX = 2.7f, phaseY = 4.6f,
        baseRotation = 0f, rotationAmp = 0f, freqR = 1, phaseR = 0f
    )
)

/**
 * The living background layer: every shape's position and rotation
 * are computed from one shared clock inside graphicsLayer, so the
 * animation runs entirely in the draw phase — no recomposition per
 * frame. The layer is the first child of its Box, so the shapes stay
 * behind the photo, its ring and the social squares.
 */
@Composable
private fun FloatingShapes(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "aboutShapes")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 90000,
                easing = LinearEasing
            )
        ),
        label = "aboutShapesTime"
    )
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        FloatingShapesSpec.forEach { shape ->
            val halfPx = with(density) { shape.size.toPx() } / 2f
            Box(
                modifier = Modifier
                    .size(shape.size)
                    .graphicsLayer {
                        val dx = shape.ampX *
                            (0.7f * sin(shape.freqX1 * time + shape.phaseX) +
                                0.3f * sin(shape.freqX2 * time + shape.phaseX * 1.7f))
                        val dy = shape.ampY *
                            (0.7f * sin(shape.freqY1 * time + shape.phaseY) +
                                0.3f * sin(shape.freqY2 * time + shape.phaseY * 1.7f))
                        translationX = (shape.homeX + dx.toFloat()) * widthPx - halfPx
                        translationY = (shape.homeY + dy.toFloat()) * heightPx - halfPx
                        rotationZ = shape.baseRotation +
                            shape.rotationAmp * sin(shape.freqR * time + shape.phaseR).toFloat()
                    }
                    .clip(
                        if (shape.circle) {
                            CircleShape
                        } else {
                            RoundedCornerShape(shape.corner)
                        }
                    )
                    .background(shape.color)
            )
        }
    }
}

@Composable
private fun SocialRow() {
    val uriHandler = LocalUriHandler.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SocialSquare(
            color = AccentBlue,
            iconRes = R.drawable.ic_brand_facebook,
            description = stringResource(R.string.about_facebook),
            onClick = { uriHandler.openUri(DeveloperInfo.FACEBOOK_URL) }
        )
        SocialSquare(
            color = AccentPink,
            iconRes = R.drawable.ic_brand_instagram,
            description = stringResource(R.string.about_instagram),
            onClick = { uriHandler.openUri(DeveloperInfo.INSTAGRAM_URL) }
        )
        SocialSquare(
            color = AccentYellow,
            iconRes = R.drawable.ic_brand_x,
            description = stringResource(R.string.about_x),
            onClick = { uriHandler.openUri(DeveloperInfo.X_URL) }
        )
        SocialSquare(
            color = AccentGreen,
            iconRes = R.drawable.ic_brand_whatsapp,
            description = stringResource(R.string.about_whatsapp),
            onClick = { uriHandler.openUri(DeveloperInfo.WHATSAPP_URL) }
        )
    }
}

@Composable
private fun SocialSquare(
    color: Color,
    iconRes: Int,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = description,
            tint = Color.Unspecified,
            modifier = Modifier.size(28.dp)
        )
    }
}
