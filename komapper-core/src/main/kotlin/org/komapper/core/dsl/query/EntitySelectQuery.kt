package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntitySelectStatementBuilder
import org.komapper.core.dsl.command.EntitySelectCommand
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.scope.JoinDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel

interface EntitySelectQuery<ENTITY> : ListQuery<ENTITY> {

    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY>
    fun orderBy(vararg items: ColumnInfo<*>): EntitySelectQuery<ENTITY>
    fun offset(value: Int): EntitySelectQuery<ENTITY>
    fun limit(value: Int): EntitySelectQuery<ENTITY>
    fun forUpdate(): EntitySelectQuery<ENTITY>

    override fun peek(dialect: Dialect, block: (Statement) -> Unit): EntitySelectQuery<ENTITY> {
        super.peek(dialect, block)
        return this
    }

    fun <T, S> associate(
        e1: EntityMetamodel<T>,
        e2: EntityMetamodel<S>,
        associator: Associator<T, S>
    ): EntitySelectQuery<ENTITY>
}

internal data class EntitySelectQueryImpl<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val context: EntitySelectContext<ENTITY> = EntitySelectContext(entityMetamodel)
) :
    EntitySelectQuery<ENTITY> {

    private val support: SelectQuerySupport<ENTITY, EntitySelectContext<ENTITY>> = SelectQuerySupport(context)

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.innerJoin(entityMetamodel, declaration)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.leftJoin(entityMetamodel, declaration)
        return copy(context = newContext)
    }

    override fun <T, S> associate(
        e1: EntityMetamodel<T>,
        e2: EntityMetamodel<S>,
        associator: Associator<T, S>
    ): EntitySelectQueryImpl<ENTITY> {
        val entityMetamodels = context.getReferencingEntityMetamodels()
        if (entityMetamodels.none { it == e1 }) {
            error("The e1 is not found. Use e1 in the join clause.")
        }
        if (entityMetamodels.none { it == e2 }) {
            error("The e2 is not found. Use e2 in the join clause.")
        }
        @Suppress("UNCHECKED_CAST")
        val newContext = context.putAssociator(e1 to e2, associator as Associator<Any, Any>)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun orderBy(vararg items: ColumnInfo<*>): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.orderBy(*items)
        return copy(context = newContext)
    }

    override fun offset(value: Int): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.offset(value)
        return copy(context = newContext)
    }

    override fun limit(value: Int): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.limit(value)
        return copy(context = newContext)
    }

    override fun forUpdate(): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val transformable = Transformable { it.toList() }
        return transformable.run(config)
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = EntitySelectStatementBuilder(dialect, context)
        return builder.build()
    }

    override fun first(): Query<ENTITY> {
        return Transformable { it.first() }
    }

    override fun firstOrNull(): Query<ENTITY?> {
        return Transformable { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<ENTITY>) -> R): Query<R> {
        return Transformable(transformer)
    }

    private inner class Transformable<R>(val transformer: (Sequence<ENTITY>) -> R) : Query<R> {
        override fun run(config: DatabaseConfig): R {
            val statement = buildStatement(config.dialect)
            val command = EntitySelectCommand(entityMetamodel, context, config, statement, transformer)
            return command.execute()
        }

        override fun toStatement(dialect: Dialect): Statement = buildStatement(dialect)
    }
}
