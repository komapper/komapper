package org.komapper.dialect.h2.jdbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.jdbc.JdbcDialect
import java.sql.SQLException

interface H2JdbcDialect : H2Dialect, JdbcDialect {
    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == H2Dialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }

    override fun supportsReturnGeneratedKeysFlag(): Boolean = false
}

private object H2JdbcDialectImpl : H2JdbcDialect

fun H2JdbcDialect(): H2JdbcDialect {
    return H2JdbcDialectImpl
}
