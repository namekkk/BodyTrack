package com.bodytrack.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bodytrack.app.data.local.BodyRecord
import com.bodytrack.app.ui.theme.*
import com.bodytrack.app.viewmodel.BodyRecordUiState
import com.bodytrack.app.viewmodel.BodyRecordViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * 今日记录界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: BodyRecordViewModel,
    uiState: BodyRecordUiState,
    onSaveSuccess: () -> Unit,
    onSaveError: (String) -> Unit
) {
    val todayRecord by viewModel.todayRecord.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // 日期卡片
        DateCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 今日已有记录显示
        todayRecord?.let { record ->
            TodayRecordCard(record)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 输入卡片
        InputCard(
            uiState = uiState,
            onHeightChange = viewModel::updateHeightInput,
            onWeightChange = viewModel::updateWeightInput,
            onSave = {
                viewModel.saveRecord(
                    onSuccess = onSaveSuccess,
                    onError = onSaveError
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BMI预览
        uiState.previewBmi?.let { bmi ->
            BmiPreviewCard(bmi)
        }
    }
}

@Composable
private fun DateCard() {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    val dayOfWeek = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = today.format(formatter),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TodayRecordCard(record: BodyRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📊 今日记录",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DataItem(
                    icon = Icons.Default.Height,
                    label = "身高",
                    value = "${record.height} cm",
                    color = MaterialTheme.colorScheme.primary
                )

                DataItem(
                    icon = Icons.Default.MonitorWeight,
                    label = "体重",
                    value = "${record.weight} kg",
                    color = MaterialTheme.colorScheme.secondary
                )

                DataItem(
                    icon = Icons.Default.FitnessCenter,
                    label = "BMI",
                    value = String.format("%.1f", record.bmi),
                    color = getBmiColor(record.bmi)
                )
            }
        }
    }
}

@Composable
private fun DataItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputCard(
    uiState: BodyRecordUiState,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "✏️ 记录数据",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 身高输入
            OutlinedTextField(
                value = uiState.heightInput,
                onValueChange = onHeightChange,
                label = { Text("身高 (cm)") },
                leadingIcon = {
                    Icon(Icons.Default.Height, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 体重输入
            OutlinedTextField(
                value = uiState.weightInput,
                onValueChange = onWeightChange,
                label = { Text("体重 (kg)") },
                leadingIcon = {
                    Icon(Icons.Default.MonitorWeight, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 保存按钮
            Button(
                onClick = onSave,
                enabled = !uiState.isLoading &&
                        uiState.heightInput.isNotEmpty() &&
                        uiState.weightInput.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存记录")
                }
            }
        }
    }
}

@Composable
private fun BmiPreviewCard(bmi: Float) {
    val category = BodyRecord.getBmiCategory(bmi)
    val color = getBmiColor(bmi)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "BMI 预览",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = String.format("%.1f", bmi),
                    style = MaterialTheme.typography.headlineMedium,
                    color = color
                )
            }

            Surface(
                color = color,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = category.label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
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
