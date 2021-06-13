package org.komapper.dialect.mysql.jdbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class MySqlJdbcDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver == MySqlDialect.driver
    }

    override fun create(dataTypes: List<JdbcDataType<*>>): JdbcDialect {
        return MySqlJdbcDialect(dataTypes)
    }
}
