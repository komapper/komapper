package org.komapper.dialect.mariadb.jdbc

import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import java.sql.SQLException

open class JdbcMariaDbDialect(
    dataTypeProvider: JdbcDataTypeProvider
) : MariaDbDialect, JdbcAbstractDialect(dataTypeProvider) {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode in MariaDbDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
        }
    }
}
