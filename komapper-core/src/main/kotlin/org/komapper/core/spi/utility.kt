package org.komapper.core.spi

/**
 * Find the most prioritized element from [Iterable].
 * @param T the element type
 * @return the most prioritized element
 */
fun <T : Prioritized> Iterable<T>.findByPriority(): T? {
    return this.maxByOrNull { it.priority }
}
