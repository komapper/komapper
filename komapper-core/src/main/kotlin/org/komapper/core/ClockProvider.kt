package org.komapper.core

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * The provider of [Clock].
 */
@ThreadSafe
fun interface ClockProvider {
    /**
     * Returns the current time.
     * @return the current time
     */
    fun now(): Clock
}

class DefaultClockProvider(private val zoneId: ZoneId = ZoneId.systemDefault()) : ClockProvider {
    override fun now(): Clock {
        return Clock.fixed(Instant.now(), zoneId)
    }
}
