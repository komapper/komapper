package integration.jdbc.mariadb

import integration.core.MariaDbSetting
import org.komapper.core.ExecutionOptions
import org.komapper.jdbc.JdbcDatabase

@Suppress("unused")
class JdbcMariaDbSetting(private val driver: String, url: String) : MariaDbSetting<JdbcDatabase> {
    override val database: JdbcDatabase = JdbcDatabase(
        url,
        "test",
        "test",
        executionOptions = ExecutionOptions(batchSize = 2),
    )
}
