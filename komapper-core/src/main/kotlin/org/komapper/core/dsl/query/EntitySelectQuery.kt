package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.Dialect
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntitySelectStatementBuilder
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.element.Associator
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntitySelectOption
import org.komapper.core.dsl.option.QueryOptionConfigurator
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration
import java.sql.ResultSet
import kotlin.reflect.cast

interface EntitySelectQuery<ENTITY : Any> : Subquery<ENTITY> {

    fun <OTHER_ENTITY : Any> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY>
    fun orderBy(vararg expressions: PropertyExpression<*>): EntitySelectQuery<ENTITY>
    fun offset(value: Int): EntitySelectQuery<ENTITY>
    fun limit(value: Int): EntitySelectQuery<ENTITY>
    fun forUpdate(): EntitySelectQuery<ENTITY>
    fun option(configurator: QueryOptionConfigurator<EntitySelectOption>): EntitySelectQuery<ENTITY>

    fun <T : Any, S : Any> associate(
        e1: EntityMetamodel<T>,
        e2: EntityMetamodel<S>,
        associator: Associator<T, S>
    ): EntitySelectQuery<ENTITY>

    fun asSqlQuery(): SqlSelectQuery<ENTITY>
}

internal data class EntitySelectQueryImpl<ENTITY : Any>(
    private val context: EntitySelectContext<ENTITY>,
    private val option: EntitySelectOption = EntitySelectOption()
) :
    EntitySelectQuery<ENTITY> {

    companion object Message {
        fun entityMetamodelNotFound(parameterName: String): String {
            return "The '$parameterName' metamodel is not found. Bind it to this query in advance using the from or join clause."
        }

        fun idPropertyRequired(parameterName: String): String {
            return "The '$parameterName' metamodel must have one or more id properties."
        }
    }

    private val support: SelectQuerySupport<ENTITY, EntitySelectContext<ENTITY>> = SelectQuerySupport(context)
    override val subqueryContext = SubqueryContext.EntitySelect<ENTITY>(context)

    override fun <OTHER_ENTITY : Any> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.innerJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.leftJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun <T : Any, S : Any> associate(
        e1: EntityMetamodel<T>,
        e2: EntityMetamodel<S>,
        associator: Associator<T, S>
    ): EntitySelectQueryImpl<ENTITY> {
        require(e1.idProperties().isNotEmpty()) { idPropertyRequired("e1") }
        require(e2.idProperties().isNotEmpty()) { idPropertyRequired("e2") }
        val entityExpressions = context.getEntityExpressions()
        require(e1 in entityExpressions) { entityMetamodelNotFound("e1") }
        require(e2 in entityExpressions) { entityMetamodelNotFound("e2") }
        @Suppress("UNCHECKED_CAST")
        val newContext = context.putAssociator(e1 to e2, associator as Associator<Any, Any>)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun orderBy(vararg expressions: PropertyExpression<*>): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.orderBy(*expressions)
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

    override fun option(configurator: QueryOptionConfigurator<EntitySelectOption>): EntitySelectQuery<ENTITY> {
        return copy(option = configurator.apply(option))
    }

    override fun except(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return support.except(this, other)
    }

    override fun intersect(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return support.intersect(this, other)
    }

    override fun union(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return support.union(this, other)
    }

    override fun unionAll(other: Subquery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return support.unionAll(this, other)
    }

    override fun asSqlQuery(): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(context.asSqlSelectContext(), option.asSqlSelectOption())
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val terminal = Terminal { it.toList() }
        return terminal.run(config)
    }

    override fun dryRun(config: DatabaseConfig): String {
        val terminal = Terminal { it.toList() }
        return terminal.dryRun(config)
    }

    override fun first(): Query<ENTITY> {
        return Terminal { it.first() }
    }

    override fun firstOrNull(): Query<ENTITY?> {
        return Terminal { it.firstOrNull() }
    }

    override fun <R> collect(transformer: (Sequence<ENTITY>) -> R): Query<R> {
        return Terminal(transformer)
    }

    private inner class Terminal<R>(val transformer: (Sequence<ENTITY>) -> R) : Query<R> {

        override fun run(config: DatabaseConfig): R {
            if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
                error("Empty where clause is not allowed.")
            }
            val statement = buildStatement(config)
            val executor = JdbcExecutor(config, option.asJdbcOption())
            return executor.executeQuery(statement) { rs ->
                // hold only unique entities
                val pool: MutableMap<EntityKey, Any> = mutableMapOf()
                val rows = fetchAllEntities(config.dialect, rs)
                for (row in rows) {
                    val entityKeys: MutableMap<EntityMetamodel<*>, EntityKey> = mutableMapOf()
                    for ((key, entity) in row) {
                        pool.putIfAbsent(key, entity)
                        entityKeys[key.entityMetamodel] = key
                    }
                    associate(entityKeys, pool)
                }
                pool.asSequence().filter {
                    it.key.entityMetamodel == context.entityMetamodel
                }.map {
                    context.entityMetamodel.klass().cast(it.value)
                }.let {
                    transformer(it)
                }
            }
        }

        override fun dryRun(config: DatabaseConfig): String {
            return buildStatement(config).sql
        }

        private fun buildStatement(config: DatabaseConfig): Statement {
            val builder = EntitySelectStatementBuilder(config.dialect, context)
            return builder.build()
        }

        private fun fetchAllEntities(dialect: Dialect, rs: ResultSet): List<Map<EntityKey, Any>> {
            val entityMetamodels = context.projection.values
            val rows = mutableListOf<Map<EntityKey, Any>>()
            while (rs.next()) {
                val row = mutableMapOf<EntityKey, Any>()
                val mapper = EntityMapper(dialect, rs)
                for (entityMetamodel in entityMetamodels) {
                    val entity = mapper.execute(entityMetamodel) ?: continue
                    val idValues = entityMetamodel.idProperties()
                        .map { p ->
                            p.getterWithUncheckedCast(entity)
                                ?: error("The id value must not be null. entity=$entity, property=${p.name}")
                        }
                    val key = EntityKey(entityMetamodel, idValues)
                    row[key] = entity
                }
                rows.add(row)
            }
            return rows
        }

        private fun associate(
            entityKeys: Map<EntityMetamodel<*>, EntityKey>,
            pool: MutableMap<EntityKey, Any>
        ) {
            for ((association, associator) in context.associatorMap) {
                val key1 = entityKeys[association.first]
                val key2 = entityKeys[association.second]
                if (key1 == null || key2 == null) {
                    continue
                }
                val entity1 = pool[key1]!!
                val entity2 = pool[key2]!!
                val newEntity = associator.apply(entity1, entity2)
                pool.replace(key1, newEntity)
            }
        }
    }
}

private data class EntityKey(
    val entityMetamodel: EntityMetamodel<*>,
    val items: List<Any>
)
