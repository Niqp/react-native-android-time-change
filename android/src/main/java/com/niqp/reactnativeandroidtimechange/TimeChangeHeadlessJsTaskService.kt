package com.niqp.reactnativeandroidtimechange

import android.content.Intent
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.jstasks.HeadlessJsTaskConfig

class TimeChangeHeadlessJsTaskService : HeadlessJsTaskService() {
  override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig? {
    val taskName = intent?.getStringExtra(TimeChangePayloadMapper.EXTRA_TASK_NAME)
      ?: TimeChangePreferences.getHeadlessTaskName(this)
      ?: return null
    val payload = TimeChangePayloadMapper.mapServiceIntent(intent) ?: return null

    return HeadlessJsTaskConfig(taskName, payload.toWritableMap(), TASK_TIMEOUT_MS, false)
  }

  private companion object {
    private const val TASK_TIMEOUT_MS = 30_000L
  }
}
