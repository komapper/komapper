package org.komapper.dialect.sqlserver.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.r2dbc.AtSignBinder
import org.komapper.r2dbc.Binder
import org.komapper.r2dbc.R2dbcDialect

interface SqlServerR2dbcDialect : SqlServerDialect, R2dbcDialect {
    override fun getBinder(): Binder {
        return AtSignBinder
    }

    override fun isSequenceExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == SqlServerDialect.OBJECT_ALREADY_EXISTS_ERROR_CODE
    }

    override fun isTableExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == SqlServerDialect.OBJECT_ALREADY_EXISTS_ERROR_CODE
    }

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode == SqlServerDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun supportsBatchExecutionReturningGeneratedValues(): Boolean = false
}

private object SqlServerR2dbcDialectImpl : SqlServerR2dbcDialect

fun SqlServerR2dbcDialect(): SqlServerR2dbcDialect {
    return SqlServerR2dbcDialectImpl
}
