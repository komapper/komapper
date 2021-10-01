package org.komapper.core

import org.komapper.core.spi.LoggerFactory
import org.komapper.core.spi.findByPriority
import java.util.ServiceLoader

object Loggers {
    fun get(): Logger {
        val loader = ServiceLoader.load(LoggerFactory::class.java)
        val factory = loader.findByPriority()
        return factory?.create() ?: StdOutLogger()
    }
}
