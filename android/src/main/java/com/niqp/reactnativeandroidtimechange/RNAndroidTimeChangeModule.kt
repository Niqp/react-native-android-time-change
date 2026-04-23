package com.niqp.reactnativeandroidtimechange

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = RNAndroidTimeChangeModule.NAME)
class RNAndroidTimeChangeModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun initialize() {
    super.initialize()
    TimeChangeReactBridge.setReactContext(reactContext)
  }

  override fun invalidate() {
    TimeChangeReactBridge.clearReactContext(reactContext)
    super.invalidate()
  }

  override fun getName(): String = NAME

  @ReactMethod
  fun registerTimeChangeHeadlessTask(taskName: String) {
    TimeChangePreferences.setHeadlessTaskName(reactContext, taskName)
  }

  @ReactMethod
  fun getCurrentTimeContext(promise: Promise) {
    val currentContext = TimeChangePayloadMapper.currentTimeContext()
    val context = Arguments.createMap().apply {
      putString("timeZone", currentContext.timeZone)
      putInt("utcOffsetMinutes", currentContext.utcOffsetMinutes)
      putDouble("timestamp", currentContext.timestamp.toDouble())
    }

    promise.resolve(context)
  }

  @ReactMethod
  fun addListener(eventName: String) {
    TimeChangeReactBridge.addListener()
  }

  @ReactMethod
  fun removeListeners(count: Int) {
    TimeChangeReactBridge.removeListeners(count)
  }

  companion object {
    const val NAME = "RNAndroidTimeChange"
  }
}
