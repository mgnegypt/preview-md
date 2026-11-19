package com.mgnegypt.previewmd.ui.shell

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mgnegypt.previewmd.AppContainer
import com.mgnegypt.previewmd.R
import com.mgnegypt.previewmd.ui.components.FloatingBottomBar
import com.mgnegypt.previewmd.ui.home.HomeScreen
import com.mgnegypt.previewmd.ui.home.HomeViewModel
import com.mgnegypt.previewmd.ui.navigation.AboutRoute
import com.mgnegypt.previewmd.ui.navigation.EditorRoute
import com.mgnegypt.previewmd.ui.navigation.HomeRoute
import com.mgnegypt.previewmd.ui.navigation.SettingsRoute
import com.mgnegypt.previewmd.ui.navigation.ViewerRoute
import com.mgnegypt.previewmd.ui.settings.SettingsScreen
import com.mgnegypt.previewmd.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch

/**
 * The navigation shell: hosts the pickers (open document, open tree,
 * create document), the + action sheet, the floating bottom bar and the
 * NavHost. Viewer, Editor and About still show placeholders until their
 * phases (4, 5 and 7) land.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch { container.intakeOpenedFile(uri) }
            navController.navigate(ViewerRoute(uri = uri.toString())) {
                launchSingleTop = true
            }
        }
    }
    val openTreeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            scope.launch {
                container.fileRepository.takePersistablePermission(treeUri)
                val name = container.fileRepository.treeDisplayName(treeUri)
                container.recentFilesRepository.addFolder(
                    uri = treeUri.toString(),
                    name = name
                )
            }
        }
    }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri ->
        if (uri != null) {
            scope.launch { container.intakeOpenedFile(uri) }
            navController.navigate(ViewerRoute(uri = uri.toString())) {
                launchSingleTop = true
            }
        }
    }

    // Files opened from outside the graph (Welcome, VIEW/SEND intents)
    // arrive through the container and are shown in the viewer.
    val pendingOpen by container.pendingOpen
        .collectAsStateWithLifecycle()
    LaunchedEffect(pendingOpen) {
        val uri = pendingOpen ?: return@LaunchedEffect
        container.consumePendingOpen()
        scope.launch {
            container.intakeOpenedFile(Uri.parse(uri))
        }
        navController.navigate(ViewerRoute(uri = uri)) {
            launchSingleTop = true
        }
    }

    val selectedIndex = when {
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
                            1 -> openDocumentLauncher.launch(
                                arrayOf("text/markdown", "text/plain", "*/*")
                            )
                            2 -> navController.navigate(SettingsRoute) {
                                launchSingleTop = true
                            }
                            3 -> navController.navigate(AboutRoute) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onCenterClick = { showSheet = true },
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
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        recentFiles = container.recentFilesRepository,
                        files = container.fileRepository,
                        settings = container.settingsRepository
                    )
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onOpenFile = { uri ->
                        navController.navigate(
                            ViewerRoute(uri = uri.toString())
                        ) { launchSingleTop = true }
                    },
                    onOpenFileRequest = {
                        openDocumentLauncher.launch(
                            arrayOf("text/markdown", "text/plain", "*/*")
                        )
                    },
                    onAddFolderRequest = {
                        openTreeLauncher.launch(null)
                    }
                )
            }
            composable<ViewerRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<ViewerRoute>()
                val viewerViewModel: com.mgnegypt.previewmd.ui.viewer.ViewerViewModel =
                    viewModel(
                        key = "viewer-${route.uri}",
                        factory = com.mgnegypt.previewmd.ui.viewer.ViewerViewModel.Factory(
                            files = container.fileRepository,
                            uriString = route.uri
                        )
                    )
                com.mgnegypt.previewmd.ui.viewer.ViewerScreen(
                    viewModel = viewerViewModel,
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        navController.navigate(EditorRoute(uri = route.uri)) {
                            launchSingleTop = true
                        }
                    },
                    onOpenLinkedDocument = { uriString ->
                        scope.launch {
                            container.intakeOpenedFile(Uri.parse(uriString))
                        }
                        navController.navigate(ViewerRoute(uri = uriString)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable<EditorRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<EditorRoute>()
                val editorViewModel: com.mgnegypt.previewmd.ui.editor.EditorViewModel =
                    viewModel(
                        key = "editor-${route.uri}",
                        factory = com.mgnegypt.previewmd.ui.editor.EditorViewModel.Factory(
                            files = container.fileRepository,
                            drafts = container.draftStore,
                            uriString = route.uri
                        )
                    )
                com.mgnegypt.previewmd.ui.editor.EditorScreen(
                    viewModel = editorViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable<SettingsRoute> {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        container.settingsRepository
                    )
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

    if (showSheet) {
        val sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden
        )
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp)
            ) {
                SheetAction(
                    icon = Icons.Filled.NoteAdd,
                    label = stringResource(R.string.sheet_new_file),
                    onClick = {
                        showSheet = false
                        createDocumentLauncher.launch("untitled.md")
                    }
                )
                SheetAction(
                    icon = Icons.Filled.FolderOpen,
                    label = stringResource(R.string.sheet_open_file),
                    onClick = {
                        showSheet = false
                        openDocumentLauncher.launch(
                            arrayOf("text/markdown", "text/plain", "*/*")
                        )
                    }
                )
                SheetAction(
                    icon = Icons.Filled.CreateNewFolder,
                    label = stringResource(R.string.sheet_add_folder),
                    onClick = {
                        showSheet = false
                        openTreeLauncher.launch(null)
                    }
                )
            }
        }
    }
}

@Composable
private fun SheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp)
        )
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
