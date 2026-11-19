package com.mgnegypt.previewmd.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.ui.components.PillButtonPrimary
import com.mgnegypt.previewmd.ui.components.PillButtonSecondary
import com.mgnegypt.previewmd.ui.theme.AccentBlue
import com.mgnegypt.previewmd.ui.theme.AccentGreen
import com.mgnegypt.previewmd.ui.theme.AccentPink
import com.mgnegypt.previewmd.ui.theme.AccentYellow
import com.mgnegypt.previewmd.ui.theme.Ink

/**
 * First-launch welcome, exactly as drawn: the four accent shapes, the
 * headline, and the two pill actions. Both actions finish onboarding;
 * "Open a file" goes through the file picker from Phase 3 on.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onOpenFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        WelcomeShapes(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
        )
        Text(
            text = stringResource(R.string.welcome_headline),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = stringResource(R.string.welcome_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 14.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PillButtonPrimary(
                text = stringResource(R.string.welcome_get_started),
                onClick = onGetStarted,
                trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight
            )
            PillButtonSecondary(
                text = stringResource(R.string.welcome_open_file),
                onClick = onOpenFile
            )
        }
    }
}

@Composable
private fun WelcomeShapes(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // Blue circle, top start.
        Box(
            modifier = Modifier
                .size(168.dp)
                .align(Alignment.TopStart)
                .offset(x = 6.dp, y = 6.dp)
                .clip(CircleShape)
                .background(AccentBlue)
        )
        // Yellow rounded square, top end, tilted.
        Box(
            modifier = Modifier
                .size(148.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 10.dp)
                .rotate(10f)
                .clip(RoundedCornerShape(38.dp))
                .background(AccentYellow)
        )
        // Pink rounded square with the MD mark, center.
        Box(
            modifier = Modifier
                .size(138.dp)
                .align(Alignment.Center)
                .offset(x = (-16).dp, y = 26.dp)
                .rotate(-8f)
                .clip(RoundedCornerShape(36.dp))
                .background(AccentPink),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MD",
                color = Ink,
                fontWeight = FontWeight.Black,
                fontSize = 34.sp
            )
        }
        // Green leaf, bottom end.
        Box(
            modifier = Modifier
                .size(104.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-18).dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 52.dp,
                        topEnd = 52.dp,
                        bottomEnd = 52.dp,
                        bottomStart = 6.dp
                    )
                )
                .background(AccentGreen)
        )
    }
}
