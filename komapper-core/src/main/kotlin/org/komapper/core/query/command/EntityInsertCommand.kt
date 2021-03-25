package org.komapper.core.query.command

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.Executor
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.EntityMetamodel

internal class EntityInsertCommand<ENTITY>(
    @Suppress("unused") private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val entity: ENTITY,
    private val config: DatabaseConfig,
    override val statement: Statement
) : Command<ENTITY> {

    private val executor: Executor = Executor(config) { con, sql ->
        val assignment = entityMetamodel.idAssignment()
        if (assignment is Assignment.Identity<*, *>) {
            con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
        } else {
            con.prepareStatement(sql)
        }
    }

    override fun execute(): ENTITY {
        return executor.executeUpdate(statement) { ps, _ ->
            val assignment = entityMetamodel.idAssignment()
            if (assignment is Assignment.Identity<ENTITY, *>) {
                assignment.assign(entity) {
                    ps.generatedKeys.use { rs ->
                        if (rs.next()) {
                            rs.getLong(1)
                        } else {
                            TODO()
                        }
                    }
                }
            } else {
                entity
            }
        }
    }
}
