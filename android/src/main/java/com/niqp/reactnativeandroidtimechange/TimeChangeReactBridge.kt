package com.niqp.reactnativeandroidtimechange

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.common.LifecycleState
import java.lang.ref.WeakReference

object TimeChangeReactBridge {
  private const val EVENT_NAME = "RNAndroidTimeChangeEvent"

  private var reactContextReference: WeakReference<ReactApplicationContext>? = null
  private var listenerCount = 0

  fun setReactContext(reactContext: ReactApplicationContext) {
    reactContextReference = WeakReference(reactContext)
  }

  fun clearReactContext(reactContext: ReactApplicationContext) {
    if (reactContextReference?.get() === reactContext) {
      reactContextReference = null
      listenerCount = 0
    }
  }

  fun addListener() {
    listenerCount += 1
  }

  fun removeListeners(count: Int) {
    listenerCount = (listenerCount - count).coerceAtLeast(0)
  }

  fun emitIfForeground(payload: TimeChangePayload): Boolean {
    val reactContext = reactContextReference?.get() ?: return false

    if (!reactContext.hasActiveReactInstance() || reactContext.lifecycleState != LifecycleState.RESUMED) {
      return false
    }

    if (listenerCount <= 0) {
      return false
    }

    reactContext.emitDeviceEvent(EVENT_NAME, payload.toWritableMap())
    return true
  }
}
