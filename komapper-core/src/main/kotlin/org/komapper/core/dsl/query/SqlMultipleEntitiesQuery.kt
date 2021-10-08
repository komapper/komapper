package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlMultipleEntitiesQuery(
    override val context: SqlSelectContext<*, *, *>,
    private val options: SqlSelectOptions,
    private val metamodels: List<EntityMetamodel<*, *, *>>
) : FlowSubquery<Entities> {

    private val support: FlowSubquerySupport<Entities> =
        FlowSubquerySupport(context) { SqlMultipleEntitiesSetOperationQuery(it, metamodels = metamodels) }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlMultipleEntitiesQuery(context, options, metamodels) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlMultipleEntitiesQuery(context, options, metamodels)
    }

    override fun <R> collect(collect: suspend (Flow<Entities>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlMultipleEntitiesQuery(context, options, metamodels, collect)
        }
    }

    override fun except(other: SubqueryExpression<Entities>): FlowSetOperationQuery<Entities> {
        return support.except(other)
    }

    override fun intersect(other: SubqueryExpression<Entities>): FlowSetOperationQuery<Entities> {
        return support.intersect(other)
    }

    override fun union(other: SubqueryExpression<Entities>): FlowSetOperationQuery<Entities> {
        return support.union(other)
    }

    override fun unionAll(other: SubqueryExpression<Entities>): FlowSetOperationQuery<Entities> {
        return support.unionAll(other)
    }
}
