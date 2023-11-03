package integration.jdbc.mysql5

import integration.core.MySql5Setting
import org.komapper.core.ExecutionOptions
import org.komapper.dialect.mysql.MySqlVersion
import org.komapper.dialect.mysql.jdbc.MySqlJdbcDialect
import org.komapper.jdbc.JdbcDatabase

@Suppress("unused")
class JdbcMySql5Setting(url: String) : MySql5Setting<JdbcDatabase> {
    override val database: JdbcDatabase = JdbcDatabase(
        url,
        "test",
        "test",
        dialect = MySqlJdbcDialect(MySqlVersion.V5),
        executionOptions = ExecutionOptions(batchSize = 2),
    )
}
