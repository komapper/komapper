package org.komapper.core.dsl.command

internal fun interface Command<T> {
    fun execute(): T
}
