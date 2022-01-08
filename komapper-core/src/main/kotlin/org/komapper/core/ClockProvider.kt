package org.komapper.core

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * The provider of [Clock].
 *
 * The clock is used in insert queries and update queries.
 */
@ThreadSafe
fun interface ClockProvider {
    /**
     * @return the current time
     */
    fun now(): Clock
}

/**
 * The default implementation of [ClockProvider].
 */
class DefaultClockProvider(private val zoneId: ZoneId = ZoneId.systemDefault()) : ClockProvider {
    override fun now(): Clock {
        return Clock.fixed(Instant.now(), zoneId)
    }
}
