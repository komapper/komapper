package org.komapper.dialect.mysql.jdbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcDialects
import java.sql.SQLException

interface JdbcMySqlDialect : JdbcDialect, MySqlDialect {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode in MySqlDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
        }
    }
}

internal class JdbcMySqlDialectImpl(
    dataTypeProvider: JdbcDataTypeProvider
) : JdbcMySqlDialect, JdbcAbstractDialect(dataTypeProvider) 

fun JdbcMySqlDialect(dataTypeProvider: JdbcDataTypeProvider? = null): JdbcMySqlDialect {
    return JdbcDialects.get(MySqlDialect.driver, dataTypeProvider) as JdbcMySqlDialect
}