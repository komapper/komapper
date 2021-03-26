package org.komapper.core.query.command

internal fun interface Command<T> {
    fun execute(): T
}
