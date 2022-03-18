package integration.jdbc.postgresql

import integration.core.PostgreSqlSetting
import org.komapper.core.ExecutionOptions
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDialects

@Suppress("unused")
class JdbcPostgreSqlSetting(private val driver: String, url: String) : PostgreSqlSetting<JdbcDatabase> {

    override val database: JdbcDatabase by lazy {
        val dialect = JdbcDialects.getByUrl(url, listOf(PostgreSqlJsonType()))
        JdbcDatabase(url, dialect = dialect, executionOptions = ExecutionOptions(batchSize = 2))
    }
}
