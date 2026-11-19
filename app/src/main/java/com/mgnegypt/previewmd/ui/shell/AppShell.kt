package com.mgnegypt.previewmd.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mgnegypt.previewmd.AppContainer
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.ui.components.FloatingBottomBar
import com.mgnegypt.previewmd.ui.navigation.AboutRoute
import com.mgnegypt.previewmd.ui.navigation.EditorRoute
import com.mgnegypt.previewmd.ui.navigation.HomeRoute
import com.mgnegypt.previewmd.ui.navigation.SettingsRoute
import com.mgnegypt.previewmd.ui.navigation.ViewerRoute
import com.mgnegypt.previewmd.ui.settings.SettingsScreen
import com.mgnegypt.previewmd.ui.settings.SettingsViewModel

/**
 * The navigation shell. Destinations whose phases have not landed yet show
 * a simple titled placeholder; each is replaced by its real screen in its
 * own phase (Home in Phase 3, Viewer in 4, Editor in 5, About in 7). The
 * floating bottom bar is shown on Home and Viewer, as drawn; the bar's
 * slots are Home, Viewer, Settings and About around the center action.
 */
@Composable
fun AppShell(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val selectedIndex = when {
        currentRoute == HomeRoute::class.qualifiedName -> 0
        currentRoute == ViewerRoute::class.qualifiedName -> 1
        currentRoute == SettingsRoute::class.qualifiedName -> 2
        currentRoute == AboutRoute::class.qualifiedName -> 3
        else -> 0
    }
    val showBar = currentRoute == HomeRoute::class.qualifiedName ||
        currentRoute == ViewerRoute::class.qualifiedName

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBar) {
                FloatingBottomBar(
                    selectedIndex = selectedIndex,
                    onItemSelected = { index ->
                        when (index) {
                            0 -> navController.navigate(HomeRoute) {
                                popUpTo(HomeRoute) { inclusive = true }
                            }
                            1 -> navController.navigate(ViewerRoute(uri = "")) {
                                launchSingleTop = true
                            }
                            2 -> navController.navigate(SettingsRoute) {
                                launchSingleTop = true
                            }
                            3 -> navController.navigate(AboutRoute) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onCenterClick = {
                        // The file picker lands in Phase 3; until then the
                        // center action opens the viewer placeholder.
                        navController.navigate(ViewerRoute(uri = "")) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 16.dp
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeRoute> {
                PlaceholderScreen(title = stringResource(R.string.home_title))
            }
            composable<ViewerRoute> {
                PlaceholderScreen(title = stringResource(R.string.viewer_title))
            }
            composable<EditorRoute> {
                PlaceholderScreen(title = stringResource(R.string.editor_title))
            }
            composable<SettingsRoute> {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(container.settingsRepository)
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable<AboutRoute> {
                PlaceholderScreen(title = stringResource(R.string.about_title))
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
