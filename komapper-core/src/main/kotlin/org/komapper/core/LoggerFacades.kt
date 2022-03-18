package org.komapper.core

import org.komapper.core.spi.LoggerFacadeFactory
import org.komapper.core.spi.findByPriority
import java.util.ServiceLoader

/**
 * The provider of [LoggerFacade].
 */
object LoggerFacades {
    fun get(logger: Logger): LoggerFacade {
        val loader = ServiceLoader.load(LoggerFacadeFactory::class.java)
        val factory = loader.findByPriority()
        return factory?.create(logger) ?: DefaultLoggerFacade(logger)
    }
}
