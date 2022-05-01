package org.komapper.dialect.h2.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.h2.H2Dialect
import org.komapper.r2dbc.R2dbcDialect

interface H2R2dbcDialect : H2Dialect, R2dbcDialect {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode == H2Dialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}

private object H2R2dbcDialectImpl : H2R2dbcDialect

fun H2R2dbcDialect(): H2R2dbcDialect {
    return H2R2dbcDialectImpl
}
