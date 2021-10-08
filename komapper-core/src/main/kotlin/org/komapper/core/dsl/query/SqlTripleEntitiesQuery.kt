package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlTripleEntitiesQuery<
    A : Any, A_META : EntityMetamodel<A, *, A_META>,
    B : Any, B_META : EntityMetamodel<B, *, B_META>,
    C : Any, C_META : EntityMetamodel<C, *, C_META>>(
    override val context: SqlSelectContext<A, *, A_META>,
    private val options: SqlSelectOptions,
    private val metamodels: Triple<A_META, B_META, C_META>
) : FlowSubquery<Triple<A, B?, C?>> {

    private val support: FlowSubquerySupport<Triple<A, B?, C?>> =
        FlowSubquerySupport(context) { SqlTripleEntitiesSetOperationQuery(it, metamodels = metamodels) }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlTripleEntitiesQuery(context, options, metamodels) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlTripleEntitiesQuery(context, options, metamodels)
    }

    override fun <R> collect(collect: suspend (Flow<Triple<A, B?, C?>>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlTripleEntitiesQuery(context, options, metamodels, collect)
        }
    }

    override fun except(other: SubqueryExpression<Triple<A, B?, C?>>): FlowSetOperationQuery<Triple<A, B?, C?>> {
        return support.except(other)
    }

    override fun intersect(other: SubqueryExpression<Triple<A, B?, C?>>): FlowSetOperationQuery<Triple<A, B?, C?>> {
        return support.intersect(other)
    }

    override fun union(other: SubqueryExpression<Triple<A, B?, C?>>): FlowSetOperationQuery<Triple<A, B?, C?>> {
        return support.union(other)
    }

    override fun unionAll(other: SubqueryExpression<Triple<A, B?, C?>>): FlowSetOperationQuery<Triple<A, B?, C?>> {
        return support.unionAll(other)
    }
}
