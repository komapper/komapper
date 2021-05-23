package org.komapper.slf4j

import org.komapper.core.Logger
import org.komapper.core.spi.LoggerFactory

class Slf4jLoggerFactory : LoggerFactory {
    override fun create(): Logger {
        return Slf4jLogger()
    }
}
