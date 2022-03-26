package org.komapper.dialect.mysql.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.r2dbc.R2dbcDialect

interface R2dbcMySqlDialect : R2dbcDialect, MySqlDialect {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode in MySqlDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}

internal object R2dbcMySqlDialectImpl : R2dbcMySqlDialect

fun R2dbcMySqlDialect(): R2dbcMySqlDialect {
    return R2dbcMySqlDialectImpl
}
