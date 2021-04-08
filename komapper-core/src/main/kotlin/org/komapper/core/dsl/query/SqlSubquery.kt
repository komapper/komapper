package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.scope.HavingDeclaration
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration

interface SqlSubquery<ENTITY : Any> : SqlSubqueryProjection {
    fun <OTHER_ENTITY : Any> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSubquery<ENTITY>

    fun <OTHER_ENTITY : Any> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSubquery<ENTITY>

    fun where(declaration: WhereDeclaration): SqlSubquery<ENTITY>
    fun groupBy(vararg expressions: PropertyExpression<*>): SqlSubquery<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSubquery<ENTITY>
    fun orderBy(vararg expressions: PropertyExpression<*>): SqlSubquery<ENTITY>
    fun offset(value: Int): SqlSubquery<ENTITY>
    fun limit(value: Int): SqlSubquery<ENTITY>
    fun forUpdate(): SqlSubquery<ENTITY>
    fun select(p: PropertyExpression<*>): OneProperty
    fun select(p1: PropertyExpression<*>, p2: PropertyExpression<*>): TwoProperties
}

internal data class SqlSubqueryImpl<ENTITY : Any>(
    private val context: SqlSelectContext<ENTITY>
) :
    SqlSubquery<ENTITY> {

    override val contextHolder = ContextHolder(context)

    private val support: SelectQuerySupport<ENTITY, SqlSelectContext<ENTITY>> = SelectQuerySupport(context)

    override fun <OTHER_ENTITY : Any> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSubqueryImpl<ENTITY> {
        val newContext = support.innerJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSubqueryImpl<ENTITY> {
        val newContext = support.leftJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlSubqueryImpl<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun groupBy(vararg expressions: PropertyExpression<*>): SqlSubqueryImpl<ENTITY> {
        val newContext = context.copy(groupBy = expressions.toList())
        return copy(context = newContext)
    }

    override fun having(declaration: HavingDeclaration): SqlSubqueryImpl<ENTITY> {
        val scope = HavingScope()
        declaration(scope)
        val newContext = context.addHaving(scope.toList())
        return copy(context = newContext)
    }

    override fun orderBy(vararg expressions: PropertyExpression<*>): SqlSubqueryImpl<ENTITY> {
        val newContext = support.orderBy(*expressions)
        return copy(context = newContext)
    }

    override fun offset(value: Int): SqlSubqueryImpl<ENTITY> {
        val newContext = support.offset(value)
        return copy(context = newContext)
    }

    override fun limit(value: Int): SqlSubqueryImpl<ENTITY> {
        val newContext = support.limit(value)
        return copy(context = newContext)
    }

    override fun forUpdate(): SqlSubqueryImpl<ENTITY> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun select(p: PropertyExpression<*>): OneProperty {
        val newContext = context.setProperty(p)
        return SqlSubqueryProjectionImpl(ContextHolder(newContext))
    }

    override fun select(p1: PropertyExpression<*>, p2: PropertyExpression<*>): TwoProperties {
        val newContext = context.setProperties(listOf(p1, p2))
        return SqlSubqueryProjectionImpl(ContextHolder(newContext))
    }
}
