package org.komapper.dialect.sqlserver.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.r2dbc.AtSignBinder
import org.komapper.r2dbc.Binder
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcDialects

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

internal class R2dbcSqlServerDialectImpl(
    dataTypeProvider: R2dbcDataTypeProvider
) : R2dbcSqlServerDialect, R2dbcAbstractDialect(dataTypeProvider)

fun R2dbcSqlServerDialect(dataTypeProvider: R2dbcDataTypeProvider? = null): R2dbcSqlServerDialect {
    return R2dbcDialects.get(SqlServerDialect.driver, dataTypeProvider) as R2dbcSqlServerDialect
}
