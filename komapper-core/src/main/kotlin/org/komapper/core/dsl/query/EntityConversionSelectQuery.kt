package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal class EntityConversionSelectQuery<ENTITY : Any>(
    override val context: SelectContext<*, *, *>,
    private val metamodel: EntityMetamodel<ENTITY, *, *>,
) : FlowSubquery<ENTITY> {

    private val support: FlowSubquerySupport<ENTITY> =
        FlowSubquerySupport(context) { EntityConversionSetOperationQuery(it, metamodel) }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityConversionSelectQuery(context, metamodel)
    }

    override fun <R> collect(collect: suspend (Flow<ENTITY>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.entityConversionSelectQuery(context, metamodel, collect)
        }
    }

    override fun except(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return support.except(other)
    }

    override fun intersect(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return support.intersect(other)
    }

    override fun union(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return support.union(other)
    }

    override fun unionAll(other: SubqueryExpression<ENTITY>): FlowSetOperationQuery<ENTITY> {
        return support.unionAll(other)
    }
}
