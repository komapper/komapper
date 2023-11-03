package integration.jdbc.sqlserver

import integration.core.SqlServerSetting
import org.komapper.core.ExecutionOptions
import org.komapper.jdbc.JdbcDatabase

@Suppress("unused")
class JdbcSqlServerSetting(url: String) : SqlServerSetting<JdbcDatabase> {
    override val database: JdbcDatabase = JdbcDatabase(
        url,
        "test",
        "test",
        executionOptions = ExecutionOptions(batchSize = 2),
    )
}
