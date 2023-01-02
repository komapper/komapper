package integration.jdbc.h2

import integration.core.H2Setting
import org.komapper.core.ExecutionOptions
import org.komapper.jdbc.JdbcDatabase

@Suppress("unused")
class JdbcH2Setting(private val driver: String, url: String) : H2Setting<JdbcDatabase> {
    override val database: JdbcDatabase = JdbcDatabase(
        url,
        executionOptions = ExecutionOptions(batchSize = 2),
    )
}
