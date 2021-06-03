package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSelectOption

class SqlPairEntitiesQuery<A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>>(
    val context: SqlSelectContext<A, *, A_META>,
    val option: SqlSelectOption,
    val metamodels: Pair<A_META, B_META>
) : Subquery<Pair<A, B?>> {

    override val subqueryContext: SubqueryContext<Pair<A, B?>>
        get() = SubqueryContext.SqlSelect(context)

    override fun first(): Query<Pair<A, B?>> {
        TODO("Not yet implemented")
    }

    override fun firstOrNull(): Query<Pair<A, B?>?> {
        TODO("Not yet implemented")
    }

    override fun <R> collect(transform: (Sequence<Pair<A, B?>>) -> R): Query<R> {
        TODO("Not yet implemented")
    }

    override fun except(other: Subquery<Pair<A, B?>>): SetOperationQuery<Pair<A, B?>> {
        return setOperation(SqlSetOperationKind.EXCEPT, other)
    }

    override fun intersect(other: Subquery<Pair<A, B?>>): SetOperationQuery<Pair<A, B?>> {
        return setOperation(SqlSetOperationKind.INTERSECT, other)
    }

    override fun union(other: Subquery<Pair<A, B?>>): SetOperationQuery<Pair<A, B?>> {
        return setOperation(SqlSetOperationKind.UNION, other)
    }

    override fun unionAll(other: Subquery<Pair<A, B?>>): SetOperationQuery<Pair<A, B?>> {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        other: Subquery<Pair<A, B?>>
    ): SetOperationQuery<Pair<A, B?>> {
        val setOperatorContext = SqlSetOperationContext(kind, this.subqueryContext, other.subqueryContext)
        return SqlPairEntitiesSetOperationQuery(setOperatorContext, metamodels = metamodels)
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}