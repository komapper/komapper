package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.scope.HavingDeclaration
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel

interface SqlSubquery<ENTITY> : SqlSubqueryResult {
    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSubquery<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSubquery<ENTITY>

    fun where(declaration: WhereDeclaration): SqlSubquery<ENTITY>
    fun groupBy(vararg items: ColumnInfo<*>): SqlSubquery<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSubquery<ENTITY>
    fun orderBy(vararg items: ColumnInfo<*>): SqlSubquery<ENTITY>
    fun offset(value: Int): SqlSubquery<ENTITY>
    fun limit(value: Int): SqlSubquery<ENTITY>
    fun forUpdate(): SqlSubquery<ENTITY>
    fun select(columnInfo: ColumnInfo<*>): SingleColumnSqlSubqueryResult
}

internal data class SqlSubqueryImpl<ENTITY>(
    private val context: SqlSelectContext<ENTITY>
) :
    SqlSubquery<ENTITY> {

    override val contextHolder = ContextHolder(context)

    private val support: SelectQuerySupport<ENTITY, SqlSelectContext<ENTITY>> = SelectQuerySupport(context)

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSubqueryImpl<ENTITY> {
        val newContext = support.innerJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY> leftJoin(
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

    override fun groupBy(vararg items: ColumnInfo<*>): SqlSubqueryImpl<ENTITY> {
        val newContext = context.copy(groupBy = items.toList())
        return copy(context = newContext)
    }

    override fun having(declaration: HavingDeclaration): SqlSubqueryImpl<ENTITY> {
        val scope = HavingScope()
        declaration(scope)
        val newContext = context.addHaving(scope.criteria.toList())
        return copy(context = newContext)
    }

    override fun orderBy(vararg items: ColumnInfo<*>): SqlSubqueryImpl<ENTITY> {
        val newContext = support.orderBy(*items)
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

    override fun select(columnInfo: ColumnInfo<*>): SingleColumnSqlSubqueryResult {
        val newContext = context.setColumn(columnInfo)
        return SingleColumnSqlSubqueryResultImpl(ContextHolder(newContext))
    }
}
