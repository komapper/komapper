package org.komapper.dialect.h2.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.h2.H2Dialect
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcDialects

interface R2dbcH2Dialect: R2dbcDialect, H2Dialect {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode == H2Dialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}

internal class R2dbcH2DialectImpl(
    dataTypeProvider: R2dbcDataTypeProvider
) : R2dbcH2Dialect, R2dbcAbstractDialect(dataTypeProvider)

fun R2dbcH2Dialect(dataTypeProvider: R2dbcDataTypeProvider? = null): R2dbcH2Dialect {
    return R2dbcDialects.get(H2Dialect.driver, dataTypeProvider) as R2dbcH2Dialect
}