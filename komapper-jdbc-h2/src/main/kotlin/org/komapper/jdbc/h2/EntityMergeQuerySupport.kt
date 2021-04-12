package org.komapper.jdbc.h2

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.option.VersionOption
import org.komapper.core.dsl.query.checkOptimisticLock

internal class EntityMergeQuerySupport<ENTITY : Any>(
    private val context: EntityMergeContext<ENTITY>,
    private val option: VersionOption
) {

    fun preMerge(config: DatabaseConfig, entity: ENTITY): ENTITY {
        val assignment = context.entityMetamodel.idAssignment()
        val idProperty = context.entityMetamodel.idProperties().firstOrNull()
        return if (idProperty !in context.on && assignment is Assignment.Sequence<ENTITY, *>) {
            assignment.assign(entity, config.name) {
                val sequenceName = assignment.getCanonicalSequenceName(config.dialect::quote)
                val sql = config.dialect.getSequenceSql(sequenceName)
                val statement = Statement(sql)
                val executor = JdbcExecutor(config, option.asJdbcOption())
                executor.executeQuery(statement) { rs ->
                    if (rs.next()) rs.getLong(1) else error("No result: ${statement.sql}")
                }
            }
        } else {
            entity
        }.let { newEntity ->
            val clock = config.clockProvider.now()
            if (context.entityMetamodel.createdAtProperty() !in context.on) {
                context.entityMetamodel.updateCreatedAt(newEntity, clock)
            } else {
                newEntity
            }.let {
                if (context.entityMetamodel.updatedAtProperty() !in context.on) {
                    context.entityMetamodel.updateUpdatedAt(it, clock)
                } else {
                    it
                }
            }
        }
    }

    fun <T> merge(config: DatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val executor = JdbcExecutor(config, option.asJdbcOption()) { con, sql ->
            val assignment = context.entityMetamodel.idAssignment()
            if (assignment is Assignment.Identity<*, *>) {
                con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
            } else {
                con.prepareStatement(sql)
            }
        }
        return execute(executor)
    }

    fun postMerge(count: Int, index: Int? = null) {
        if (context.entityMetamodel.versionProperty() != null) {
            checkOptimisticLock(option, count, index)
        }
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityMergeStatementBuilder(config.dialect, context, entity, option)
        return builder.build()
    }
}
