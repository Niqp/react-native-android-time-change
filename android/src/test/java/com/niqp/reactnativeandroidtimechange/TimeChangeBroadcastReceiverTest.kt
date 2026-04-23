package com.niqp.reactnativeandroidtimechange

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowArguments::class])
class TimeChangeBroadcastReceiverTest {
  private lateinit var context: Application
  private lateinit var receiver: TimeChangeBroadcastReceiver
  private lateinit var reactContext: TestReactApplicationContext
  private lateinit var originalTimeZone: TimeZone

  @Before
  fun setUp() {
    originalTimeZone = TimeZone.getDefault()
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    context = RuntimeEnvironment.getApplication()
    receiver = TimeChangeBroadcastReceiver()
    reactContext = TestReactApplicationContext(context)
    TimeChangeReactBridge.setReactContext(reactContext)
    TimeChangeReactBridge.clearReactContext(reactContext)
    clearHeadlessTaskName()
    shadowOf(context).clearStartedServices()
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    TimeZone.setDefault(originalTimeZone)
  }

  @After
  fun tearDown() {
    TimeChangeReactBridge.setReactContext(reactContext)
    TimeChangeReactBridge.clearReactContext(reactContext)
    clearHeadlessTaskName()
    shadowOf(context).clearStartedServices()
  }

  @Test
  fun nullIntentDoesNothing() {
    receiver.onReceive(context, null)

    assertNull(shadowOf(context).nextStartedService)
  }

  @Test
  fun unknownIntentDoesNothing() {
    receiver.onReceive(context, Intent("example.UNKNOWN"))

    assertNull(shadowOf(context).nextStartedService)
  }

  @Test
  fun backgroundBroadcastWithoutRegisteredTaskDoesNotStartService() {
    receiver.onReceive(context, Intent(Intent.ACTION_TIME_CHANGED))

    assertNull(shadowOf(context).nextStartedService)
  }

  @Test
  fun backgroundBroadcastWithRegisteredTaskStartsHeadlessService() {
    TimeChangePreferences.setHeadlessTaskName(context, TASK_NAME)

    receiver.onReceive(context, Intent(Intent.ACTION_TIME_CHANGED))

    val serviceIntent = shadowOf(context).nextStartedService
    assertEquals(ComponentName(context, TimeChangeHeadlessJsTaskService::class.java), serviceIntent.component)
  }

  @Test
  fun startedServiceIntentIncludesTaskNameAndNormalizedPayloadExtras() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    TimeChangePreferences.setHeadlessTaskName(context, TASK_NAME)
    val intent = Intent(TimeChangePayloadMapper.ACTION_TIMEZONE_OFFSET_CHANGED)
      .putExtra(TimeChangePayloadMapper.EXTRA_OLD_TIMEZONE_OFFSET, 3_600)
      .putExtra(TimeChangePayloadMapper.EXTRA_NEW_TIMEZONE_OFFSET, 7_200)

    receiver.onReceive(context, intent)

    val serviceIntent = shadowOf(context).nextStartedService
    assertEquals(TASK_NAME, serviceIntent.getStringExtra(TimeChangePayloadMapper.EXTRA_TASK_NAME))
    assertEquals(
      "timezone_offset_changed",
      serviceIntent.getStringExtra(TimeChangePayloadMapper.EXTRA_ACTION)
    )
    assertEquals("UTC", serviceIntent.getStringExtra(TimeChangePayloadMapper.EXTRA_TIME_ZONE))
    assertEquals(120, serviceIntent.getIntExtra(TimeChangePayloadMapper.EXTRA_UTC_OFFSET_MINUTES, 0))
    assertEquals(
      60,
      serviceIntent.getIntExtra(TimeChangePayloadMapper.EXTRA_PREVIOUS_UTC_OFFSET_MINUTES, 0)
    )
  }

  @Test
  fun foregroundBridgeSuccessSkipsStartingHeadlessService() {
    reactContext.onHostResume(null)
    TimeChangeReactBridge.setReactContext(reactContext)
    TimeChangeReactBridge.addListener()
    TimeChangePreferences.setHeadlessTaskName(context, TASK_NAME)

    receiver.onReceive(context, Intent(Intent.ACTION_TIME_CHANGED))

    assertNull(shadowOf(context).nextStartedService)
  }

  @Test
  fun foregroundWithoutListenersFallsBackToHeadlessService() {
    reactContext.onHostResume(null)
    TimeChangeReactBridge.setReactContext(reactContext)
    TimeChangePreferences.setHeadlessTaskName(context, TASK_NAME)

    receiver.onReceive(context, Intent(Intent.ACTION_TIME_CHANGED))

    val serviceIntent = shadowOf(context).nextStartedService
    assertEquals(ComponentName(context, TimeChangeHeadlessJsTaskService::class.java), serviceIntent.component)
  }

  private fun clearHeadlessTaskName() {
    context.getSharedPreferences("rn_android_time_change", Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
  }

  private companion object {
    private const val TASK_NAME = "RNAndroidTimeChangeTask"
  }
}
