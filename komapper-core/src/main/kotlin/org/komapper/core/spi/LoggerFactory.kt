package org.komapper.core.spi

import org.komapper.core.Logger
import org.komapper.core.StdOutLogger
import org.komapper.core.ThreadSafe
import java.util.ServiceLoader

@ThreadSafe
interface LoggerFactory {
    fun create(): Logger
}

object LoggerProvider {
    fun get(): Logger {
        val loader = ServiceLoader.load(LoggerFactory::class.java)
        val factory = loader.firstOrNull()
        return factory?.create() ?: StdOutLogger()
    }
}
