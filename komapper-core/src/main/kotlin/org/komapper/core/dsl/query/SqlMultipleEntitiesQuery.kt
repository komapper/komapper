package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSelectOption

class SqlMultipleEntitiesQuery(
    val context: SqlSelectContext<*, *, *>,
    val option: SqlSelectOption,
    val metamodels: List<EntityMetamodel<*, *, *>>
) : Subquery<Entities> {

    override val subqueryContext: SubqueryContext<Entities>
        get() = SubqueryContext.SqlSelect(context)

    override fun first(): Query<Entities> {
        TODO("Not yet implemented")
    }

    override fun firstOrNull(): Query<Entities?> {
        TODO("Not yet implemented")
    }

    override fun <R> collect(collect: suspend (Flow<Entities>) -> R): Query<R> {
        TODO("Not yet implemented")
    }

    override fun except(other: Subquery<Entities>): SetOperationQuery<Entities> {
        TODO("Not yet implemented")
    }

    override fun intersect(other: Subquery<Entities>): SetOperationQuery<Entities> {
        TODO("Not yet implemented")
    }

    override fun union(other: Subquery<Entities>): SetOperationQuery<Entities> {
        TODO("Not yet implemented")
    }

    override fun unionAll(other: Subquery<Entities>): SetOperationQuery<Entities> {
        TODO("Not yet implemented")
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }
}
