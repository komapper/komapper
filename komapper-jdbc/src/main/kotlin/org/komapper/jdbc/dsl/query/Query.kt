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
