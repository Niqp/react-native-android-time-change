package com.niqp.reactnativeandroidtimechange

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.HeadlessJsTaskService

class TimeChangeBroadcastReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val payload = TimeChangePayloadMapper.mapIntent(intent) ?: return

    if (TimeChangeReactBridge.emitIfForeground(payload)) {
      return
    }

    val taskName = TimeChangePreferences.getHeadlessTaskName(context)
    if (taskName == null) {
      Log.w(TAG, "Dropping time change broadcast because no Headless JS task is registered.")
      return
    }

    val serviceIntent = Intent(context, TimeChangeHeadlessJsTaskService::class.java).apply {
      putExtra(TimeChangePayloadMapper.EXTRA_TASK_NAME, taskName)
      TimeChangePayloadMapper.putPayloadExtras(this, payload)
    }

    try {
      context.startService(serviceIntent)
      HeadlessJsTaskService.acquireWakeLockNow(context)
    } catch (error: IllegalStateException) {
      Log.w(TAG, "Unable to start Headless JS task for time change broadcast.", error)
    }
  }

  private companion object {
    private const val TAG = "RNAndroidTimeChange"
  }
}
