package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationComponent
import org.komapper.core.dsl.context.SqlSetOperationContext

interface Query<T> {
    fun run(config: DatabaseConfig): T
    fun dryRun(config: DatabaseConfig = DryRunDatabaseConfig): Statement
}

interface ListQuery<T> : Query<List<T>> {
    fun first(): Query<T>
    fun firstOrNull(): Query<T?>
    fun <R> transform(transformer: (Sequence<T>) -> R): Query<R>
}

interface Subquery<T> : ListQuery<T> {
    val subqueryContext: SubqueryContext
}

interface SqlSetOperandQuery<T> : Subquery<T> {
    val setOperationComponent: SqlSetOperationComponent<T>

    infix fun except(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T>
    infix fun intersect(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T>
    infix fun union(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T>
    infix fun unionAll(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T>
}

sealed class SubqueryContext {
    internal data class EntitySelect(val context: EntitySelectContext<*>) : SubqueryContext()
    internal data class SqlSelect(val context: SqlSelectContext<*>) : SubqueryContext()
    internal data class SqlSetOperation(val context: SqlSetOperationContext<*>) : SubqueryContext()
}
