package org.komapper.core

import kotlin.test.Test

class StdOutLoggerTest {
    private val logger = StdOutLogger()

    @Test
    fun test() {
        logger.debug("test") { "hello, world" }
    }
}
