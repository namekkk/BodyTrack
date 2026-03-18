package com.bodytrack.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 身体记录数据访问对象
 */
@Dao
interface BodyRecordDao {
    // 获取所有记录，按日期降序排列
    @Query("SELECT * FROM body_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<BodyRecord>>

    // 获取最近的N条记录
    @Query("SELECT * FROM body_records ORDER BY date DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<BodyRecord>>

    // 获取指定日期的记录
    @Query("SELECT * FROM body_records WHERE date = :date LIMIT 1")
    suspend fun getRecordByDate(date: String): BodyRecord?

    // 获取指定日期的记录（Flow版本）
    @Query("SELECT * FROM body_records WHERE date = :date LIMIT 1")
    fun getRecordByDateFlow(date: String): Flow<BodyRecord?>

    // 获取日期范围内的记录
    @Query("SELECT * FROM body_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<BodyRecord>>

    // 插入记录
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BodyRecord): Long

    // 更新记录
    @Update
    suspend fun update(record: BodyRecord)

    // 删除记录
    @Delete
    suspend fun delete(record: BodyRecord)

    // 删除所有记录
    @Query("DELETE FROM body_records")
    suspend fun deleteAll()

    // 获取记录总数
    @Query("SELECT COUNT(*) FROM body_records")
    fun getRecordCount(): Flow<Int>

    // 获取最轻体重
    @Query("SELECT MIN(weight) FROM body_records")
    fun getMinWeight(): Flow<Float?>

    // 获取最重体重
    @Query("SELECT MAX(weight) FROM body_records")
    fun getMaxWeight(): Flow<Float?>

    // 获取最低BMI
    @Query("SELECT MIN(bmi) FROM body_records")
    fun getMinBmi(): Flow<Float?>

    // 获取最高BMI
    @Query("SELECT MAX(bmi) FROM body_records")
    fun getMaxBmi(): Flow<Float?>

    // 获取平均体重
    @Query("SELECT AVG(weight) FROM body_records")
    fun getAvgWeight(): Flow<Float?>

    // 获取最近7条体重记录
    @Query("SELECT weight FROM body_records ORDER BY date DESC LIMIT 7")
    fun getRecentWeights(): Flow<List<Float>>
}
