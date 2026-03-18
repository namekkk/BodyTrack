package com.bodytrack.app.ui.screen

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.bodytrack.app.data.local.BodyRecord
import com.bodytrack.app.viewmodel.BodyRecordViewModel
import com.bodytrack.app.viewmodel.SettingsViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 设置界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BodyRecordViewModel,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current

    val reminderEnabled by settingsViewModel.reminderEnabled.collectAsState()
    val reminderTimeString by settingsViewModel.reminderTimeString.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()

    var showTimePicker by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showExportSuccess by remember { mutableStateOf(false) }
    var showExportError by remember { mutableStateOf<String?>(null) }
    var showRestoreSuccess by remember { mutableStateOf(false) }
    var showRestoreError by remember { mutableStateOf<String?>(null) }

    // 导出CSV启动器
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { os ->
                    val csv = generateCsv(allRecords)
                    os.write(csv.toByteArray())
                    showExportSuccess = true
                }
            } catch (e: Exception) {
                showExportError = e.message
            }
        }
    }

    // 备份JSON启动器
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { os ->
                    val json = Gson().toJson(allRecords)
                    os.write(json.toByteArray())
                    showExportSuccess = true
                }
            } catch (e: Exception) {
                showExportError = e.message
            }
        }
    }

    // 恢复JSON启动器
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { is_ ->
                    val json = is_.bufferedReader().readText()
                    val type = object : TypeToken<List<BodyRecord>>() {}.type
                    val records: List<BodyRecord> = Gson().fromJson(json, type)
                    viewModel.restoreRecords(
                        records = records,
                        onSuccess = { showRestoreSuccess = true },
                        onError = { error -> showRestoreError = error }
                    )
                }
            } catch (e: Exception) {
                showRestoreError = e.message
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 提醒设置
        SettingsSectionTitle("⏰ 提醒设置")

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 提醒开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "每日提醒",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "提醒记录身高体重",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { settingsViewModel.toggleReminder(it) }
                    )
                }

                if (reminderEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // 提醒时间
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "提醒时间",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = reminderTimeString,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        OutlinedButton(
                            onClick = { showTimePicker = true }
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("修改")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 数据导出
        SettingsSectionTitle("📤 数据导出")

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 导出CSV
                SettingsItem(
                    icon = Icons.Default.TableChart,
                    title = "导出为 CSV",
                    description = "导出数据为Excel可打开的CSV文件",
                    enabled = allRecords.isNotEmpty()
                ) {
                    csvExportLauncher.launch("body_track_${System.currentTimeMillis()}.csv")
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // 备份数据
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "备份数据",
                    description = "将所有数据备份为JSON文件",
                    enabled = allRecords.isNotEmpty()
                ) {
                    backupLauncher.launch("body_track_backup_${System.currentTimeMillis()}.json")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 数据恢复
        SettingsSectionTitle("📥 数据恢复")

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 恢复数据
                SettingsItem(
                    icon = Icons.Default.Restore,
                    title = "从备份恢复",
                    description = "从JSON备份文件恢复数据"
                ) {
                    restoreLauncher.launch(arrayOf("application/json"))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 数据管理
        SettingsSectionTitle("🗑️ 数据管理")

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SettingsItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "清空所有数据",
                    description = "删除所有记录（不可恢复）",
                    enabled = allRecords.isNotEmpty(),
                    isDestructive = true
                ) {
                    showClearDialog = true
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 关于
        SettingsSectionTitle("ℹ️ 关于")

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "体态追踪",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "版本 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "每日记录身高体重，追踪身体变化",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }

    // 时间选择对话框
    if (showTimePicker) {
        val currentTime = LocalTime.now()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                settingsViewModel.setReminderTime(hour, minute)
                showTimePicker = false
            },
            currentTime.hour,
            currentTime.minute,
            true
        ).apply {
            setOnDismissListener { showTimePicker = false }
            show()
        }
    }

    // 清空确认对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确认清空") },
            text = { Text("确定要删除所有记录吗？此操作不可恢复！") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllRecords(
                            onSuccess = { showClearDialog = false },
                            onError = { showClearDialog = false }
                        )
                    }
                ) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 成功/错误提示
    if (showExportSuccess) {
        LaunchedEffect(Unit) {
            showExportSuccess = false
        }
        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("操作成功")
        }
    }

    showExportError?.let { error ->
        AlertDialog(
            onDismissRequest = { showExportError = null },
            title = { Text("错误") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { showExportError = null }) {
                    Text("确定")
                }
            }
        )
    }

    if (showRestoreSuccess) {
        LaunchedEffect(Unit) {
            showRestoreSuccess = false
        }
        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("数据恢复成功")
        }
    }

    showRestoreError?.let { error ->
        AlertDialog(
            onDismissRequest = { showRestoreError = null },
            title = { Text("恢复失败") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { showRestoreError = null }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error
            else if (enabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }

        OutlinedButton(
            onClick = onClick,
            enabled = enabled
        ) {
            Text(if (isDestructive) "清空" else "操作")
        }
    }
}

private fun generateCsv(records: List<BodyRecord>): String {
    val header = "日期,身高(cm),体重(kg),BMI,备注\n"
    val rows = records.joinToString("\n") { record ->
        "${record.date},${record.height},${record.weight},${String.format("%.1f", record.bmi)},\"${record.note}\""
    }
    return header + rows
}
