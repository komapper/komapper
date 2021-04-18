package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.JoinKind
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.ForUpdateOption
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.OnScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope

internal data class SelectQuerySupport<ENTITY : Any, META : EntityMetamodel<ENTITY, META>, CONTEXT : SelectContext<ENTITY, META, CONTEXT>>(
    private val context: CONTEXT
) {

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, OTHER_META>> innerJoin(
        entityMetamodel: OTHER_META,
        declaration: OnDeclaration<OTHER_ENTITY>
    ): CONTEXT {
        return join(entityMetamodel, declaration, JoinKind.INNER)
    }

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, OTHER_META>> leftJoin(
        entityMetamodel: OTHER_META,
        declaration: OnDeclaration<OTHER_ENTITY>
    ): CONTEXT {
        return join(entityMetamodel, declaration, JoinKind.LEFT_OUTER)
    }

    private fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, OTHER_META>> join(
        entityMetamodel: OTHER_META,
        declaration: OnDeclaration<OTHER_ENTITY>,
        kind: JoinKind
    ): CONTEXT {
        val scope = OnScope<OTHER_ENTITY>().apply(declaration)
        if (scope.isNotEmpty()) {
            val join = Join(entityMetamodel, kind, scope.toList())
            return context.addJoin(join)
        }
        return context
    }

    fun where(declaration: WhereDeclaration): CONTEXT {
        val scope = WhereScope().apply(declaration)
        return context.addWhere(scope)
    }

    fun orderBy(vararg expressions: PropertyExpression<*>): CONTEXT {
        val items = expressions.map {
            when (it) {
                is SortItem -> it
                else -> SortItem.Property.Asc(it)
            }
        }
        return context.addOrderBy(items)
    }

    fun offset(value: Int): CONTEXT {
        return context.setOffset(value)
    }

    fun limit(value: Int): CONTEXT {
        return context.setLimit(value)
    }

    fun forUpdate(): CONTEXT {
        val forUpdate = ForUpdate(ForUpdateOption.BASIC)
        return context.setForUpdate(forUpdate)
    }

    fun except(left: Subquery<ENTITY>, right: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.EXCEPT, left, right)
    }

    fun intersect(left: Subquery<ENTITY>, right: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.INTERSECT, left, right)
    }

    fun union(left: Subquery<ENTITY>, right: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION, left, right)
    }

    fun unionAll(left: Subquery<ENTITY>, right: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION_ALL, left, right)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        left: Subquery<ENTITY>,
        right: Subquery<ENTITY>
    ): SqlSetOperationQuery<ENTITY> {
        val setOperatorContext = SqlSetOperationContext(kind, left.subqueryContext, right.subqueryContext)
        return SetOperationQueryImpl(setOperatorContext) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            val entity = m.execute(context.target)
            checkNotNull(entity)
        }
    }
}
