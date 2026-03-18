package com.bodytrack.app.data.repository

import com.bodytrack.app.data.local.BodyRecord
import com.bodytrack.app.data.local.BodyRecordDao
import kotlinx.coroutines.flow.Flow

/**
 * 身体记录仓库
 */
class BodyRecordRepository(private val dao: BodyRecordDao) {
    // 获取所有记录
    val allRecords: Flow<List<BodyRecord>> = dao.getAllRecords()

    // 获取记录数量
    val recordCount: Flow<Int> = dao.getRecordCount()

    // 获取最轻/最重体重
    val minWeight: Flow<Float?> = dao.getMinWeight()
    val maxWeight: Flow<Float?> = dao.getMaxWeight()

    // 获取BMI范围
    val minBmi: Flow<Float?> = dao.getMinBmi()
    val maxBmi: Flow<Float?> = dao.getMaxBmi()

    // 获取平均体重
    val avgWeight: Flow<Float?> = dao.getAvgWeight()

    // 获取最近的体重记录
    val recentWeights: Flow<List<Float>> = dao.getRecentWeights()

    /**
     * 获取今日记录
     */
    fun getTodayRecord(): Flow<BodyRecord?> {
        return dao.getRecordByDateFlow(BodyRecord.todayString())
    }

    /**
     * 获取最近N条记录
     */
    fun getRecentRecords(limit: Int = 30): Flow<List<BodyRecord>> {
        return dao.getRecentRecords(limit)
    }

    /**
     * 获取日期范围内的记录
     */
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<BodyRecord>> {
        return dao.getRecordsByDateRange(startDate, endDate)
    }

    /**
     * 保存记录（插入或更新）
     */
    suspend fun saveRecord(height: Float, weight: Float, note: String = ""): Result<BodyRecord> {
        return try {
            val bmi = BodyRecord.calculateBmi(height, weight)
            val today = BodyRecord.todayString()

            // 检查今天是否已有记录
            val existing = dao.getRecordByDate(today)

            val record = if (existing != null) {
                existing.copy(height = height, weight = weight, bmi = bmi, note = note)
            } else {
                BodyRecord(
                    date = today,
                    height = height,
                    weight = weight,
                    bmi = bmi,
                    note = note
                )
            }

            if (existing != null) {
                dao.update(record)
            } else {
                dao.insert(record)
            }

            Result.success(record)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除记录
     */
    suspend fun deleteRecord(record: BodyRecord) {
        dao.delete(record)
    }

    /**
     * 删除所有记录
     */
    suspend fun deleteAllRecords() {
        dao.deleteAll()
    }

    /**
     * 批量插入记录（用于恢复数据）
     */
    suspend fun insertAll(records: List<BodyRecord>) {
        records.forEach { record ->
            dao.insert(record)
        }
    }
}
