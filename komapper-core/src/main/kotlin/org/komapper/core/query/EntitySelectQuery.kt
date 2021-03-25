package org.komapper.core.query

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.builder.EntitySelectStatementBuilder
import org.komapper.core.query.command.EntitySelectCommand
import org.komapper.core.query.context.EntitySelectContext
import org.komapper.core.query.context.JoinContext
import org.komapper.core.query.context.JoinKind
import org.komapper.core.query.data.SortItem
import org.komapper.core.query.scope.JoinDeclaration
import org.komapper.core.query.scope.JoinScope
import org.komapper.core.query.scope.WhereDeclaration
import org.komapper.core.query.scope.WhereScope

interface EntitySelectQuery<ENTITY> : Query<List<ENTITY>> {

    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery1<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery1<ENTITY>

    fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY>
    fun orderBy(vararg sortItems: PropertyMetamodel<*, *>): EntitySelectQuery<ENTITY>
    fun offset(value: Int): EntitySelectQuery<ENTITY>
    fun limit(value: Int): EntitySelectQuery<ENTITY>
    fun forUpdate(): EntitySelectQuery<ENTITY>
}

// TODO is this necessary?
interface EntitySelectQuery1<ENTITY> : EntitySelectQuery<ENTITY> {

    override fun where(declaration: WhereDeclaration): EntitySelectQuery1<ENTITY>
    override fun orderBy(vararg sortItems: PropertyMetamodel<*, *>): EntitySelectQuery1<ENTITY>
    override fun offset(value: Int): EntitySelectQuery1<ENTITY>
    override fun limit(value: Int): EntitySelectQuery1<ENTITY>
    override fun forUpdate(): EntitySelectQuery1<ENTITY>

    fun <T, S> associate(
        e1: EntityMetamodel<T>,
        e2: EntityMetamodel<S>,
        associator: Associator<T, S>
    ): EntitySelectQuery1<ENTITY>
}

interface EntitySelectSubQuery<ENTITY> {
    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectSubQuery<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectSubQuery<ENTITY>

    fun where(declaration: WhereDeclaration): EntitySelectSubQuery<ENTITY>
    fun orderBy(vararg sortItems: PropertyMetamodel<*, *>): EntitySelectSubQuery<ENTITY>
    fun offset(value: Int): EntitySelectSubQuery<ENTITY>
    fun limit(value: Int): EntitySelectSubQuery<ENTITY>
    fun select(propertyMetamodel: PropertyMetamodel<*, *>): SingleProjection
}

sealed class SingleProjection {
    internal data class ContextHolder(val context: EntitySelectContext<*>) : SingleProjection()
}

internal class EntitySelectQueryImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: EntitySelectContext<ENTITY> = EntitySelectContext(entityMetamodel)
) :
    EntitySelectQuery<ENTITY>,
    EntitySelectQuery1<ENTITY>,
    EntitySelectSubQuery<ENTITY> {

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY> {
        return join(entityMetamodel, declaration, JoinKind.INNER)
    }

    override fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY> {
        return join(entityMetamodel, declaration, JoinKind.LEFT_OUTER)
    }

    private fun <OTHER_ENTITY> join(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>,
        kind: JoinKind
    ): EntitySelectQueryImpl<ENTITY> {
        val join = JoinContext(entityMetamodel, kind)
        val scope = JoinScope(join)
        declaration(scope)
        if (join.isNotEmpty()) {
            context.joins.add(join)
        }
        return this
    }

    override fun <T, S> associate(
        e1: EntityMetamodel<T>,
        e2: EntityMetamodel<S>,
        associator: Associator<T, S>
    ): EntitySelectQueryImpl<ENTITY> {
        val entityMetamodels = context.getEntityMetamodels()
        if (entityMetamodels.none { it == e1 }) {
            error("The e1 is not found. Use e1 in the join clause.")
        }
        if (entityMetamodels.none { it == e2 }) {
            error("The e2 is not found. Use e2 in the join clause.")
        }
        @Suppress("UNCHECKED_CAST")
        context.associatorMap[e1 to e2] = associator as Associator<Any, Any>
        return this
    }

    override fun where(declaration: WhereDeclaration): EntitySelectQueryImpl<ENTITY> {
        val scope = WhereScope(context.where)
        declaration(scope)
        return this
    }

    override fun orderBy(vararg sortItems: PropertyMetamodel<*, *>): EntitySelectQueryImpl<ENTITY> {
        for (item in sortItems) {
            when (item) {
                is SortItem -> context.orderBy.add(item)
                else -> context.orderBy.add(SortItem.Asc(item))
            }
        }
        return this
    }

    override fun offset(value: Int): EntitySelectQueryImpl<ENTITY> {
        context.offset = value
        return this
    }

    override fun limit(value: Int): EntitySelectQueryImpl<ENTITY> {
        context.limit = value
        return this
    }

    override fun forUpdate(): EntitySelectQueryImpl<ENTITY> {
        context.forUpdate.option = ForUpdateOption.BASIC
        return this
    }

    override fun select(propertyMetamodel: PropertyMetamodel<*, *>): SingleProjection {
        context.projections.add(propertyMetamodel)
        return SingleProjection.ContextHolder(context)
    }

    override fun run(config: DefaultDatabaseConfig): List<ENTITY> {
        val statement = buildStatement(config)
        val command = EntitySelectCommand(entityMetamodel, context, config, statement)
        return command.execute()
    }

    override fun toStatement(config: DefaultDatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DefaultDatabaseConfig): Statement {
        val builder = EntitySelectStatementBuilder(config, context)
        return builder.build()
    }
}
