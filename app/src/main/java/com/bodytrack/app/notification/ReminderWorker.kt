package com.bodytrack.app.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * 提醒工作器
 * 使用WorkManager定期检查并发送提醒
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // WorkManager的定时不太精确，我们需要在ReminderReceiver中处理
            // 这里只是一个占位符，实际的提醒逻辑在AlarmManager中处理
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
