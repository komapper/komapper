package org.komapper.dialect.mysql.jdbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import java.sql.SQLException

open class JdbcMySqlDialect(
    dataTypeProvider: JdbcDataTypeProvider
) : MySqlDialect, JdbcAbstractDialect(dataTypeProvider) {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode in MySqlDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
        }
    }
}
