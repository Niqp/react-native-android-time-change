package com.niqp.reactnativeandroidtimechange

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

data class TimeChangePayload(
  val action: String,
  val timeZone: String?,
  val utcOffsetMinutes: Int,
  val previousUtcOffsetMinutes: Int?,
  val timestamp: Long
) {
  fun toWritableMap(): WritableMap {
    return Arguments.createMap().apply {
      putString("action", action)
      if (timeZone != null) {
        putString("timeZone", timeZone)
      }
      putInt("utcOffsetMinutes", utcOffsetMinutes)
      if (previousUtcOffsetMinutes != null) {
        putInt("previousUtcOffsetMinutes", previousUtcOffsetMinutes)
      }
      putDouble("timestamp", timestamp.toDouble())
    }
  }
}
