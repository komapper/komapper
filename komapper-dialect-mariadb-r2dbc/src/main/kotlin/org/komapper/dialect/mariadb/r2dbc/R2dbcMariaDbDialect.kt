package org.komapper.dialect.mariadb.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.r2dbc.R2dbcDialect

interface R2dbcMariaDbDialect : R2dbcDialect, MariaDbDialect {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode in MariaDbDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}

internal object R2dbcMariaDbDialectImpl : R2dbcMariaDbDialect

fun R2dbcMariaDbDialect(): R2dbcMariaDbDialect {
    return R2dbcMariaDbDialectImpl
}
