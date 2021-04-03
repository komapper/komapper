package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.config.EmptyDialect
import org.komapper.core.data.Statement

interface Query<T> {
    fun execute(config: DatabaseConfig): T
    fun statement(dialect: Dialect = EmptyDialect()): Statement
}

interface ListQuery<T> : Query<List<T>> {
    fun first(): Query<T>
    fun firstOrNull(): Query<T?>
    fun <R> transform(transformer: (Sequence<T>) -> R): Query<R>
}
