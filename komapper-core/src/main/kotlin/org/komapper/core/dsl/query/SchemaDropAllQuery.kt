package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.option.QueryOptionConfigurator
import org.komapper.core.dsl.option.SchemaDropAllOption

interface SchemaDropAllQuery : Query<Unit> {
    fun option(configurator: QueryOptionConfigurator<SchemaDropAllOption>): SchemaDropAllQuery
}

internal data class SchemaDropAllQueryImpl(
    private val option: SchemaDropAllOption = SchemaDropAllOption()
) : SchemaDropAllQuery {

    override fun option(configurator: QueryOptionConfigurator<SchemaDropAllOption>): SchemaDropAllQuery {
        return copy(option = configurator.apply(option))
    }

    override fun run(holder: DatabaseConfigHolder) {
        val config = holder.config
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, SchemaDropAllOption())
        executor.execute(statement)
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        val config = holder.config
        return buildStatement(config).sql
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.dropAll()
    }
}
