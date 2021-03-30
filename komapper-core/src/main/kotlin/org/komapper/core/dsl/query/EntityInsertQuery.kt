package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.scope.EntityInsertOptionsDeclaration
import org.komapper.core.dsl.scope.EntityInsertOptionsScope
import org.komapper.core.dsl.util.getName
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.metamodel.Assignment
import java.sql.PreparedStatement

internal interface EntityInsertQuery<ENTITY> : Query<ENTITY> {
    fun options(declaration: EntityInsertOptionsDeclaration): EntityInsertQuery<ENTITY>
}

internal data class EntityInsertQueryImpl<ENTITY>(
    private val context: EntityInsertContext<ENTITY>,
    private val entity: ENTITY
) :
    EntityInsertQuery<ENTITY> {

    override fun options(declaration: EntityInsertOptionsDeclaration): EntityInsertQuery<ENTITY> {
        val scope = EntityInsertOptionsScope(context.options)
        declaration(scope)
        val newContext = context.copy(options = scope.options)
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig): ENTITY {
        val executor = JdbcExecutor(config, context.options) { con, sql ->
            val assignment = context.entityMetamodel.idAssignment()
            if (assignment is Assignment.Identity<*, *>) {
                con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
            } else {
                con.prepareStatement(sql)
            }
        }

        val newEntity = preInsert(config)
        val statement = buildStatement(config.dialect, newEntity)
        return executor.executeUpdate(statement) { ps, _ ->
            postInsert(newEntity, ps)
        }
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect, entity)
    }

    private fun buildStatement(dialect: Dialect, entity: ENTITY): Statement {
        val builder = EntityInsertStatementBuilder(dialect, context, entity)
        return builder.build()
    }

    private fun preInsert(config: DatabaseConfig): ENTITY {
        val assignment = context.entityMetamodel.idAssignment()
        return if (assignment is Assignment.Sequence<ENTITY, *>) {
            val sequenceName = assignment.getName(config.dialect::quote)
            assignment.assign(entity, config.name) {
                val sql = config.dialect.getSequenceSql(sequenceName)
                val statement = Statement(sql)
                val executor = JdbcExecutor(config, context.options)
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

    private fun postInsert(entity: ENTITY, ps: PreparedStatement): ENTITY {
        val assignment = context.entityMetamodel.idAssignment()
        return if (assignment is Assignment.Identity<ENTITY, *>) {
            assignment.assign(entity) {
                ps.generatedKeys.use { rs ->
                    if (rs.next()) rs.getLong(1) else error("No result: Statement.generatedKeys")
                }
            }
        } else {
            entity
        }
    }
}
