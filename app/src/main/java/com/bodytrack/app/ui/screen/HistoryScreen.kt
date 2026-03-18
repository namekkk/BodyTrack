package com.bodytrack.app.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bodytrack.app.data.local.BodyRecord
import com.bodytrack.app.ui.theme.*
import com.bodytrack.app.viewmodel.BodyRecordViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 历史记录界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: BodyRecordViewModel,
    onNavigateToChart: () -> Unit
) {
    val allRecords by viewModel.allRecords.collectAsState()
    val recordCount by viewModel.recordCount.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<BodyRecord?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部统计卡片
        if (recordCount > 0) {
            StatisticsHeader(viewModel)
        }

        // 记录列表
        if (allRecords.isEmpty()) {
            EmptyHistoryView(onNavigateToChart)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = allRecords,
                    key = { it.id }
                ) { record ->
                    RecordItem(
                        record = record,
                        onDeleteClick = {
                            recordToDelete = record
                            showDeleteDialog = true
                        },
                        formatDate = viewModel::formatDate
                    )
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && recordToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                recordToDelete = null
            },
            title = { Text("确认删除") },
            text = { Text("确定要删除 ${recordToDelete?.date} 的记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        recordToDelete?.let { viewModel.deleteRecord(it) }
                        showDeleteDialog = false
                        recordToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        recordToDelete = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun StatisticsHeader(viewModel: BodyRecordViewModel) {
    val minWeight by viewModel.minWeight.collectAsState()
    val maxWeight by viewModel.maxWeight.collectAsState()
    val avgWeight by viewModel.avgWeight.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📊 统计数据",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "最低体重",
                    value = minWeight?.let { "${String.format("%.1f", it)} kg" } ?: "--",
                    color = BmiNormal
                )
                StatItem(
                    label = "平均体重",
                    value = avgWeight?.let { "${String.format("%.1f", it)} kg" } ?: "--",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "最高体重",
                    value = maxWeight?.let { "${String.format("%.1f", it)} kg" } ?: "--",
                    color = BmiOverweight
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordItem(
    record: BodyRecord,
    onDeleteClick: () -> Unit,
    formatDate: (String) -> String
) {
    val bmiColor = getBmiColor(record.bmi)
    val category = BodyRecord.getBmiCategory(record.bmi)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 日期和删除按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(record.date),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 数据行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 身高
                Column {
                    Text(
                        text = "身高",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${record.height} cm",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // 体重
                Column {
                    Text(
                        text = "体重",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${record.weight} kg",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // BMI
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "BMI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", record.bmi),
                            style = MaterialTheme.typography.bodyLarge,
                            color = bmiColor
                        )
                        Surface(
                            color = bmiColor.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = category.label,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = bmiColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryView(onNavigateToChart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "暂无历史记录",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "开始记录你的身高体重数据吧！",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun getBmiColor(bmi: Float): androidx.compose.ui.graphics.Color {
    return when {
        bmi < 18.5f -> BmiUnderweight
        bmi < 24f -> BmiNormal
        bmi < 28f -> BmiOverweight
        else -> BmiObese
    }
}
