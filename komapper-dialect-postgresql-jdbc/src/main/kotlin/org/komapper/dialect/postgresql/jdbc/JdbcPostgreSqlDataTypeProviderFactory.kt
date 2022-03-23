package org.komapper.dialect.postgresql.jdbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.spi.JdbcDataTypeProviderFactory

class JdbcPostgreSqlDataTypeProviderFactory : JdbcDataTypeProviderFactory {
    override fun supports(driver: String): Boolean {
        return driver == PostgreSqlDialect.driver
    }

    override fun create(next: JdbcDataTypeProvider): JdbcDataTypeProvider {
        return JdbcPostgreSqlDataTypeProvider(next)
    }
}
