package org.komapper.dialect.mariadb.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.r2dbc.R2dbcDialect

interface MariaDbR2dbcDialect : MariaDbDialect, R2dbcDialect {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode in MariaDbDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}

private object MariaDbR2dbcDialectImpl : MariaDbR2dbcDialect

fun MariaDbR2dbcDialect(): MariaDbR2dbcDialect {
    return MariaDbR2dbcDialectImpl
}
