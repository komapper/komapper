package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement

interface Queryable<T> {
    fun run(config: DatabaseConfig): T
    fun toStatement(config: DatabaseConfig): Statement
}

interface ListQueryable<T> : Queryable<List<T>> {
    fun first(): Queryable<T>
    fun firstOrNull(): Queryable<T?>
    fun <R> transform(transformer: (Sequence<T>) -> R): Queryable<R>
}
