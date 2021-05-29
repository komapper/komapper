package org.komapper.r2dbc.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.r2dbc.R2dbcDatabaseConfig

interface Query<T> {
    suspend fun run(config: R2dbcDatabaseConfig): T
    fun dryRun(config: R2dbcDatabaseConfig): String
}

interface FlowQuery<T> : Query<Flow<T>>

interface Subquery<T> : FlowQuery<T>, SubqueryExpression<T> {
    override val subqueryContext: SubqueryContext<T>
    infix fun except(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun intersect(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun union(other: Subquery<T>): SqlSetOperationQuery<T>
    infix fun unionAll(other: Subquery<T>): SqlSetOperationQuery<T>
}
