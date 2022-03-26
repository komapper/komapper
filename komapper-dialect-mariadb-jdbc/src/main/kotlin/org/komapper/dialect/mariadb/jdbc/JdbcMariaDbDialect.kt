package org.komapper.dialect.mariadb.jdbc

import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcDialects
import java.sql.SQLException

interface JdbcMariaDbDialect: JdbcDialect, MariaDbDialect {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode in MariaDbDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
        }
    }
}

internal class JdbcMariaDbDialectImpl(
    dataTypeProvider: JdbcDataTypeProvider
) : JdbcMariaDbDialect, JdbcAbstractDialect(dataTypeProvider)

fun JdbcMariaDbDialect(dataTypeProvider: JdbcDataTypeProvider? = null): JdbcMariaDbDialect {
    return JdbcDialects.get(MariaDbDialect.driver, dataTypeProvider) as JdbcMariaDbDialect
}