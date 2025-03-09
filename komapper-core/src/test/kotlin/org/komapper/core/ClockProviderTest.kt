package org.komapper.core

import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals

class ClockProviderTest {
    @Test
    fun testDefaultClockProvider_NoArgsUsesSystemDefaultZone() {
        val providedClock = DefaultClockProvider().now()
        assertEquals(ZoneId.systemDefault(), providedClock.zone)
    }

    @Test
    fun testDefaultClockProvider_WithSourceClock() {
        val now = Instant.now()
        val clock = Clock.fixed(now, ZoneId.of("Etc/UTC"))
        val actualInstant = DefaultClockProvider(clock).now().instant()
        assertEquals(now, actualInstant)
    }

    @Test
    fun testDefaultClockProvider_FromZone() {
        val zone = ZoneId.of("Europe/Madrid")
        val providedClock = DefaultClockProvider(zone).now()
        assertEquals(zone, providedClock.zone)
    }

    @Test
    fun testDefaultClockProvider_ReturnsFixedClock() {
        val providedClock = DefaultClockProvider().now()
        val first = providedClock.instant()
        Thread.sleep(5)
        assertEquals(first, providedClock.instant())
    }
}
