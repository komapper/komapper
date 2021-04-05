package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.config.EmptyDialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.SqlSetOperationComponent

interface Query<T> {
    fun run(config: DatabaseConfig): T
    fun dryRun(dialect: Dialect = EmptyDialect()): Statement

    fun <S> map(transformer: (T) -> S): Query<S> {
        return object : Query<S> {
            override fun run(config: DatabaseConfig): S {
                val value = this@Query.run(config)
                return transformer(value)
            }

            override fun dryRun(dialect: Dialect): Statement {
                return this@Query.dryRun(dialect)
            }
        }
    }

    fun <R> flatMap(transformer: (T) -> Query<R>): Query<R> {
        return object : Query<R> {
            override fun run(config: DatabaseConfig): R {
                val result = this@Query.run(config)
                return transformer(result).run(config)
            }

            override fun dryRun(dialect: Dialect): Statement {
                return this@Query.dryRun(dialect)
            }
        }
    }

    fun <R> flatZip(transformer: (T) -> Query<R>): Query<Pair<T, R>> {
        return object : Query<Pair<T, R>> {
            override fun run(config: DatabaseConfig): Pair<T, R> {
                val result = this@Query.run(config)
                return result to transformer(result).run(config)
            }

            override fun dryRun(dialect: Dialect): Statement {
                return this@Query.dryRun(dialect)
            }
        }
    }

    infix operator fun <S> plus(other: Query<S>): Query<S> {
        return object : Query<S> {
            override fun run(config: DatabaseConfig): S {
                this@Query.run(config)
                return other.run(config)
            }

            override fun dryRun(dialect: Dialect): Statement {
                return this@Query.dryRun(dialect) + other.dryRun(dialect)
            }
        }
    }

    companion object Empty : Query<Unit> {
        private val emptyStatement = Statement("")

        override fun run(config: DatabaseConfig) {
        }

        override fun dryRun(dialect: Dialect): Statement {
            return emptyStatement
        }
    }
}

interface ListQuery<T> : Query<List<T>> {
    fun first(): Query<T>
    fun firstOrNull(): Query<T?>
    fun <R> transform(transformer: (Sequence<T>) -> R): Query<R>
}

interface SqlSetOperandQuery<T> : ListQuery<T> {
    val setOperationComponent: SqlSetOperationComponent<T>

    infix fun except(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T>
    infix fun intersect(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T>
    infix fun union(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T>
    infix fun unionAll(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T>
}
