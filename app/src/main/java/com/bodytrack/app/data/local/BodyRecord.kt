package com.bodytrack.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 身体记录实体类
 * 用于存储每日的身高体重数据
 */
@Entity(tableName = "body_records")
data class BodyRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // 格式: yyyy-MM-dd
    val height: Float, // 身高(cm)
    val weight: Float, // 体重(kg)
    val bmi: Float,    // 自动计算的BMI
    val note: String = "", // 备注
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 计算BMI指数
         * BMI = 体重(kg) / 身高(m)²
         */
        fun calculateBmi(heightCm: Float, weightKg: Float): Float {
            if (heightCm <= 0 || weightKg <= 0) return 0f
            val heightM = heightCm / 100f
            return weightKg / (heightM * heightM)
        }

        /**
         * 获取BMI分类
         * 偏瘦: < 18.5
         * 正常: 18.5 - 23.9
         * 偏重: 24.0 - 27.9
         * 肥胖: >= 28.0
         */
        fun getBmiCategory(bmi: Float): BmiCategory {
            return when {
                bmi < 18.5f -> BmiCategory.UNDERWEIGHT
                bmi < 24f -> BmiCategory.NORMAL
                bmi < 28f -> BmiCategory.OVERWEIGHT
                else -> BmiCategory.OBESE
            }
        }

        /**
         * 获取今天的日期字符串
         */
        fun todayString(): String {
            return LocalDate.now().toString()
        }
    }
}

enum class BmiCategory(val label: String, val colorName: String) {
    UNDERWEIGHT("偏瘦", "Blue"),
    NORMAL("正常", "Green"),
    OVERWEIGHT("偏重", "Orange"),
    OBESE("肥胖", "Red")
}
