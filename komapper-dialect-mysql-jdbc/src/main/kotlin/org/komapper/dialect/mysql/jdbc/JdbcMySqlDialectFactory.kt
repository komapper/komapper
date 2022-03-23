package org.komapper.dialect.mysql.jdbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class JdbcMySqlDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver == MySqlDialect.DRIVER
    }

    override fun create(dataTypeProvider: JdbcDataTypeProvider): JdbcDialect {
        return JdbcMySqlDialect(dataTypeProvider)
    }
}
