package org.komapper.dialect.h2.jdbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.jdbc.JdbcDialect
import java.sql.SQLException

interface JdbcH2Dialect : JdbcDialect, H2Dialect {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == H2Dialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }
}

internal object JdbcH2DialectImpl : JdbcH2Dialect

fun JdbcH2Dialect(): JdbcH2Dialect {
    return JdbcH2DialectImpl
}
