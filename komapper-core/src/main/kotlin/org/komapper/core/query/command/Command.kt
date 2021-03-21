package org.komapper.core.query.command

import org.komapper.core.data.Statement

internal interface Command<T> {
    val statement: Statement
    fun execute(): T
}
