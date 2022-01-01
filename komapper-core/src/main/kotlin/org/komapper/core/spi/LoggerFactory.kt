package org.komapper.core.spi

import org.komapper.core.Logger
import org.komapper.core.ThreadSafe

/**
 * The factory of [Logger].
 */
@ThreadSafe
interface LoggerFactory : Prioritized {
    fun create(): Logger
}
