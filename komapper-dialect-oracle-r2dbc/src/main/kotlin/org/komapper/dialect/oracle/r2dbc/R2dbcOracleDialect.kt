package org.komapper.dialect.oracle.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.oracle.OracleDialect
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcDialects

interface R2dbcOracleDialect: R2dbcDialect, OracleDialect {

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

internal class R2dbcOracleDialectImpl(
    dataTypeProvider: R2dbcDataTypeProvider
) : R2dbcOracleDialect, R2dbcAbstractDialect(dataTypeProvider)


fun R2dbcOracleDialect(dataTypeProvider: R2dbcDataTypeProvider? = null): R2dbcOracleDialect {
    return R2dbcDialects.get(OracleDialect.driver, dataTypeProvider) as R2dbcOracleDialect
}