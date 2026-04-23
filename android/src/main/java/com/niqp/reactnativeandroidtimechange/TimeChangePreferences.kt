package com.niqp.reactnativeandroidtimechange

import android.content.Context

object TimeChangePreferences {
  private const val PREFERENCES_NAME = "rn_android_time_change"
  private const val KEY_HEADLESS_TASK_NAME = "headless_task_name"

  fun setHeadlessTaskName(context: Context, taskName: String) {
    context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
      .edit()
      .putString(KEY_HEADLESS_TASK_NAME, taskName)
      .apply()
  }

  fun getHeadlessTaskName(context: Context): String? {
    return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
      .getString(KEY_HEADLESS_TASK_NAME, null)
      ?.takeIf { it.isNotBlank() }
  }
}
