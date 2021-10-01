package org.komapper.core.spi

fun <T : Prioritized> Iterable<T>.findByPriority(): T? {
    return this.maxByOrNull { it.priority }
}
