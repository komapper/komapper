package integration.jdbc.postgresql

import integration.core.PostgreSqlSetting
import org.komapper.core.ExecutionOptions
import org.komapper.jdbc.JdbcAbstractDataTypeProvider
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDialects
import org.komapper.jdbc.JdbcEmptyDataTypeProvider

@Suppress("unused")
class JdbcPostgreSqlSetting(private val driver: String, url: String) : PostgreSqlSetting<JdbcDatabase> {

    override val database: JdbcDatabase by lazy {
        val dataTypeProvider =
            object : JdbcAbstractDataTypeProvider(JdbcEmptyDataTypeProvider, listOf(PostgreSqlJsonType())) {}
        val dialect = JdbcDialects.getByUrl(url, dataTypeProvider)
        JdbcDatabase(url, dialect = dialect, executionOptions = ExecutionOptions(batchSize = 2))
    }
}
