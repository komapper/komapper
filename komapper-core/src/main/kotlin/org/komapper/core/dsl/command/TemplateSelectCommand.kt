package org.komapper.core.dsl.command

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.query.Row
import org.komapper.core.jdbc.JdbcExecutor

internal class TemplateSelectCommand<T, R>(
    private val config: DatabaseConfig,
    private val statement: Statement,
    private val provider: Row.() -> T,
    private val transformer: (Sequence<T>) -> R
) : Command<R> {

    private val executor: JdbcExecutor = JdbcExecutor(config)

    override fun execute(): R {
        return executor.executeQuery(
            statement,
            {
                val row = Row(config.dialect, it)
                provider(row)
            },
            transformer
        )
    }
}
