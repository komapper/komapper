package org.komapper.core.spi

import org.komapper.core.Logger

interface LoggerFactory {
    fun create(): Logger
}
