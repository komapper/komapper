package integration.jdbc.oracle

import integration.core.OracleSetting
import org.komapper.core.ExecutionOptions
import org.komapper.jdbc.JdbcDatabase

@Suppress("unused")
class JdbcOracleSetting(private val driver: String, url: String) : OracleSetting<JdbcDatabase> {
    override val database: JdbcDatabase = JdbcDatabase(
        url,
        "test",
        "test",
        executionOptions = ExecutionOptions(batchSize = 2)
    )
}
