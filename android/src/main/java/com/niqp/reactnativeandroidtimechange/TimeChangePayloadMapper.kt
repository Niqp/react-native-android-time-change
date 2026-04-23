package com.niqp.reactnativeandroidtimechange

import android.content.Intent
import java.util.TimeZone

object TimeChangePayloadMapper {
  const val ACTION_TIME_SET_JS = "time_set"
  const val ACTION_TIMEZONE_CHANGED_JS = "timezone_changed"
  const val ACTION_TIMEZONE_OFFSET_CHANGED_JS = "timezone_offset_changed"

  const val ACTION_TIMEZONE_OFFSET_CHANGED = "android.intent.action.TIMEZONE_OFFSET_CHANGED"
  const val EXTRA_OLD_TIMEZONE_OFFSET = "android.intent.extra.OLD_TIMEZONE_OFFSET"
  const val EXTRA_NEW_TIMEZONE_OFFSET = "android.intent.extra.NEW_TIMEZONE_OFFSET"
  const val EXTRA_TASK_NAME = "rn_android_time_change_task_name"
  const val EXTRA_ACTION = "rn_android_time_change_action"
  const val EXTRA_TIME_ZONE = "rn_android_time_change_time_zone"
  const val EXTRA_UTC_OFFSET_MINUTES = "rn_android_time_change_utc_offset_minutes"
  const val EXTRA_PREVIOUS_UTC_OFFSET_MINUTES = "rn_android_time_change_previous_utc_offset_minutes"
  const val EXTRA_TIMESTAMP = "rn_android_time_change_timestamp"

  private const val LEGACY_EXTRA_TIMEZONE = "time-zone"
  private const val MILLIS_PER_MINUTE = 60_000
  private const val SECONDS_PER_MINUTE = 60

  fun mapIntent(intent: Intent?, timestamp: Long = System.currentTimeMillis()): TimeChangePayload? {
    return when (intent?.action) {
      Intent.ACTION_TIME_CHANGED -> TimeChangePayload(
        action = ACTION_TIME_SET_JS,
        timeZone = null,
        utcOffsetMinutes = currentUtcOffsetMinutes(timestamp),
        previousUtcOffsetMinutes = null,
        timestamp = timestamp
      )

      Intent.ACTION_TIMEZONE_CHANGED -> {
        val timeZone = intent.getStringExtra(Intent.EXTRA_TIMEZONE)
          ?: intent.getStringExtra(LEGACY_EXTRA_TIMEZONE)
          ?: TimeZone.getDefault().id

        TimeChangePayload(
          action = ACTION_TIMEZONE_CHANGED_JS,
          timeZone = timeZone,
          utcOffsetMinutes = utcOffsetMinutes(timeZone, timestamp),
          previousUtcOffsetMinutes = null,
          timestamp = timestamp
        )
      }

      ACTION_TIMEZONE_OFFSET_CHANGED -> {
        val newOffsetSeconds = optionalIntExtra(intent, EXTRA_NEW_TIMEZONE_OFFSET)
        val oldOffsetSeconds = optionalIntExtra(intent, EXTRA_OLD_TIMEZONE_OFFSET)

        TimeChangePayload(
          action = ACTION_TIMEZONE_OFFSET_CHANGED_JS,
          timeZone = TimeZone.getDefault().id,
          utcOffsetMinutes = newOffsetSeconds?.let { it / SECONDS_PER_MINUTE } ?: currentUtcOffsetMinutes(timestamp),
          previousUtcOffsetMinutes = oldOffsetSeconds?.let { it / SECONDS_PER_MINUTE },
          timestamp = timestamp
        )
      }

      else -> null
    }
  }

  fun putPayloadExtras(intent: Intent, payload: TimeChangePayload) {
    intent.putExtra(EXTRA_ACTION, payload.action)
    intent.putExtra(EXTRA_UTC_OFFSET_MINUTES, payload.utcOffsetMinutes)
    intent.putExtra(EXTRA_TIMESTAMP, payload.timestamp)

    payload.timeZone?.let { intent.putExtra(EXTRA_TIME_ZONE, it) }
    payload.previousUtcOffsetMinutes?.let { intent.putExtra(EXTRA_PREVIOUS_UTC_OFFSET_MINUTES, it) }
  }

  fun mapServiceIntent(intent: Intent?): TimeChangePayload? {
    if (intent == null || !intent.hasExtra(EXTRA_ACTION) || !intent.hasExtra(EXTRA_UTC_OFFSET_MINUTES)) {
      return null
    }

    val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())

    return TimeChangePayload(
      action = intent.getStringExtra(EXTRA_ACTION) ?: return null,
      timeZone = intent.getStringExtra(EXTRA_TIME_ZONE),
      utcOffsetMinutes = intent.getIntExtra(EXTRA_UTC_OFFSET_MINUTES, currentUtcOffsetMinutes(timestamp)),
      previousUtcOffsetMinutes = optionalIntExtra(intent, EXTRA_PREVIOUS_UTC_OFFSET_MINUTES),
      timestamp = timestamp
    )
  }

  fun currentTimeContext(timestamp: Long = System.currentTimeMillis()): TimeChangePayload {
    val timeZone = TimeZone.getDefault().id

    return TimeChangePayload(
      action = ACTION_TIMEZONE_CHANGED_JS,
      timeZone = timeZone,
      utcOffsetMinutes = utcOffsetMinutes(timeZone, timestamp),
      previousUtcOffsetMinutes = null,
      timestamp = timestamp
    )
  }

  private fun currentUtcOffsetMinutes(timestamp: Long): Int {
    return TimeZone.getDefault().getOffset(timestamp) / MILLIS_PER_MINUTE
  }

  private fun utcOffsetMinutes(timeZoneId: String, timestamp: Long): Int {
    return TimeZone.getTimeZone(timeZoneId).getOffset(timestamp) / MILLIS_PER_MINUTE
  }

  private fun optionalIntExtra(intent: Intent, key: String): Int? {
    return if (intent.hasExtra(key)) intent.getIntExtra(key, 0) else null
  }
}
