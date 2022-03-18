package org.komapper.core.spi

import org.komapper.core.Logger
import org.komapper.core.LoggerFacade
import org.komapper.core.ThreadSafe

/**
 * The factory of [LoggerFacade].
 */
@ThreadSafe
interface LoggerFacadeFactory : Prioritized {
    fun create(logger: Logger): LoggerFacade
}
