package org.komapper.core.query

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.context.SqlSelectContext
import org.komapper.core.query.scope.HavingDeclaration
import org.komapper.core.query.scope.HavingScope
import org.komapper.core.query.scope.JoinDeclaration
import org.komapper.core.query.scope.WhereDeclaration

interface SelectSubquery<ENTITY> : Projection {
    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SelectSubquery<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SelectSubquery<ENTITY>

    fun where(declaration: WhereDeclaration): SelectSubquery<ENTITY>
    fun groupBy(vararg items: ColumnInfo<*>): SelectSubquery<ENTITY>
    fun having(declaration: HavingDeclaration): SelectSubquery<ENTITY>
    fun orderBy(vararg items: ColumnInfo<*>): SelectSubquery<ENTITY>
    fun offset(value: Int): SelectSubquery<ENTITY>
    fun limit(value: Int): SelectSubquery<ENTITY>
    fun forUpdate(): SelectSubquery<ENTITY>
    fun select(columnInfo: ColumnInfo<*>): SingleColumnProjection
}

internal data class SelectSubqueryImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: SqlSelectContext<ENTITY> = SqlSelectContext(entityMetamodel)
) :
    SelectSubquery<ENTITY> {

    override val contextHolder = ContextHolder(context)

    private val support: SelectQuerySupport<ENTITY, SqlSelectContext<ENTITY>> = SelectQuerySupport(context)

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SelectSubqueryImpl<ENTITY> {
        val newContext = support.innerJoin(entityMetamodel, declaration)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): SelectSubqueryImpl<ENTITY> {
        val newContext = support.leftJoin(entityMetamodel, declaration)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SelectSubqueryImpl<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun groupBy(vararg items: ColumnInfo<*>): SelectSubqueryImpl<ENTITY> {
        val newContext = context.copy(groupBy = items.toList())
        return copy(context = newContext)
    }

    override fun having(declaration: HavingDeclaration): SelectSubqueryImpl<ENTITY> {
        val scope = HavingScope()
        declaration(scope)
        val newContext = context.addHaving(scope.criteria.toList())
        return copy(context = newContext)
    }

    override fun orderBy(vararg items: ColumnInfo<*>): SelectSubqueryImpl<ENTITY> {
        val newContext = support.orderBy(*items)
        return copy(context = newContext)
    }

    override fun offset(value: Int): SelectSubqueryImpl<ENTITY> {
        val newContext = support.offset(value)
        return copy(context = newContext)
    }

    override fun limit(value: Int): SelectSubqueryImpl<ENTITY> {
        val newContext = support.limit(value)
        return copy(context = newContext)
    }

    override fun forUpdate(): SelectSubqueryImpl<ENTITY> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun select(columnInfo: ColumnInfo<*>): SingleColumnProjection {
        val newContext = context.addColumn(columnInfo)
        return SingleColumnProjectionImpl(ContextHolder(newContext))
    }
}
