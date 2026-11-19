package com.mgnegypt.previewmd.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mgnegypt.previewmd.ui.theme.AccentPink
import com.mgnegypt.previewmd.ui.theme.Cream
import com.mgnegypt.previewmd.ui.theme.DeepBlack
import com.mgnegypt.previewmd.ui.theme.Ink
import com.mgnegypt.previewmd.ui.theme.PillShape
import com.mgnegypt.previewmd.ui.theme.PreviewMDTheme

/**
 * The design's floating bottom bar: a deep-black pill with four icon slots
 * and a raised pink center action. Slots are Home, Files, Theme and About;
 * the center button is an action (open a file), not a destination.
 */
@Composable
fun FloatingBottomBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onCenterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(PillShape)
            .background(DeepBlack)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BarItem(
            icon = Icons.Filled.Home,
            selected = selectedIndex == 0,
            onClick = { onItemSelected(0) }
        )
        BarItem(
            icon = Icons.Outlined.Description,
            selected = selectedIndex == 1,
            onClick = { onItemSelected(1) }
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(AccentPink)
                .clickable(onClick = onCenterClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Ink,
                modifier = Modifier.size(28.dp)
            )
        }
        BarItem(
            icon = Icons.Filled.LightMode,
            selected = selectedIndex == 2,
            onClick = { onItemSelected(2) }
        )
        BarItem(
            icon = Icons.Filled.Person,
            selected = selectedIndex == 3,
            onClick = { onItemSelected(3) }
        )
    }
}

@Composable
private fun BarItem(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Cream else Cream.copy(alpha = 0.45f),
            modifier = Modifier.size(26.dp)
        )
    }
}

@Preview(name = "BottomBar Light", showBackground = true)
@Composable
private fun BottomBarLightPreview() {
    PreviewMDTheme(darkTheme = false) {
        Box(Modifier.padding(16.dp)) {
            FloatingBottomBar(
                selectedIndex = 0,
                onItemSelected = {},
                onCenterClick = {}
            )
        }
    }
}

@Preview(
    name = "BottomBar Dark RTL",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ar"
)
@Composable
private fun BottomBarDarkRtlPreview() {
    PreviewMDTheme(darkTheme = true) {
        Box(Modifier.padding(16.dp)) {
            FloatingBottomBar(
                selectedIndex = 3,
                onItemSelected = {},
                onCenterClick = {}
            )
        }
    }
}
