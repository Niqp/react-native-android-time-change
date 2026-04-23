package com.niqp.reactnativeandroidtimechange

import com.facebook.react.bridge.ReadableMap
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowArguments::class])
class TimeChangeReactBridgeTest {
  private lateinit var reactContext: TestReactApplicationContext

  @Before
  fun setUp() {
    reactContext = TestReactApplicationContext(RuntimeEnvironment.getApplication())
    TimeChangeReactBridge.setReactContext(reactContext)
    TimeChangeReactBridge.clearReactContext(reactContext)
  }

  @After
  fun tearDown() {
    TimeChangeReactBridge.setReactContext(reactContext)
    TimeChangeReactBridge.clearReactContext(reactContext)
  }

  @Test
  fun noReactContextReturnsFalse() {
    assertFalse(TimeChangeReactBridge.emitIfForeground(PAYLOAD))
  }

  @Test
  fun inactiveReactContextReturnsFalse() {
    reactContext.activeReactInstance = false
    reactContext.onHostResume(null)
    TimeChangeReactBridge.setReactContext(reactContext)

    assertFalse(TimeChangeReactBridge.emitIfForeground(PAYLOAD))
  }

  @Test
  fun nonResumedReactContextReturnsFalse() {
    TimeChangeReactBridge.setReactContext(reactContext)

    assertFalse(TimeChangeReactBridge.emitIfForeground(PAYLOAD))
  }

  @Test
  fun resumedContextWithZeroListenersReturnsFalse() {
    reactContext.onHostResume(null)
    TimeChangeReactBridge.setReactContext(reactContext)

    assertFalse(TimeChangeReactBridge.emitIfForeground(PAYLOAD))
    assertEquals(emptyList<TestReactApplicationContext.EmittedEvent>(), reactContext.emittedEvents)
  }

  @Test
  fun resumedContextWithListenersEmitsDeviceEvent() {
    reactContext.onHostResume(null)
    TimeChangeReactBridge.setReactContext(reactContext)
    TimeChangeReactBridge.addListener()

    assertTrue(TimeChangeReactBridge.emitIfForeground(PAYLOAD))

    assertEquals(1, reactContext.emittedEvents.size)
    assertEquals("RNAndroidTimeChangeEvent", reactContext.emittedEvents[0].name)
    val data = reactContext.emittedEvents[0].data as ReadableMap
    assertEquals("timezone_changed", data.getString("action"))
    assertEquals("Europe/Madrid", data.getString("timeZone"))
    assertEquals(60, data.getInt("utcOffsetMinutes"))
    assertEquals(TIMESTAMP.toDouble(), data.getDouble("timestamp"), 0.0)
  }

  @Test
  fun removeListenersCannotMakeListenerCountNegative() {
    reactContext.onHostResume(null)
    TimeChangeReactBridge.setReactContext(reactContext)
    TimeChangeReactBridge.addListener()
    TimeChangeReactBridge.removeListeners(10)

    assertFalse(TimeChangeReactBridge.emitIfForeground(PAYLOAD))
    assertEquals(emptyList<TestReactApplicationContext.EmittedEvent>(), reactContext.emittedEvents)
  }

  @Test
  fun clearReactContextResetsContextAndListenerCount() {
    reactContext.onHostResume(null)
    TimeChangeReactBridge.setReactContext(reactContext)
    TimeChangeReactBridge.addListener()
    TimeChangeReactBridge.clearReactContext(reactContext)

    assertFalse(TimeChangeReactBridge.emitIfForeground(PAYLOAD))

    TimeChangeReactBridge.setReactContext(reactContext)
    assertFalse(TimeChangeReactBridge.emitIfForeground(PAYLOAD))
    assertEquals(emptyList<TestReactApplicationContext.EmittedEvent>(), reactContext.emittedEvents)
  }

  private companion object {
    private const val TIMESTAMP = 1_704_067_200_000L

    private val PAYLOAD = TimeChangePayload(
      action = "timezone_changed",
      timeZone = "Europe/Madrid",
      utcOffsetMinutes = 60,
      previousUtcOffsetMinutes = null,
      timestamp = TIMESTAMP
    )
  }
}
