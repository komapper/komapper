package org.komapper.jdbc.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.DryRunDatabaseConfig

@ThreadSafe
interface Query<T> {
    fun run(config: DatabaseConfig): T
    fun dryRun(config: DatabaseConfig = DryRunDatabaseConfig): String

    fun <R> flatMap(transform: (T) -> Query<R>): Query<R> {
        val self = this
        return object : Query<R> {
            override fun run(config: DatabaseConfig): R {
                val result = self.run(config)
                return transform(result).run(config)
            }

            override fun dryRun(config: DatabaseConfig): String {
                return self.dryRun(config)
            }
        }
    }

    fun <R> flatZip(transform: (T) -> Query<R>): Query<Pair<T, R>> {
        val self = this
        return object : Query<Pair<T, R>> {
            override fun run(config: DatabaseConfig): Pair<T, R> {
                val result = self.run(config)
                return result to transform(result).run(config)
            }

            override fun dryRun(config: DatabaseConfig): String {
                return self.dryRun(config)
            }
        }
    }

    infix operator fun <S> plus(other: Query<S>): Query<S> {
        val self = this
        return object : Query<S> {
            override fun run(config: DatabaseConfig): S {
                self.run(config)
                return other.run(config)
            }

            override fun dryRun(config: DatabaseConfig): String {
                return self.dryRun(config) + other.dryRun(config)
            }
        }
    }
}

interface ListQuery<T> : Query<List<T>> {
    fun first(): Query<T>
    fun firstOrNull(): Query<T?>
    fun <R> collect(transform: (Sequence<T>) -> R): Query<R>
}

interface Subquery<T> : ListQuery<T>, SubqueryExpression<T> {
    override val subqueryContext: SubqueryContext<T>
    infix fun except(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun intersect(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun union(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun unionAll(other: Subquery<T>): SqlSetOperationQuery<T>
}
