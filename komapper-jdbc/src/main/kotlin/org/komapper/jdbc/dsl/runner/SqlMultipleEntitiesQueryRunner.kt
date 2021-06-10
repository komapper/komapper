package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.core.dsl.query.Entities
import org.komapper.core.dsl.query.EntitiesImpl
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

class SqlMultipleEntitiesQueryRunner<R>(
    private val context: SqlSelectContext<*, *, *>,
    private val option: SqlSelectOption,
    private val metamodels: List<EntityMetamodel<*, *, *>>,
    private val collect: suspend (Flow<Entities>) -> R
) :
    JdbcQueryRunner<R> {

    override fun run(config: DatabaseConfig): R {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val provide: (JdbcDialect, ResultSet) -> Entities = { dialect, rs ->
            val mapper = EntityMapper(dialect, rs)
            val map = metamodels.associateWith { mapper.execute(it) }
            EntitiesImpl(map)
        }
        val executor = JdbcExecutor(config, option)
        return executor.executeQuery(statement, provide, collect)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).toSql()
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
