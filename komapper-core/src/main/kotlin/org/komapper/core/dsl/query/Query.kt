package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.config.EmptyDialect
import org.komapper.core.data.Statement

interface Query<T> {
    fun run(config: DatabaseConfig): T
    fun toStatement(dialect: Dialect = EmptyDialect()): Statement

    fun peek(dialect: Dialect = EmptyDialect(), block: (Statement) -> Unit): Query<T> {
        block(toStatement(dialect))
        return this
    }
}

interface ListQuery<T> : Query<List<T>> {
    fun first(): Query<T>
    fun firstOrNull(): Query<T?>
    fun <R> transform(transformer: (Sequence<T>) -> R): Query<R>

    override fun peek(dialect: Dialect, block: (Statement) -> Unit): ListQuery<T> {
        super.peek(dialect, block)
        return this
    }
}
