package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.SqlSetOperationComponent

interface Query<T> {
    fun run(config: DatabaseConfig): T
    fun dryRun(config: DatabaseConfig = DryRunDatabaseConfig): Statement
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
