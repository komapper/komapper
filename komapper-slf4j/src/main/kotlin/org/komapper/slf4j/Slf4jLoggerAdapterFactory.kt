package org.komapper.slf4j

import org.komapper.core.spi.LoggerFactory

class Slf4jLoggerAdapterFactory : LoggerFactory {
    override fun create(): Slf4jLoggerAdapter {
        return Slf4jLoggerAdapter()
    }
}
