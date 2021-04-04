package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntitySelectStatementBuilder
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.Projection
import org.komapper.core.dsl.scope.EntitySelectOptionDeclaration
import org.komapper.core.dsl.scope.EntitySelectOptionScope
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel
import java.sql.ResultSet

interface EntitySelectQuery<ENTITY> : ListQuery<ENTITY> {

    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY>
    fun orderBy(vararg items: ColumnInfo<*>): EntitySelectQuery<ENTITY>
    fun offset(value: Int): EntitySelectQuery<ENTITY>
    fun limit(value: Int): EntitySelectQuery<ENTITY>
    fun forUpdate(): EntitySelectQuery<ENTITY>
    fun option(declaration: EntitySelectOptionDeclaration): EntitySelectQuery<ENTITY>

    fun <T, S> associate(
        e1: EntityMetamodel<T>,
        e2: EntityMetamodel<S>,
        associator: Associator<T, S>
    ): EntitySelectQuery<ENTITY>
}

internal data class EntitySelectQueryImpl<ENTITY>(
    private val context: EntitySelectContext<ENTITY>,
    private val option: EntitySelectOption = QueryOptionImpl(allowEmptyWhereClause = true)
) :
    EntitySelectQuery<ENTITY> {

    companion object Message {
        fun entityMetamodelNotFound(parameterName: String): String {
            return "The '$parameterName' metamodel is not found. Bind it to this query in advance using the from or join clause."
        }
    }

    private val support: SelectQuerySupport<ENTITY, EntitySelectContext<ENTITY>> = SelectQuerySupport(context)

    override fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.innerJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY> {
        val newContext = support.leftJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun <T, S> associate(
        e1: EntityMetamodel<T>,
        e2: EntityMetamodel<S>,
        associator: Associator<T, S>
    ): EntitySelectQueryImpl<ENTITY> {
        val entityMetamodels = context.getAliasableEntityMetamodels()
        if (entityMetamodels.none { it == e1 }) error(entityMetamodelNotFound("e1"))
        if (entityMetamodels.none { it == e2 }) error(entityMetamodelNotFound("e2"))
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

    override fun option(declaration: EntitySelectOptionDeclaration): EntitySelectQuery<ENTITY> {
        val scope = EntitySelectOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val terminal = Terminal { it.toList() }
        return terminal.run(config)
    }

    override fun dryRun(dialect: Dialect): Statement {
        val terminal = Terminal { it.toList() }
        return terminal.dryRun(dialect)
    }

    override fun first(): Query<ENTITY> {
        return Terminal { it.first() }
    }

    override fun firstOrNull(): Query<ENTITY?> {
        return Terminal { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<ENTITY>) -> R): Query<R> {
        return Terminal(transformer)
    }

    private inner class Terminal<R>(val transformer: (Sequence<ENTITY>) -> R) : Query<R> {

        override fun run(config: DatabaseConfig): R {
            if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
                error("Empty where clause is not allowed.")
            }
            val statement = buildStatement(config.dialect)
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
                    @Suppress("UNCHECKED_CAST")
                    it.value as ENTITY
                }.let {
                    transformer(it)
                }
            }
        }

        override fun dryRun(dialect: Dialect): Statement {
            return buildStatement(dialect)
        }

        private fun buildStatement(dialect: Dialect): Statement {
            val builder = EntitySelectStatementBuilder(dialect, context)
            return builder.build()
        }

        private fun fetchAllEntities(dialect: Dialect, rs: ResultSet): List<Map<EntityKey, Any>> {
            val entityMetamodels = when (val projection = context.projection) {
                is Projection.Columns -> error("cannot happen")
                is Projection.Tables -> projection.values
            }
            val rows = mutableListOf<Map<EntityKey, Any>>()
            while (rs.next()) {
                val row = mutableMapOf<EntityKey, Any>()
                val mapper = EntityMapper(dialect, rs)
                for (entityMetamodel in entityMetamodels) {
                    val entity = mapper.execute(entityMetamodel) as Any
                    val idValues = entityMetamodel.idProperties()
                        .map { it.getterWithUncheckedCast(entity) }
                        .map { checkNotNull(it) }
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
