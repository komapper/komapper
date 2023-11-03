package integration.jdbc.postgresql

import integration.core.PostgreSqlSetting
import org.komapper.core.ExecutionOptions
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDatabase

@Suppress("unused")
class JdbcPostgreSqlSetting(url: String) : PostgreSqlSetting<JdbcDatabase> {

    override val database: JdbcDatabase by lazy {
        val dataTypeProvider = JdbcDataTypeProvider(PostgreSqlJsonType())
        JdbcDatabase(
            url = url,
            dataTypeProvider = dataTypeProvider,
            executionOptions = ExecutionOptions(batchSize = 2),
        )
    }
}
