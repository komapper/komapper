package org.komapper.core

import java.time.Clock
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
class DefaultClockProvider(private val source: Clock = Clock.systemDefaultZone()) : ClockProvider {
    constructor(zoneId: ZoneId) : this(Clock.system(zoneId))

    override fun now(): Clock {
        return Clock.fixed(source.instant(), source.zone)
    }
}
