package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.config.EmptyDialect
import org.komapper.core.data.Statement

interface Query<T> {
    fun execute(config: DatabaseConfig): T
    fun statement(dialect: Dialect = EmptyDialect()): Statement

    fun <S> map(transformer: (T) -> S): Query<S> {
        return object : Query<S> {
            override fun execute(config: DatabaseConfig): S {
                val value = this@Query.execute(config)
                return transformer(value)
            }

            override fun statement(dialect: Dialect): Statement {
                return this@Query.statement(dialect)
            }
        }
    }

    fun <R> flatMap(transformer: (T) -> Query<R>): Query<R> {
        return object : Query<R> {
            override fun execute(config: DatabaseConfig): R {
                val result = this@Query.execute(config)
                return transformer(result).execute(config)
            }

            override fun statement(dialect: Dialect): Statement {
                return this@Query.statement(dialect)
            }
        }
    }

    infix operator fun <S> plus(other: Query<S>): Query<S> {
        return object : Query<S> {
            override fun execute(config: DatabaseConfig): S {
                this@Query.execute(config)
                return other.execute(config)
            }

            override fun statement(dialect: Dialect): Statement {
                return this@Query.statement(dialect) + other.statement(dialect)
            }
        }
    }

    companion object Empty : Query<Unit> {
        private val emptyStatement = Statement("")

        override fun execute(config: DatabaseConfig) {
        }

        override fun statement(dialect: Dialect): Statement {
            return emptyStatement
        }
    }
}

interface ListQuery<T> : Query<List<T>> {
    fun first(): Query<T>
    fun firstOrNull(): Query<T?>
    fun <R> transform(transformer: (Sequence<T>) -> R): Query<R>
}
