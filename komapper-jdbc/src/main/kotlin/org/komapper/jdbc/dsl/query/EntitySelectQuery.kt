package org.komapper.jdbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.EntitySelectStatementBuilder
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.element.Associator
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntitySelectOption
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet
import kotlin.reflect.cast

interface EntitySelectQuery<ENTITY : Any> : Subquery<ENTITY> {

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> innerJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> leftJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQuery<ENTITY>

    fun first(declaration: WhereDeclaration): Query<ENTITY>
    fun firstOrNull(declaration: WhereDeclaration): Query<ENTITY?>
    fun where(declaration: WhereDeclaration): EntitySelectQuery<ENTITY>
    fun orderBy(vararg expressions: ColumnExpression<*, *>): EntitySelectQuery<ENTITY>
    fun offset(offset: Int): EntitySelectQuery<ENTITY>
    fun limit(limit: Int): EntitySelectQuery<ENTITY>
    fun forUpdate(): EntitySelectQuery<ENTITY>
    fun option(configure: (EntitySelectOption) -> EntitySelectOption): EntitySelectQuery<ENTITY>

    fun <T : Any, S : Any> associate(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>,
        associator: Associator<T, S>
    ): EntitySelectQuery<ENTITY>

    fun asSqlQuery(): SqlSelectQuery<ENTITY>
}

internal data class EntitySelectQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
    private val option: EntitySelectOption = EntitySelectOption.default
) :
    EntitySelectQuery<ENTITY> {

    companion object Message {
        fun entityMetamodelNotFound(parameterName: String): String {
            return "The '$parameterName' metamodel is not found. Bind it to this query in advance using the from or join clause."
        }
    }

    private val support: SelectQuerySupport<ENTITY, ID, META, EntitySelectContext<ENTITY, ID, META>> =
        SelectQuerySupport(context)
    override val subqueryContext = SubqueryContext.EntitySelect<ENTITY>(context)

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> innerJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.innerJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any, OTHER_META : EntityMetamodel<OTHER_ENTITY, *, OTHER_META>> leftJoin(
        metamodel: OTHER_META,
        on: OnDeclaration<OTHER_ENTITY>
    ): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.leftJoin(metamodel, on)
        return copy(context = newContext)
    }

    override fun <T : Any, S : Any> associate(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>,
        associator: Associator<T, S>
    ): EntitySelectQueryImpl<ENTITY, ID, META> {
        val metamodels = context.getEntityMetamodels()
        require(metamodel1 in metamodels) { entityMetamodelNotFound("metamodel1") }
        require(metamodel2 in metamodels) { entityMetamodelNotFound("metamodel2") }
        @Suppress("UNCHECKED_CAST")
        val newContext = context.putAssociator(metamodel1 to metamodel2, associator as Associator<Any, Any>)
        return copy(context = newContext)
    }

    override fun first(declaration: WhereDeclaration): Query<ENTITY> {
        val newContext = support.first(declaration)
        return copy(context = newContext).first()
    }

    override fun firstOrNull(declaration: WhereDeclaration): Query<ENTITY?> {
        val newContext = support.first(declaration)
        return copy(context = newContext).firstOrNull()
    }

    override fun where(declaration: WhereDeclaration): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.orderBy(*expressions)
        return copy(context = newContext)
    }

    override fun offset(offset: Int): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.offset(offset)
        return copy(context = newContext)
    }

    override fun limit(limit: Int): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.limit(limit)
        return copy(context = newContext)
    }

    override fun forUpdate(): EntitySelectQueryImpl<ENTITY, ID, META> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun option(configure: (EntitySelectOption) -> EntitySelectOption): EntitySelectQuery<ENTITY> {
        return copy(option = configure(option))
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

    override fun <R> collect(transform: (Sequence<ENTITY>) -> R): Query<R> {
        return Terminal(transform)
    }

    private inner class Terminal<R>(val transform: (Sequence<ENTITY>) -> R) : Query<R> {

        override fun run(config: DatabaseConfig): R {
            if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
                error("Empty where clause is not allowed.")
            }
            val statement = buildStatement(config)
            val executor = JdbcExecutor(config, option)
            return executor.executeQuery(statement) { rs ->
                // hold only unique entities
                val pool: MutableMap<EntityKey, Any> = mutableMapOf()
                val rows = fetchAllEntities(config.dialect, rs)
                for (row in rows) {
                    val entityKeys: MutableMap<EntityMetamodel<*, *, *>, EntityKey> = mutableMapOf()
                    for ((key, entity) in row) {
                        pool.putIfAbsent(key, entity)
                        entityKeys[key.entityMetamodel] = key
                    }
                    associate(entityKeys, pool)
                }
                pool.asSequence().filter {
                    it.key.entityMetamodel == context.target
                }.map {
                    context.target.klass().cast(it.value)
                }.let {
                    transform(it)
                }
            }
        }

        override fun dryRun(config: DatabaseConfig): String {
            return buildStatement(config).toString()
        }

        private fun buildStatement(config: DatabaseConfig): Statement {
            val builder = EntitySelectStatementBuilder(config.dialect, context)
            return builder.build()
        }

        private fun fetchAllEntities(dialect: JdbcDialect, rs: ResultSet): List<Map<EntityKey, Any>> {
            val metamodels = context.projection.metamodels
            val rows = mutableListOf<Map<EntityKey, Any>>()
            while (rs.next()) {
                val row = mutableMapOf<EntityKey, Any>()
                val mapper = EntityMapper(dialect, rs)
                for (metamodel in metamodels) {
                    val entity = mapper.execute(metamodel) ?: continue
                    @Suppress("UNCHECKED_CAST")
                    metamodel as EntityMetamodel<Any, Any, *>
                    val id = metamodel.getId(entity)
                    val key = EntityKey(metamodel, id)
                    row[key] = entity
                }
                rows.add(row)
            }
            return rows
        }

        private fun associate(
            entityKeys: Map<EntityMetamodel<*, *, *>, EntityKey>,
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
    val entityMetamodel: EntityMetamodel<*, *, *>,
    val id: Any
)
