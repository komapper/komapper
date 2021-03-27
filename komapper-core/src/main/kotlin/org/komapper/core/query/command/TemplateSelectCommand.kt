package org.komapper.core.query.command

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.query.Row

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
