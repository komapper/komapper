package integration.jdbc.postgresql

import integration.core.PostgreSqlSetting
import org.komapper.core.ExecutionOptions
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDialects

@Suppress("unused")
class JdbcPostgreSqlSetting(url: String) : PostgreSqlSetting<JdbcDatabase> {
    override val database: JdbcDatabase by lazy {
        val dataTypeProvider = JdbcDataTypeProvider(PostgreSqlJsonType())
        JdbcDatabase(
            url = url,
            dialect = JdbcDialects.get("postgresql"),
            dataTypeProvider = dataTypeProvider,
            executionOptions = ExecutionOptions(batchSize = 2),
        )
    }
}
