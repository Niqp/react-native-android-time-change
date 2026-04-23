package com.niqp.reactnativeandroidtimechange

import android.content.Context
import android.content.Intent
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowArguments::class])
class TimeChangeHeadlessJsTaskServiceTest {
  private lateinit var service: TimeChangeHeadlessJsTaskService

  @Before
  fun setUp() {
    service = Robolectric.buildService(TimeChangeHeadlessJsTaskService::class.java)
      .create()
      .get()
    clearHeadlessTaskName()
  }

  @After
  fun tearDown() {
    clearHeadlessTaskName()
  }

  @Test
  fun returnsNullWhenTaskNameIsMissing() {
    assertNull(service.taskConfig(serviceIntent(taskName = null)))
  }

  @Test
  fun returnsNullWhenPayloadExtrasAreMissing() {
    assertNull(service.taskConfig(Intent().putExtra(TimeChangePayloadMapper.EXTRA_TASK_NAME, TASK_NAME)))
  }

  @Test
  fun usesTaskNameFromIntentWhenPresent() {
    TimeChangePreferences.setHeadlessTaskName(service, "StoredTask")

    val config = service.taskConfig(serviceIntent(taskName = TASK_NAME))

    assertEquals(TASK_NAME, config?.taskKey)
  }

  @Test
  fun fallsBackToStoredPreferenceTaskName() {
    TimeChangePreferences.setHeadlessTaskName(service, TASK_NAME)

    val config = service.taskConfig(serviceIntent(taskName = null))

    assertEquals(TASK_NAME, config?.taskKey)
  }

  @Test
  fun emitsExpectedHeadlessTaskConfig() {
    val config = service.taskConfig(serviceIntent(taskName = TASK_NAME))

    requireNotNull(config)
    assertEquals(TASK_NAME, config.taskKey)
    assertEquals(30_000L, config.timeout)
    assertFalse(config.isAllowedInForeground)
    assertEquals("timezone_offset_changed", config.data.getString("action"))
    assertEquals("UTC", config.data.getString("timeZone"))
    assertEquals(120, config.data.getInt("utcOffsetMinutes"))
    assertEquals(60, config.data.getInt("previousUtcOffsetMinutes"))
    assertEquals(TIMESTAMP.toDouble(), config.data.getDouble("timestamp"), 0.0)
  }

  private fun TimeChangeHeadlessJsTaskService.taskConfig(intent: Intent?): HeadlessJsTaskConfig? {
    val method = TimeChangeHeadlessJsTaskService::class.java
      .getDeclaredMethod("getTaskConfig", Intent::class.java)
    method.isAccessible = true

    return method.invoke(this, intent) as HeadlessJsTaskConfig?
  }

  private fun serviceIntent(taskName: String?): Intent {
    val payload = TimeChangePayload(
      action = "timezone_offset_changed",
      timeZone = "UTC",
      utcOffsetMinutes = 120,
      previousUtcOffsetMinutes = 60,
      timestamp = TIMESTAMP
    )

    return Intent().apply {
      taskName?.let { putExtra(TimeChangePayloadMapper.EXTRA_TASK_NAME, it) }
      TimeChangePayloadMapper.putPayloadExtras(this, payload)
    }
  }

  private fun clearHeadlessTaskName() {
    service.getSharedPreferences("rn_android_time_change", Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
  }

  private companion object {
    private const val TASK_NAME = "RNAndroidTimeChangeTask"
    private const val TIMESTAMP = 1_704_067_200_000L
  }
}
