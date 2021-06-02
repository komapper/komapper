package org.komapper.dialect.mysql.jdbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.jdbc.DataType
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.spi.JdbcDialectFactory

class MySqlJdbcDialectFactory : JdbcDialectFactory {
    override fun supports(driver: String): Boolean {
        return driver == MySqlDialect.driver
    }

    override fun create(dataTypes: List<DataType<*>>): JdbcDialect {
        return MySqlJdbcDialect(dataTypes)
    }
}
