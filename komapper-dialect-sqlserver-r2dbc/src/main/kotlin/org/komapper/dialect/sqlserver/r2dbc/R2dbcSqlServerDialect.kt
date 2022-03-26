package org.komapper.dialect.sqlserver.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.r2dbc.AtSignBinder
import org.komapper.r2dbc.Binder
import org.komapper.r2dbc.R2dbcDialect

interface R2dbcSqlServerDialect : R2dbcDialect, SqlServerDialect {

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

internal object R2dbcSqlServerDialectImpl : R2dbcSqlServerDialect

fun R2dbcSqlServerDialect(): R2dbcSqlServerDialect {
    return R2dbcSqlServerDialectImpl
}
