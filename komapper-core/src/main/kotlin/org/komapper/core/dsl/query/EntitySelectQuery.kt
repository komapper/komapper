package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.declaration.OnDeclaration
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntitySelectOptions
import org.komapper.core.dsl.visitor.QueryVisitor

interface EntitySelectQuery<ENTITY : Any> : Subquery<ENTITY> {

    fun innerJoin(metamodel: EntityMetamodel<*, *, *>, on: OnDeclaration): EntitySelectQuery<ENTITY>
    fun leftJoin(metamodel: EntityMetamodel<*, *, *>, on: OnDeclaration): EntitySelectQuery<ENTITY>
    fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY>
    fun orderBy(vararg expressions: SortExpression): EntitySelectQuery<ENTITY>
    fun offset(offset: Int): EntitySelectQuery<ENTITY>
    fun limit(limit: Int): EntitySelectQuery<ENTITY>
    fun forUpdate(): EntitySelectQuery<ENTITY>
    fun options(configure: (EntitySelectOptions) -> EntitySelectOptions): EntitySelectQuery<ENTITY>
    fun <T : Any, S : Any> associate(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>,
    ): EntityAggregateQuery

    fun asSqlQuery(): SqlSelectQuery<ENTITY>
}

internal data class EntitySelectQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    override val context: EntitySelectContext<ENTITY, ID, META>,
    private val options: EntitySelectOptions = EntitySelectOptions.default
) :
    EntitySelectQuery<ENTITY> {

    companion object Message {
        fun entityMetamodelNotFound(parameterName: String): String {
            return "The '$parameterName' metamodel is not found. Bind it to this query in advance using the from or join clause."
        }
    }

    private val support: SelectQuerySupport<ENTITY, ID, META, EntitySelectContext<ENTITY, ID, META>> =
        SelectQuerySupport(context)

    override fun innerJoin(metamodel: EntityMetamodel<*, *, *>, on: OnDeclaration): EntitySelectQuery<ENTITY> {
        val newContext = support.innerJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun leftJoin(metamodel: EntityMetamodel<*, *, *>, on: OnDeclaration): EntitySelectQuery<ENTITY> {
        val newContext = support.leftJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun orderBy(vararg expressions: SortExpression): EntitySelectQuery<ENTITY> {
        val newContext = support.orderBy(*expressions)
        return copy(context = newContext)
    }

    override fun offset(offset: Int): EntitySelectQuery<ENTITY> {
        val newContext = support.offset(offset)
        return copy(context = newContext)
    }

    override fun limit(limit: Int): EntitySelectQuery<ENTITY> {
        val newContext = support.limit(limit)
        return copy(context = newContext)
    }

    override fun forUpdate(): EntitySelectQuery<ENTITY> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun options(configure: (EntitySelectOptions) -> EntitySelectOptions): EntitySelectQuery<ENTITY> {
        return copy(options = configure(options))
    }

    override fun <T : Any, S : Any> associate(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>,
    ): EntityAggregateQuery {
        return AggregateQueryImpl(context, options).associate(metamodel1, metamodel2)
    }

    override fun <R> collect(collect: suspend (Flow<ENTITY>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.entitySelectQuery(context, options, collect)
        }
    }

    override fun except(other: SubqueryExpression<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.EXCEPT, this, other)
    }

    override fun intersect(other: SubqueryExpression<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.INTERSECT, this, other)
    }

    override fun union(other: SubqueryExpression<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION, this, other)
    }

    override fun unionAll(other: SubqueryExpression<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION_ALL, this, other)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        left: SubqueryExpression<ENTITY>,
        right: SubqueryExpression<ENTITY>
    ): SqlSetOperationQuery<ENTITY> {
        val setOperatorContext = SqlSetOperationContext(kind, left.context, right.context)
        return SqlSetOperationQueryImpl(setOperatorContext, metamodel = context.target)
    }

    override fun asSqlQuery(): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(context.asSqlSelectContext(), options.asSqlSelectOption())
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entitySelectQuery(context, options) { it.toList() }
    }
}
