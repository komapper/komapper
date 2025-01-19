package org.komapper.core.spi

import org.komapper.core.StatisticManager
import org.komapper.core.ThreadSafe

/**
 * The factory of [StatisticManager].
 */
@ThreadSafe
interface StatisticManagerFactory : Prioritized {
    fun create(enabled: Boolean = false): StatisticManager
}
