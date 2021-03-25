package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.builder.EntitySelectStatementBuilder
import org.komapper.core.query.command.EntitySelectCommand
import org.komapper.core.query.context.EntitySelectContext
import org.komapper.core.query.scope.JoinDeclaration
import org.komapper.core.query.scope.WhereDeclaration

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

internal class EntitySelectQueryImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: EntitySelectContext<ENTITY> = EntitySelectContext(entityMetamodel)
) :
    EntitySelectQuery<ENTITY>,
    EntitySelectQuery1<ENTITY>,
    EntitySelectSubQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY> = SelectQuerySupport(context)

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY> {
        support.innerJoin(entityMetamodel, declaration)
        return this
    }

    override fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY> {
        support.leftJoin(entityMetamodel, declaration)
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
        support.where(declaration)
        return this
    }

    override fun orderBy(vararg sortItems: PropertyMetamodel<*, *>): EntitySelectQueryImpl<ENTITY> {
        support.orderBy(*sortItems)
        return this
    }

    override fun offset(value: Int): EntitySelectQueryImpl<ENTITY> {
        support.offset(value)
        return this
    }

    override fun limit(value: Int): EntitySelectQueryImpl<ENTITY> {
        support.limit(value)
        return this
    }

    override fun forUpdate(): EntitySelectQueryImpl<ENTITY> {
        support.forUpdate()
        return this
    }

    override fun select(propertyMetamodel: PropertyMetamodel<*, *>): SingleProjection {
        context.projections.add(propertyMetamodel)
        return SingleProjection.ContextHolder(context)
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val statement = buildStatement(config)
        val command = EntitySelectCommand(entityMetamodel, context, config, statement)
        return command.execute()
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = EntitySelectStatementBuilder(config, context)
        return builder.build()
    }
}
