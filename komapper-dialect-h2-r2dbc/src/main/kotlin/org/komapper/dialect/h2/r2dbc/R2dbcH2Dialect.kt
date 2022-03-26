package org.komapper.dialect.h2.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.h2.H2Dialect
import org.komapper.r2dbc.R2dbcDialect

interface R2dbcH2Dialect : R2dbcDialect, H2Dialect {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode == H2Dialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}

internal object R2dbcH2DialectImpl : R2dbcH2Dialect

fun R2dbcH2Dialect(): R2dbcH2Dialect {
    return R2dbcH2DialectImpl
}
