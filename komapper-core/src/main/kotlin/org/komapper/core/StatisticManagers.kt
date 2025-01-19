package org.komapper.core

import org.komapper.core.spi.StatisticManagerFactory
import org.komapper.core.spi.findByPriority
import java.util.ServiceLoader

/**
 * The provider of [StatisticManager].
 */
object StatisticManagers {
    /**
     * Retrieves an instance of [StatisticManager] based on the available factory or defaults to a
     * [DefaultStatisticManager] if no suitable factory is found.
     *
     * @param enabled a flag indicating whether the statistics collection should be enabled; defaults to `false`
     * @return an instance of [StatisticManager]
     */
    fun get(enabled: Boolean = false): StatisticManager {
        val loader = ServiceLoader.load(StatisticManagerFactory::class.java)
        val factory = loader.findByPriority()
        return factory?.create(enabled) ?: DefaultStatisticManager(enabled)
    }
}
