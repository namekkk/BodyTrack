package com.bodytrack.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.bodytrack.app.notification.ReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// DataStore扩展
private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * 设置视图模型
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore
    private val workManager = WorkManager.getInstance(application)

    // 提醒是否启用
    val reminderEnabled: StateFlow<Boolean> = dataStore.data
        .map { it[booleanPreferencesKey("reminder_enabled")] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // 提醒时间（小时）
    val reminderHour: StateFlow<Int> = dataStore.data
        .map { it[intPreferencesKey("reminder_hour")] ?: 20 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    // 提醒时间（分钟）
    val reminderMinute: StateFlow<Int> = dataStore.data
        .map { it[intPreferencesKey("reminder_minute")] ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * 获取格式化的提醒时间
     */
    val reminderTimeString: StateFlow<String> = combine(reminderHour, reminderMinute) { hour, minute ->
        LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("HH:mm"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "20:00")

    /**
     * 切换提醒状态
     */
    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[booleanPreferencesKey("reminder_enabled")] = enabled
            }
            if (enabled) {
                scheduleReminder()
            } else {
                cancelReminder()
            }
        }
    }

    /**
     * 设置提醒时间
     */
    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[intPreferencesKey("reminder_hour")] = hour
                prefs[intPreferencesKey("reminder_minute")] = minute
            }
            // 如果已启用，重新调度
            if (reminderEnabled.value) {
                scheduleReminder()
            }
        }
    }

    /**
     * 调度每日提醒
     */
    private fun scheduleReminder() {
        val hour = reminderHour.value
        val minute = reminderMinute.value

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest
        )
    }

    /**
     * 取消提醒
     */
    private fun cancelReminder() {
        workManager.cancelUniqueWork("daily_reminder")
    }
}
