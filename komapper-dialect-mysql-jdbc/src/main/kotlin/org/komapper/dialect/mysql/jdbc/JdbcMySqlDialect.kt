package org.komapper.dialect.mysql.jdbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.jdbc.JdbcDialect
import java.sql.SQLException

interface JdbcMySqlDialect : JdbcDialect, MySqlDialect {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode in MySqlDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
        }
    }
}

internal object JdbcMySqlDialectImpl : JdbcMySqlDialect

fun JdbcMySqlDialect(): JdbcMySqlDialect {
    return JdbcMySqlDialectImpl
}
