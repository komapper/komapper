package org.komapper.dialect.h2.jdbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import java.sql.SQLException

open class JdbcH2Dialect(
    dataTypeProvider: JdbcDataTypeProvider
) : H2Dialect, JdbcAbstractDialect(dataTypeProvider) {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == H2Dialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }
}
