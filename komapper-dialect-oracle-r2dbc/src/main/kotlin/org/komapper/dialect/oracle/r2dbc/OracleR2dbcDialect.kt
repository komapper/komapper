package org.komapper.dialect.oracle.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.oracle.OracleDialect
import org.komapper.r2dbc.R2dbcDialect

interface OracleR2dbcDialect : OracleDialect, R2dbcDialect {
    override fun isSequenceExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == OracleDialect.NAME_ALREADY_USED_ERROR_CODE
    }

    override fun isSequenceNotExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == OracleDialect.SEQUENCE_NOT_EXISTS_ERROR_CODE
    }

    override fun isTableExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == OracleDialect.NAME_ALREADY_USED_ERROR_CODE
    }

    override fun isTableNotExistsError(exception: R2dbcException): Boolean {
        return exception.errorCode == OracleDialect.TABLE_NOT_EXISTS_ERROR_CODE
    }

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode == OracleDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun supportsBatchExecutionReturningGeneratedValues(): Boolean = false
}

private object OracleR2dbcDialectImpl : OracleR2dbcDialect

fun OracleR2dbcDialect(): OracleR2dbcDialect {
    return OracleR2dbcDialectImpl
}
