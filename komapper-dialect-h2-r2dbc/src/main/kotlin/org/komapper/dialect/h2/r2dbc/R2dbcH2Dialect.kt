package org.komapper.dialect.h2.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.h2.H2Dialect
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider

open class R2dbcH2Dialect(
    dataTypeProvider: R2dbcDataTypeProvider
) : H2Dialect, R2dbcAbstractDialect(dataTypeProvider) {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode == H2Dialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}
