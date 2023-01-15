package org.komapper.dialect.mysql.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.r2dbc.R2dbcDialect

interface MySqlR2dbcDialect : MySqlDialect, R2dbcDialect {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode in MySqlDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false

    override fun supportsGeneratedKeysReturningWhenInsertingMultipleRows(): Boolean = false
}

private object MySqlR2dbcDialectImpl : MySqlR2dbcDialect

fun MySqlR2dbcDialect(): MySqlR2dbcDialect {
    return MySqlR2dbcDialectImpl
}
