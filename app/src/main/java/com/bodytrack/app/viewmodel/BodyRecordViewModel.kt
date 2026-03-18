package com.bodytrack.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bodytrack.app.data.local.AppDatabase
import com.bodytrack.app.data.local.BodyRecord
import com.bodytrack.app.data.repository.BodyRecordRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 身体记录视图模型
 */
class BodyRecordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BodyRecordRepository
    private val _uiState = MutableStateFlow(BodyRecordUiState())
    val uiState: StateFlow<BodyRecordUiState> = _uiState.asStateFlow()

    // 数据流
    val todayRecord: StateFlow<BodyRecord?>
    val allRecords: StateFlow<List<BodyRecord>>
    val recordCount: StateFlow<Int>
    val minWeight: StateFlow<Float?>
    val maxWeight: StateFlow<Float?>
    val minBmi: StateFlow<Float?>
    val maxBmi: StateFlow<Float?>
    val avgWeight: StateFlow<Float?>

    // 图表数据 - 最近30天
    val chartRecords: StateFlow<List<BodyRecord>>

    init {
        val database = AppDatabase.getInstance(application)
        repository = BodyRecordRepository(database.bodyRecordDao())

        todayRecord = repository.getTodayRecord()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allRecords = repository.allRecords
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        recordCount = repository.recordCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        minWeight = repository.minWeight
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        maxWeight = repository.maxWeight
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        minBmi = repository.minBmi
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        maxBmi = repository.maxBmi
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        avgWeight = repository.avgWeight
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        chartRecords = repository.getRecentRecords(30)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /**
     * 更新身高输入
     */
    fun updateHeightInput(height: String) {
        _uiState.update { it.copy(heightInput = height) }
        calculatePreviewBmi()
    }

    /**
     * 更新体重输入
     */
    fun updateWeightInput(weight: String) {
        _uiState.update { it.copy(weightInput = weight) }
        calculatePreviewBmi()
    }

    /**
     * 计算预览BMI
     */
    private fun calculatePreviewBmi() {
        val height = _uiState.value.heightInput.toFloatOrNull()
        val weight = _uiState.value.weightInput.toFloatOrNull()

        val bmi = if (height != null && weight != null && height > 0 && weight > 0) {
            BodyRecord.calculateBmi(height, weight)
        } else {
            null
        }

        _uiState.update { it.copy(previewBmi = bmi) }
    }

    /**
     * 保存记录
     */
    fun saveRecord(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val height = _uiState.value.heightInput.toFloatOrNull()
        val weight = _uiState.value.weightInput.toFloatOrNull()

        if (height == null || weight == null || height <= 0 || weight <= 0) {
            onError("请输入有效的身高和体重")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.saveRecord(height, weight)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            heightInput = "",
                            weightInput = "",
                            previewBmi = null
                        )
                    }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    onError(e.message ?: "保存失败")
                }
        }
    }

    /**
     * 删除记录
     */
    fun deleteRecord(record: BodyRecord, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteRecord(record)
            } catch (e: Exception) {
                onError(e.message ?: "删除失败")
            }
        }
    }

    /**
     * 清空所有记录
     */
    fun clearAllRecords(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteAllRecords()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "清空失败")
            }
        }
    }

    /**
     * 批量插入记录（用于恢复）
     */
    fun restoreRecords(records: List<BodyRecord>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.insertAll(records)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "恢复失败")
            }
        }
    }

    /**
     * 格式化日期显示
     */
    fun formatDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("MM月dd日")
            date.format(formatter)
        } catch (e: Exception) {
            dateString
        }
    }
}

/**
 * UI状态数据类
 */
data class BodyRecordUiState(
    val heightInput: String = "",
    val weightInput: String = "",
    val previewBmi: Float? = null,
    val isLoading: Boolean = false
)
