package org.komapper.dialect.mariadb.jdbc

import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.jdbc.JdbcDialect
import java.sql.SQLException

interface JdbcMariaDbDialect : JdbcDialect, MariaDbDialect {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode in MariaDbDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
        }
    }
}

internal object JdbcMariaDbDialectImpl : JdbcMariaDbDialect

fun JdbcMariaDbDialect(): JdbcMariaDbDialect {
    return JdbcMariaDbDialectImpl
}
