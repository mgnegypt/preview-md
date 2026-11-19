package com.mgnegypt.previewmd.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.ui.components.RoundIconButton
import com.mgnegypt.previewmd.ui.theme.AccentBlue
import com.mgnegypt.previewmd.ui.theme.AccentGreen
import com.mgnegypt.previewmd.ui.theme.AccentPink
import com.mgnegypt.previewmd.ui.theme.AccentYellow
import com.mgnegypt.previewmd.ui.theme.Ink

/**
 * About (design screen 6): the developer card with the pink avatar
 * circle and decorative accent shapes, the name and role, the four
 * social squares, and the app version. The avatar is the design's
 * placeholder silhouette and the URLs are placeholders in
 * [DeveloperInfo] until Mahmoud supplies the real ones.
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

/** Pink avatar circle with the design's decorative accent shapes. */
@Composable
private fun AvatarBlock() {
    Box(
        modifier = Modifier.size(176.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(26.dp)
                .clip(CircleShape)
                .background(AccentYellow)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(24.dp)
                .rotate(18f)
                .clip(RoundedCornerShape(7.dp))
                .background(AccentBlue)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp)
                .size(16.dp)
                .rotate(24f)
                .clip(RoundedCornerShape(5.dp))
                .background(AccentGreen)
        )
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
