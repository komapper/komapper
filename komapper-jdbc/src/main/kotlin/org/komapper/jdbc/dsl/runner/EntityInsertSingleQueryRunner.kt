package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityInsertOption
import org.komapper.jdbc.DatabaseConfig

internal class EntityInsertSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityInsertContext<ENTITY, ID, META>,
    option: EntityInsertOption,
    private val entity: ENTITY
) : JdbcQueryRunner<ENTITY> {

    private val support: EntityInsertQueryRunnerSupport<ENTITY, ID, META> =
        EntityInsertQueryRunnerSupport(context, option)

    override fun run(config: DatabaseConfig): ENTITY {
        val newEntity = preInsert(config)
        val (_, generatedKeys) = insert(config, newEntity)
        return postInsert(newEntity, generatedKeys)
    }

    private fun preInsert(config: DatabaseConfig): ENTITY {
        return support.preInsert(config, entity)
    }

    private fun insert(config: DatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = buildStatement(config, entity)
        return support.insert(config) { it.executeUpdate(statement) }
    }

    private fun postInsert(entity: ENTITY, generatedKeys: LongArray): ENTITY {
        val key = generatedKeys.firstOrNull()
        return if (key != null) {
            support.postInsert(entity, key)
        } else {
            entity
        }
    }

    override fun dryRun(config: DatabaseConfig): String {
        val statement = buildStatement(config, entity)
        return statement.toSql()
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, listOf(entity))
    }
}
