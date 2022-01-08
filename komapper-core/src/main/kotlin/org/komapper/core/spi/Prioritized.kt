package org.komapper.core.spi

import org.komapper.core.ThreadSafe

/**
 * Represents a prioritized component.
 */
@ThreadSafe
interface Prioritized {
    companion object {
        const val defaultPriority: Int = 100
    }

    /**
     * Returns the priority of this component.
     * The higher the value, the higher the priority.
     *
     * @return the priority
     */
    val priority: Int get() = defaultPriority
}
