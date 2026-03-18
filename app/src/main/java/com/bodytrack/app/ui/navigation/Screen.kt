package com.bodytrack.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 导航路由定义
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Today : Screen("today", "今日", Icons.Default.Today)
    object History : Screen("history", "历史", Icons.Default.History)
    object Chart : Screen("chart", "图表", Icons.Default.ShowChart)
    object Bmi : Screen("bmi", "BMI", Icons.Default.FitnessCenter)
    object Settings : Screen("settings", "设置", Icons.Default.Settings)

    companion object {
        val items = listOf(Today, History, Chart, Bmi, Settings)
    }
}
