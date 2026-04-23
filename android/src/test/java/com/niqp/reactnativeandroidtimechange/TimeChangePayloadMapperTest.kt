package com.niqp.reactnativeandroidtimechange

import android.content.Intent
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TimeChangePayloadMapperTest {
  private lateinit var originalTimeZone: TimeZone

  @Before
  fun setUp() {
    originalTimeZone = TimeZone.getDefault()
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  }

  @After
  fun tearDown() {
    TimeZone.setDefault(originalTimeZone)
  }

  @Test
  fun mapsTimeSetIntent() {
    val payload = TimeChangePayloadMapper.mapIntent(Intent(Intent.ACTION_TIME_CHANGED), TIMESTAMP)

    assertEquals("time_set", payload?.action)
    assertNull(payload?.timeZone)
    assertEquals(0, payload?.utcOffsetMinutes)
    assertNull(payload?.previousUtcOffsetMinutes)
    assertEquals(TIMESTAMP, payload?.timestamp)
  }

  @Test
  fun mapsTimezoneChangedIntent() {
    val intent = Intent(Intent.ACTION_TIMEZONE_CHANGED).putExtra("time-zone", "Europe/Madrid")

    val payload = TimeChangePayloadMapper.mapIntent(intent, TIMESTAMP)

    assertEquals("timezone_changed", payload?.action)
    assertEquals("Europe/Madrid", payload?.timeZone)
    assertEquals(60, payload?.utcOffsetMinutes)
    assertNull(payload?.previousUtcOffsetMinutes)
  }

  @Test
  fun mapsTimezoneChangedIntentWithOfficialTimezoneExtra() {
    val intent = Intent(Intent.ACTION_TIMEZONE_CHANGED)
      .putExtra(Intent.EXTRA_TIMEZONE, "Europe/Madrid")

    val payload = TimeChangePayloadMapper.mapIntent(intent, TIMESTAMP)

    assertEquals("timezone_changed", payload?.action)
    assertEquals("Europe/Madrid", payload?.timeZone)
    assertEquals(60, payload?.utcOffsetMinutes)
  }

  @Test
  fun mapsTimezoneChangedIntentWithoutExtraUsingDefaultTimezone() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))

    val payload = TimeChangePayloadMapper.mapIntent(Intent(Intent.ACTION_TIMEZONE_CHANGED), TIMESTAMP)

    assertEquals("timezone_changed", payload?.action)
    assertEquals("America/New_York", payload?.timeZone)
    assertEquals(-300, payload?.utcOffsetMinutes)
  }

  @Test
  fun mapsTimezoneOffsetChangedIntentWithLiteralApi37Strings() {
    val intent = Intent(TimeChangePayloadMapper.ACTION_TIMEZONE_OFFSET_CHANGED)
      .putExtra(TimeChangePayloadMapper.EXTRA_OLD_TIMEZONE_OFFSET, 3_600)
      .putExtra(TimeChangePayloadMapper.EXTRA_NEW_TIMEZONE_OFFSET, 7_200)

    val payload = TimeChangePayloadMapper.mapIntent(intent, TIMESTAMP)

    assertEquals("timezone_offset_changed", payload?.action)
    assertEquals("UTC", payload?.timeZone)
    assertEquals(120, payload?.utcOffsetMinutes)
    assertEquals(60, payload?.previousUtcOffsetMinutes)
  }

  @Test
  fun mapsTimezoneOffsetChangedIntentWithMissingOffsetsUsingDefaultTimezone() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))

    val payload = TimeChangePayloadMapper.mapIntent(
      Intent(TimeChangePayloadMapper.ACTION_TIMEZONE_OFFSET_CHANGED),
      TIMESTAMP
    )

    assertEquals("timezone_offset_changed", payload?.action)
    assertEquals("America/New_York", payload?.timeZone)
    assertEquals(-300, payload?.utcOffsetMinutes)
    assertNull(payload?.previousUtcOffsetMinutes)
  }

  @Test
  fun mapIntentReturnsNullForNullIntent() {
    assertNull(TimeChangePayloadMapper.mapIntent(null, TIMESTAMP))
  }

  @Test
  fun ignoresUnknownIntent() {
    val payload = TimeChangePayloadMapper.mapIntent(Intent("example.UNKNOWN"), TIMESTAMP)

    assertNull(payload)
  }

  @Test
  fun mapServiceIntentReturnsNullForNullIntent() {
    assertNull(TimeChangePayloadMapper.mapServiceIntent(null))
  }

  @Test
  fun mapServiceIntentReturnsNullWhenRequiredExtrasAreMissing() {
    assertNull(TimeChangePayloadMapper.mapServiceIntent(Intent()))
    assertNull(TimeChangePayloadMapper.mapServiceIntent(
      Intent().putExtra(TimeChangePayloadMapper.EXTRA_ACTION, "time_set")
    ))
    assertNull(TimeChangePayloadMapper.mapServiceIntent(
      Intent().putExtra(TimeChangePayloadMapper.EXTRA_UTC_OFFSET_MINUTES, 0)
    ))
  }

  @Test
  fun roundTripsServiceIntentPayload() {
    val source = TimeChangePayload(
      action = "timezone_offset_changed",
      timeZone = "UTC",
      utcOffsetMinutes = 120,
      previousUtcOffsetMinutes = 60,
      timestamp = TIMESTAMP
    )
    val intent = Intent()

    TimeChangePayloadMapper.putPayloadExtras(intent, source)
    val payload = TimeChangePayloadMapper.mapServiceIntent(intent)

    assertEquals(source, payload)
  }

  @Test
  fun mapsCurrentTimeContextUsingDefaultTimezoneOffsetAndTimestamp() {
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Madrid"))

    val payload = TimeChangePayloadMapper.currentTimeContext(MADRID_SUMMER_TIMESTAMP)

    assertEquals("timezone_changed", payload.action)
    assertEquals("Europe/Madrid", payload.timeZone)
    assertEquals(120, payload.utcOffsetMinutes)
    assertNull(payload.previousUtcOffsetMinutes)
    assertEquals(MADRID_SUMMER_TIMESTAMP, payload.timestamp)
  }

  @Test
  fun mapsMadridWinterAndSummerDstOffsets() {
    val winterIntent = Intent(Intent.ACTION_TIMEZONE_CHANGED)
      .putExtra(Intent.EXTRA_TIMEZONE, "Europe/Madrid")
    val summerIntent = Intent(Intent.ACTION_TIMEZONE_CHANGED)
      .putExtra(Intent.EXTRA_TIMEZONE, "Europe/Madrid")

    val winterPayload = TimeChangePayloadMapper.mapIntent(winterIntent, MADRID_WINTER_TIMESTAMP)
    val summerPayload = TimeChangePayloadMapper.mapIntent(summerIntent, MADRID_SUMMER_TIMESTAMP)

    assertEquals(60, winterPayload?.utcOffsetMinutes)
    assertEquals(120, summerPayload?.utcOffsetMinutes)
  }

  @Test
  fun mapsNegativeOffsetTimezoneAcrossDst() {
    val winterIntent = Intent(Intent.ACTION_TIMEZONE_CHANGED)
      .putExtra(Intent.EXTRA_TIMEZONE, "America/New_York")
    val summerIntent = Intent(Intent.ACTION_TIMEZONE_CHANGED)
      .putExtra(Intent.EXTRA_TIMEZONE, "America/New_York")

    val winterPayload = TimeChangePayloadMapper.mapIntent(winterIntent, NEW_YORK_WINTER_TIMESTAMP)
    val summerPayload = TimeChangePayloadMapper.mapIntent(summerIntent, NEW_YORK_SUMMER_TIMESTAMP)

    assertEquals(-300, winterPayload?.utcOffsetMinutes)
    assertEquals(-240, summerPayload?.utcOffsetMinutes)
  }

  private companion object {
    private const val TIMESTAMP = 1_704_067_200_000L
    private const val MADRID_WINTER_TIMESTAMP = 1_704_067_200_000L
    private const val MADRID_SUMMER_TIMESTAMP = 1_719_792_000_000L
    private const val NEW_YORK_WINTER_TIMESTAMP = 1_704_067_200_000L
    private const val NEW_YORK_SUMMER_TIMESTAMP = 1_719_792_000_000L
  }
}
