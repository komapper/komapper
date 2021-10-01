package org.komapper.core.spi

import org.komapper.core.Logger
import org.komapper.core.ThreadSafe

@ThreadSafe
interface LoggerFactory : Prioritized {
    fun create(): Logger
}
