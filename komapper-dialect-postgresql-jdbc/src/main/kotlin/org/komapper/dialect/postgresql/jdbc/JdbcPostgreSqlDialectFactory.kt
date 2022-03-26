package org.komapper.dialect.postgresql.jdbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class JdbcPostgreSqlDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver.lowercase() == PostgreSqlDialect.driver
    }

    override fun create(): JdbcDialect {
        return JdbcPostgreSqlDialectImpl
    }
}
