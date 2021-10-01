package org.komapper.core.spi

interface Prioritized {
    companion object {
        const val defaultPriority: Int = 100
    }
    val priority: Int get() = defaultPriority
}
