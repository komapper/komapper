package org.komapper.core

import java.time.LocalTime

/**
 * The Logger.
 */
@ThreadSafe
interface Logger {
    fun trace(category: String, message: () -> String)
    fun debug(category: String, message: () -> String)
    fun info(category: String, message: () -> String)
    fun warn(category: String, message: () -> String)
    fun error(category: String, message: () -> String)
}

/**
 * Simple [Logger] implementation printing log messages to the standard output.
 */
class StdOutLogger : Logger {
    override fun trace(category: String, message: () -> String) {
        log(category, message)
    }

    override fun debug(category: String, message: () -> String) {
        log(category, message)
    }

    override fun info(category: String, message: () -> String) {
        log(category, message)
    }

    override fun warn(category: String, message: () -> String) {
        log(category, message)
    }

    override fun error(category: String, message: () -> String) {
        log(category, message)
    }

    private fun log(category: String, message: () -> String) {
        val threadName = Thread.currentThread().name
        println("${LocalTime.now()} [$threadName] $category - ${message()}")
    }
}
