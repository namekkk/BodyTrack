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

/**
 * BMI详情界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiScreen(viewModel: BodyRecordViewModel) {
    val todayRecord by viewModel.todayRecord.collectAsState()
    val minBmi by viewModel.minBmi.collectAsState()
    val maxBmi by viewModel.maxBmi.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 当前BMI卡片
        todayRecord?.let { record ->
            CurrentBmiCard(record.bmi)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // BMI分类说明
        BmiCategoryCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 历史BMI范围
        if (minBmi != null && maxBmi != null) {
            BmiRangeCard(minBmi!!, maxBmi!!)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BMI计算公式说明
        BmiFormulaCard()
    }
}

@Composable
private fun CurrentBmiCard(bmi: Float) {
    val category = BodyRecord.getBmiCategory(bmi)
    val color = getBmiColor(bmi)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "当前 BMI",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = String.format("%.1f", bmi),
                style = MaterialTheme.typography.displayLarge,
                color = color
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = color,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = category.label,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getBmiAdvice(category),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BmiCategoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📋 BMI 分类标准",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            BmiCategoryItem("偏瘦", "< 18.5", BmiUnderweight)
            BmiCategoryItem("正常", "18.5 - 23.9", BmiNormal)
            BmiCategoryItem("偏重", "24.0 - 27.9", BmiOverweight)
            BmiCategoryItem("肥胖", "≥ 28.0", BmiObese)
        }
    }
}

@Composable
private fun BmiCategoryItem(label: String, range: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = color,
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.size(12.dp)
        ) {}

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = range,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun BmiRangeCard(minBmi: Float, maxBmi: Float) {
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
                text = "📊 BMI 变化范围",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "最低 BMI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format("%.1f", minBmi),
                        style = MaterialTheme.typography.headlineMedium,
                        color = BmiNormal
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "最高 BMI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = String.format("%.1f", maxBmi),
                        style = MaterialTheme.typography.headlineMedium,
                        color = BmiOverweight
                    )
                }
            }
        }
    }
}

@Composable
private fun BmiFormulaCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📐 BMI 计算公式",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "BMI = 体重(kg) / 身高(m)²",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "举例：体重70kg，身高175cm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "BMI = 70 / (1.75)² = 70 / 3.0625 ≈ 22.9",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun getBmiAdvice(category: com.bodytrack.app.data.local.BmiCategory): String {
    return when (category) {
        com.bodytrack.app.data.local.BmiCategory.UNDERWEIGHT ->
            "体重偏轻，建议适当增加营养摄入，加强锻炼。"
        com.bodytrack.app.data.local.BmiCategory.NORMAL ->
            "体重正常，请继续保持健康的生活方式！"
        com.bodytrack.app.data.local.BmiCategory.OVERWEIGHT ->
            "体重偏重，建议控制饮食，增加运动量。"
        com.bodytrack.app.data.local.BmiCategory.OBESE ->
            "体重过重，建议咨询医生或营养师，制定健康计划。"
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
