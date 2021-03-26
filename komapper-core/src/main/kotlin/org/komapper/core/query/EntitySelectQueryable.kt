package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.builder.EntitySelectStatementBuilder
import org.komapper.core.query.command.EntitySelectCommand
import org.komapper.core.query.context.EntitySelectContext
import org.komapper.core.query.scope.JoinDeclaration
import org.komapper.core.query.scope.WhereDeclaration

interface EntitySelectQueryable<ENTITY> : Queryable<List<ENTITY>> {

    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryable1<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryable1<ENTITY>

    fun where(declaration: WhereDeclaration): EntitySelectQueryable<ENTITY>
    fun orderBy(vararg items: ColumnInfo<*>): EntitySelectQueryable<ENTITY>
    fun offset(value: Int): EntitySelectQueryable<ENTITY>
    fun limit(value: Int): EntitySelectQueryable<ENTITY>
    fun forUpdate(): EntitySelectQueryable<ENTITY>
}

// TODO is this necessary?
interface EntitySelectQueryable1<ENTITY> : EntitySelectQueryable<ENTITY> {

    override fun where(declaration: WhereDeclaration): EntitySelectQueryable1<ENTITY>
    override fun orderBy(vararg items: ColumnInfo<*>): EntitySelectQueryable1<ENTITY>
    override fun offset(value: Int): EntitySelectQueryable1<ENTITY>
    override fun limit(value: Int): EntitySelectQueryable1<ENTITY>
    override fun forUpdate(): EntitySelectQueryable1<ENTITY>

    fun <T, S> associate(
        e1: EntityMetamodel<T>,
        e2: EntityMetamodel<S>,
        associator: Associator<T, S>
    ): EntitySelectQueryable1<ENTITY>
}

internal class EntitySelectQueryableImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: EntitySelectContext<ENTITY> = EntitySelectContext(entityMetamodel)
) :
    EntitySelectQueryable<ENTITY>,
    EntitySelectQueryable1<ENTITY> {

    private val support: SelectQuerySupport<ENTITY> = SelectQuerySupport(context)

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryableImpl<ENTITY> {
        support.innerJoin(entityMetamodel, declaration)
        return this
    }

    override fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryableImpl<ENTITY> {
        support.leftJoin(entityMetamodel, declaration)
        return this
    }

    override fun <T, S> associate(
        e1: EntityMetamodel<T>,
        e2: EntityMetamodel<S>,
        associator: Associator<T, S>
    ): EntitySelectQueryableImpl<ENTITY> {
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

    override fun where(declaration: WhereDeclaration): EntitySelectQueryableImpl<ENTITY> {
        support.where(declaration)
        return this
    }

    override fun orderBy(vararg items: ColumnInfo<*>): EntitySelectQueryableImpl<ENTITY> {
        support.orderBy(*items)
        return this
    }

    override fun offset(value: Int): EntitySelectQueryableImpl<ENTITY> {
        support.offset(value)
        return this
    }

    override fun limit(value: Int): EntitySelectQueryableImpl<ENTITY> {
        support.limit(value)
        return this
    }

    override fun forUpdate(): EntitySelectQueryableImpl<ENTITY> {
        support.forUpdate()
        return this
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
