package com.bodytrack.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
 * 图表界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(viewModel: BodyRecordViewModel) {
    val chartRecords by viewModel.chartRecords.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("体重趋势", "身高趋势", "BMI趋势")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 标签选择
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        if (chartRecords.isEmpty()) {
            EmptyChartView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 统计摘要
                ChartSummaryCard(chartRecords)

                Spacer(modifier = Modifier.height(16.dp))

                // 图表区域
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = tabs[selectedTab],
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 使用简单的折线图表示
                        when (selectedTab) {
                            0 -> SimpleLineChart(
                                data = chartRecords.map { it.weight },
                                labels = chartRecords.map { formatDateShort(it.date) },
                                color = MaterialTheme.colorScheme.primary,
                                unit = "kg"
                            )
                            1 -> SimpleLineChart(
                                data = chartRecords.map { it.height },
                                labels = chartRecords.map { formatDateShort(it.date) },
                                color = MaterialTheme.colorScheme.secondary,
                                unit = "cm"
                            )
                            2 -> SimpleLineChart(
                                data = chartRecords.map { it.bmi },
                                labels = chartRecords.map { formatDateShort(it.date) },
                                color = BmiNormal,
                                unit = ""
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 数据列表
                Text(
                    text = "详细数据",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 数据表格
                DataTable(chartRecords, selectedTab)
            }
        }
    }
}

@Composable
private fun ChartSummaryCard(records: List<BodyRecord>) {
    if (records.size < 2) return

    val firstRecord = records.first()
    val lastRecord = records.last()
    val weightDiff = lastRecord.weight - firstRecord.weight

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📈 变化趋势",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "起始体重",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${firstRecord.weight} kg",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Column {
                    Text(
                        text = "当前体重",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${lastRecord.weight} kg",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "变化",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${if (weightDiff >= 0) "+" else ""}${String.format("%.1f", weightDiff)} kg",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (weightDiff > 0) BmiObese else if (weightDiff < 0) BmiNormal else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleLineChart(
    data: List<Float>,
    labels: List<String>,
    color: androidx.compose.ui.graphics.Color,
    unit: String
) {
    if (data.isEmpty()) {
        Text(
            text = "暂无数据",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        return
    }

    val minValue = data.min()
    val maxValue = data.max()
    val range = if (maxValue - minValue > 0) maxValue - minValue else 1f

    Column {
        // 图表区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // 这里使用简单的文本表示，实际应用中可以使用 Vico 或 MPAndroidChart
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Y轴标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format("%.1f", maxValue)}$unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${String.format("%.1f", minValue)}$unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // 数据点 - 使用简单的卡片表示
                data.forEachIndexed { index, value ->
                    val normalizedHeight = ((value - minValue) / range * 150).dp

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // X轴标签
                        if (index < labels.size) {
                            Text(
                                text = labels[index],
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.width(40.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // 数据条
                        LinearProgressIndicator(
                            progress = { (value - minValue) / range },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp),
                            color = color,
                            trackColor = color.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = String.format("%.1f", value),
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                    }

                    if (index < data.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DataTable(records: List<BodyRecord>, selectedTab: Int) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 表头
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("日期", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                Text("身高", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("体重", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("BMI", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 数据行
            records.take(10).forEach { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDateShort(record.date),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${record.height}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${record.weight}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = String.format("%.1f", record.bmi),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        color = getBmiColor(record.bmi)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyChartView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShowChart,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "暂无数据",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "记录更多数据后，这里将显示趋势图表",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

private fun formatDateShort(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.format(DateTimeFormatter.ofPattern("MM/dd"))
    } catch (e: Exception) {
        dateString
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
