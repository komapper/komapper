package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class SqlMultipleEntitiesSetOperationQuery(
    private val context: SqlSetOperationContext<Entities>,
    private val options: SqlSetOperationOptions = SqlSetOperationOptions.default,
    private val metamodels: List<EntityMetamodel<*, *, *>>
) : FlowableSetOperationQuery<Entities> {

    private val support: SqlSetOperationQuerySupport<Entities> = SqlSetOperationQuerySupport(context)

    override val subqueryContext: SubqueryContext<Entities> = SubqueryContext.SqlSetOperation(context)

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlMultipleEntitiesSetOperationQuery(context, options, metamodels) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<Entities>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlMultipleEntitiesSetOperationQuery(context, options, metamodels, collect)
        }
    }

    override fun asFlowQuery(): FlowQuery<Entities> = object : FlowQuery<Entities> {
        override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlMultipleEntitiesSetOperationQuery(context, options, metamodels)
        }
    }

    override fun except(other: Subquery<Entities>): FlowableSetOperationQuery<Entities> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<Entities>): FlowableSetOperationQuery<Entities> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<Entities>): FlowableSetOperationQuery<Entities> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<Entities>): FlowableSetOperationQuery<Entities> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowableSetOperationQuery<Entities> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): FlowableSetOperationQuery<Entities> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): FlowableSetOperationQuery<Entities> {
        return copy(options = configurator(options))
    }
}
