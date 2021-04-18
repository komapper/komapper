package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.element.UpdateSet
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.QueryOption
import org.komapper.core.dsl.scope.SetDeclaration
import org.komapper.core.dsl.scope.SetScope

internal class EntityUpsertQuerySupport<ENTITY : Any>(
    private val context: EntityUpsertContext<ENTITY>,
    private val option: QueryOption
) {

    fun set(propertyMetamodels: List<PropertyMetamodel<ENTITY, *>>): EntityUpsertContext<ENTITY> {
        return context.copy(updateSet = UpdateSet.Properties(propertyMetamodels))
    }

    fun set(declaration: SetDeclaration<ENTITY>): EntityUpsertContext<ENTITY> {
        val scope = SetScope<ENTITY>().apply(declaration)
        return context.copy(updateSet = UpdateSet.Pairs(scope.toList()))
    }

    fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        val assignment = context.entityMetamodel.idAssignment()
        return if (assignment is Assignment.Sequence<ENTITY, *>) {
            assignment.assign(entity, config.name) {
                val sequenceName = assignment.getCanonicalSequenceName(config.dialect::enquote)
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
            context.entityMetamodel.updateCreatedAt(newEntity, clock).let {
                context.entityMetamodel.updateUpdatedAt(it, clock)
            }
        }
    }

    fun <T> upsert(config: DatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val requiresGeneratedKeys = context.entityMetamodel.idAssignment() is Assignment.Identity<*, *>
        val executor = JdbcExecutor(config, option.asJdbcOption(), requiresGeneratedKeys)
        return execute(executor)
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = config.dialect.getEntityUpsertStatementBuilder(context, entity)
        return builder.build()
    }
}
