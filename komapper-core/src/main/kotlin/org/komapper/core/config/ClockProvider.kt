package org.komapper.core.config

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

fun interface ClockProvider {
    fun now(): Clock
}

class DefaultClockProvider(private val zoneId: ZoneId = ZoneId.systemDefault()) : ClockProvider {
    override fun now(): Clock {
        return Clock.fixed(Instant.now(), zoneId)
    }
}
