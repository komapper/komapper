package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

interface SqlSetOperationQuery<ENTITY : Any> : FlowSetOperationQuery<ENTITY>

internal data class SqlSetOperationQueryImpl<ENTITY : Any>(
    private val context: SqlSetOperationContext<ENTITY>,
    private val options: SqlSetOperationOptions = SqlSetOperationOptions.default,
    private val metamodel: EntityMetamodel<ENTITY, *, *>
) : SqlSetOperationQuery<ENTITY>, FlowQuery<ENTITY> {

    override val subqueryContext = SubqueryContext.SqlSetOperation(context)

    private val support: SqlSetOperationQuerySupport<ENTITY> = SqlSetOperationQuerySupport(context)

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlSetOperationQuery(context, options, metamodel) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlSetOperationQuery(context, options, metamodel)
    }

    override fun <R> collect(collect: suspend (Flow<ENTITY>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlSetOperationQuery(context, options, metamodel, collect)
        }
    }

    override fun except(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: SortExpression): SqlSetOperationQuery<ENTITY> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): SqlSetOperationQuery<ENTITY> {
        return copy(options = configurator(options))
    }
}
