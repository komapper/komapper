package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityBatchDeleteOption

interface EntityBatchDeleteQuery<ENTITY : Any> : Query<Unit> {
    fun option(configurator: (EntityBatchDeleteOption) -> EntityBatchDeleteOption): EntityBatchDeleteQuery<ENTITY>
}

internal data class EntityBatchDeleteQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val option: EntityBatchDeleteOption = EntityBatchDeleteOption()
) :
    EntityBatchDeleteQuery<ENTITY> {

    private val support: EntityDeleteQuerySupport<ENTITY, ID, META> = EntityDeleteQuerySupport(context, option)

    override fun option(configurator: (EntityBatchDeleteOption) -> EntityBatchDeleteOption): EntityBatchDeleteQueryImpl<ENTITY, ID, META> {
        return copy(option = configurator(option))
    }

    override fun run(holder: DatabaseConfigHolder) {
        if (entities.isEmpty()) return
        val config = holder.config
        val (counts) = delete(config)
        postDelete(counts)
    }

    private fun delete(config: DatabaseConfig): Pair<IntArray, LongArray> {
        val statements = entities.map { buildStatement(config, it) }
        return support.delete(config) { it.executeBatch(statements) }
    }

    private fun postDelete(counts: IntArray) {
        for ((i, count) in counts.withIndex()) {
            support.postDelete(count, i)
        }
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        if (entities.isEmpty()) return ""
        val config = holder.config
        return buildStatement(config, entities.first()).sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
