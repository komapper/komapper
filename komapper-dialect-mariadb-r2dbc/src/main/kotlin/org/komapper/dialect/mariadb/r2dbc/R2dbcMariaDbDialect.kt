package org.komapper.dialect.mariadb.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider

open class R2dbcMariaDbDialect(
    dataTypeProvider: R2dbcDataTypeProvider
) : MariaDbDialect, R2dbcAbstractDialect(dataTypeProvider) {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode in MariaDbDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}
