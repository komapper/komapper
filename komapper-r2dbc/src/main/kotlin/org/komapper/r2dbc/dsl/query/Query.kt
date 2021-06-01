package org.komapper.r2dbc.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.r2dbc.DryRunR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabaseConfig

interface Query<T> {
    suspend fun run(config: R2dbcDatabaseConfig): T
    fun dryRun(config: R2dbcDatabaseConfig = DryRunR2dbcDatabaseConfig): String

    fun <R> flatMap(transform: (T) -> Query<R>): Query<R> {
        val self = this
        return object : Query<R> {
            override suspend fun run(config: R2dbcDatabaseConfig): R {
                val result = self.run(config)
                return transform(result).run(config)
            }

            override fun dryRun(config: R2dbcDatabaseConfig): String {
                return self.dryRun(config)
            }
        }
    }

    fun <R> flatZip(transform: (T) -> Query<R>): Query<Pair<T, R>> {
        val self = this
        return object : Query<Pair<T, R>> {
            override suspend fun run(config: R2dbcDatabaseConfig): Pair<T, R> {
                val result = self.run(config)
                return result to transform(result).run(config)
            }

            override fun dryRun(config: R2dbcDatabaseConfig): String {
                return self.dryRun(config)
            }
        }
    }

    infix operator fun <S> plus(other: Query<S>): Query<S> {
        val self = this
        return object : Query<S> {
            override suspend fun run(config: R2dbcDatabaseConfig): S {
                self.run(config)
                return other.run(config)
            }

            override fun dryRun(config: R2dbcDatabaseConfig): String {
                return self.dryRun(config) + other.dryRun(config)
            }
        }
    }
}

interface FlowQuery<T> : Query<Flow<T>> {
    fun first(): Query<T> {
        val query = this
        return object : Query<T> {
            override suspend fun run(config: R2dbcDatabaseConfig): T {
                return query.run(config).first()
            }

            override fun dryRun(config: R2dbcDatabaseConfig): String {
                return query.dryRun(config)
            }
        }
    }

    fun firstOrNull(): Query<T?> {
        val query = this
        return object : Query<T?> {
            override suspend fun run(config: R2dbcDatabaseConfig): T? {
                return query.run(config).firstOrNull()
            }

            override fun dryRun(config: R2dbcDatabaseConfig): String {
                return query.dryRun(config)
            }
        }
    }
}

interface Subquery<T> : FlowQuery<T>, SubqueryExpression<T> {
    override val subqueryContext: SubqueryContext<T>
    infix fun except(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun intersect(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun union(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun unionAll(other: Subquery<T>): SqlSetOperationQuery<T>
}
