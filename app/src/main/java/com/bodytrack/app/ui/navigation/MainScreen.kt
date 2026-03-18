package com.bodytrack.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.bodytrack.app.ui.screen.*
import com.bodytrack.app.viewmodel.BodyRecordViewModel
import com.bodytrack.app.viewmodel.SettingsViewModel

/**
 * 主界面（包含底部导航）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BodyRecordViewModel,
    settingsViewModel: SettingsViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val screens = Screen.items

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> TodayScreen(
                    viewModel = viewModel,
                    uiState = viewModel.uiState.collectAsState().value,
                    onSaveSuccess = {},
                    onSaveError = {}
                )
                1 -> HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToChart = { selectedTab = 2 }
                )
                2 -> ChartScreen(viewModel = viewModel)
                3 -> BmiScreen(viewModel = viewModel)
                4 -> SettingsScreen(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
