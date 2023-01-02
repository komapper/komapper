package integration.jdbc.mysql

import integration.core.MySqlSetting
import org.komapper.core.ExecutionOptions
import org.komapper.jdbc.JdbcDatabase

@Suppress("unused")
class JdbcMySqlSetting(private val driver: String, url: String) : MySqlSetting<JdbcDatabase> {
    override val database: JdbcDatabase = JdbcDatabase(
        url,
        "test",
        "test",
        executionOptions = ExecutionOptions(batchSize = 2),
    )
}
