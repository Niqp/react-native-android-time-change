package com.niqp.reactnativeandroidtimechange

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.JavaOnlyMap
import com.facebook.react.bridge.WritableMap
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(Arguments::class)
object ShadowArguments {
  @JvmStatic
  @Implementation
  fun createMap(): WritableMap = JavaOnlyMap()
}
