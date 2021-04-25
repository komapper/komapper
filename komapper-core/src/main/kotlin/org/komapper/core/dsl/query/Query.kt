package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.DryRunDatabaseConfig
import org.komapper.core.dsl.context.SubqueryContext

interface Query<T> {
    fun run(holder: DatabaseConfigHolder): T
    fun dryRun(holder: DatabaseConfigHolder = DryRunDatabaseConfig): String
}

interface ListQuery<T> : Query<List<T>> {
    fun first(): Query<T>
    fun firstOrNull(): Query<T?>
    fun <R> collect(transformer: (Sequence<T>) -> R): Query<R>
}

interface Subquery<T> : ListQuery<T> {
    val subqueryContext: SubqueryContext<T>
    infix fun except(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun intersect(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun union(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun unionAll(other: Subquery<T>): SqlSetOperationQuery<T>
}
