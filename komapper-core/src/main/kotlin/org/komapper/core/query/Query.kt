package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.config.EmptyDialect
import org.komapper.core.data.Statement

interface Query<T> {
    fun run(config: DatabaseConfig): T
    fun peek(dialect: Dialect = EmptyDialect()): Statement
}

interface ListQuery<T> : Query<List<T>> {
    fun first(): Query<T>
    fun firstOrNull(): Query<T?>
    fun <R> transform(transformer: (Sequence<T>) -> R): Query<R>
}
