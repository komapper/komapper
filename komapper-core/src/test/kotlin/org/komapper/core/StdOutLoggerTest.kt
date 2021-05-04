package org.komapper.core

import org.junit.jupiter.api.Test

class StdOutLoggerTest {
    private val logger = StdOutLogger()

    @Test
    fun test() {
        logger.debug("test") { "hello, world" }
    }
}
