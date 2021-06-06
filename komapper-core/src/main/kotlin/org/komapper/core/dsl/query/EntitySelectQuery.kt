package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.element.Associator
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntitySelectOption
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration

interface EntitySelectQuery<ENTITY : Any> : Subquery<ENTITY> {

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> innerJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> leftJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY>
    fun orderBy(vararg expressions: ColumnExpression<*, *>): EntitySelectQuery<ENTITY>
    fun offset(offset: Int): EntitySelectQuery<ENTITY>
    fun limit(limit: Int): EntitySelectQuery<ENTITY>
    fun forUpdate(): EntitySelectQuery<ENTITY>
    fun option(configure: (EntitySelectOption) -> EntitySelectOption): EntitySelectQuery<ENTITY>
    fun <T : Any, S : Any> associate(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>,
        associator: Associator<T, S>
    ): EntitySelectQuery<ENTITY>

    fun asSqlQuery(): SqlSelectQuery<ENTITY>
}

data class EntitySelectQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val context: EntitySelectContext<ENTITY, ID, META>,
    val option: EntitySelectOption = EntitySelectOption.default
) :
    EntitySelectQuery<ENTITY> {

    companion object Message {
        fun entityMetamodelNotFound(parameterName: String): String {
            return "The '$parameterName' metamodel is not found. Bind it to this query in advance using the from or join clause."
        }
    }

    private val support: SelectQuerySupport<ENTITY, ID, META, EntitySelectContext<ENTITY, ID, META>> =
        SelectQuerySupport(context)

    override val subqueryContext = SubqueryContext.EntitySelect<ENTITY>(context)

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> innerJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.innerJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> leftJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.leftJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.orderBy(*expressions)
        return copy(context = newContext)
    }

    override fun offset(offset: Int): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.offset(offset)
        return copy(context = newContext)
    }

    override fun limit(limit: Int): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.limit(limit)
        return copy(context = newContext)
    }

    override fun forUpdate(): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun option(configure: (EntitySelectOption) -> EntitySelectOption): EntitySelectQueryImpl<ENTITY, ID, META> {
        return copy(option = configure(option))
    }

    override fun <T : Any, S : Any> associate(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>,
        associator: Associator<T, S>
    ): EntitySelectQueryImpl<ENTITY, ID, META> {
        val metamodels = context.getEntityMetamodels()
        require(metamodel1 in metamodels) { entityMetamodelNotFound("metamodel1") }
        require(metamodel2 in metamodels) { entityMetamodelNotFound("metamodel2") }
        @Suppress("UNCHECKED_CAST")
        val newContext = context.putAssociator(metamodel1 to metamodel2, associator as Associator<Any, Any>)
        return copy(context = newContext)
    }

    override fun first(): Query<ENTITY> {
        return Collect(context, option) { it.first() }
    }

    override fun firstOrNull(): Query<ENTITY?> {
        return Collect(context, option) { it.firstOrNull() }
    }

    override fun <R> collect(collect: suspend (Flow<ENTITY>) -> R): Query<R> {
        return Collect(context, option, collect)
    }

    override fun except(other: Subquery<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.EXCEPT, this, other)
    }

    override fun intersect(other: Subquery<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.INTERSECT, this, other)
    }

    override fun union(other: Subquery<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION, this, other)
    }

    override fun unionAll(other: Subquery<ENTITY>): SetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION_ALL, this, other)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        left: Subquery<ENTITY>,
        right: Subquery<ENTITY>
    ): SqlSetOperationQuery<ENTITY> {
        val setOperatorContext = SqlSetOperationContext(kind, left.subqueryContext, right.subqueryContext)
        return SqlSetOperationQueryImpl(setOperatorContext, metamodel = context.target)
    }

    override fun asSqlQuery(): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(context.asSqlSelectContext(), option.asSqlSelectOption())
    }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }

    class Collect<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>(
        val context: EntitySelectContext<ENTITY, ID, META>,
        val option: EntitySelectOption,
        val transform: suspend (Flow<ENTITY>) -> R
    ) : Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }
}
