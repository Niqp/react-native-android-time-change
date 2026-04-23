package com.niqp.reactnativeandroidtimechange

import android.app.Application
import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class TimeChangePreferencesTest {
  private lateinit var context: Application

  @Before
  fun setUp() {
    context = RuntimeEnvironment.getApplication()
    clearPreferences()
  }

  @After
  fun tearDown() {
    clearPreferences()
  }

  @Test
  fun storesAndRetrievesHeadlessTaskName() {
    TimeChangePreferences.setHeadlessTaskName(context, "RNAndroidTimeChangeTask")

    assertEquals("RNAndroidTimeChangeTask", TimeChangePreferences.getHeadlessTaskName(context))
  }

  @Test
  fun blankTaskNamesAreTreatedAsAbsent() {
    TimeChangePreferences.setHeadlessTaskName(context, "   ")

    assertNull(TimeChangePreferences.getHeadlessTaskName(context))
  }

  private fun clearPreferences() {
    context.getSharedPreferences("rn_android_time_change", Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
  }
}
