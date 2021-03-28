package org.komapper.core.dsl.command

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.EntityMetamodel

internal class SqlInsertCommand<ENTITY>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    config: DatabaseConfig,
    private val statement: Statement
) : Command<Pair<Int, Long?>> {

    private val executor: JdbcExecutor = JdbcExecutor(config) { con, sql ->
        val assignment = entityMetamodel.idAssignment()
        if (assignment is Assignment.Identity<*, *>) {
            con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
        } else {
            con.prepareStatement(sql)
        }
    }

    override fun execute(): Pair<Int, Long?> {
        return executor.executeUpdate(statement) { ps, count ->
            val assignment = entityMetamodel.idAssignment()
            val id = if (assignment is Assignment.Identity<ENTITY, *>) {
                ps.generatedKeys.use { rs ->
                    if (rs.next()) rs.getLong(1) else error("No result: Statement.generatedKeys")
                }
            } else {
                null
            }
            count to id
        }
    }
}
