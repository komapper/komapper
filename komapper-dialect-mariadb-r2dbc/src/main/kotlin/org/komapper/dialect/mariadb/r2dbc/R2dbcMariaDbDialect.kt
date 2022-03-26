package org.komapper.dialect.mariadb.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.mariadb.MariaDbDialect
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcDialects

interface R2dbcMariaDbDialect : R2dbcDialect, MariaDbDialect {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode in MariaDbDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}

internal class R2dbcMariaDbDialectImpl(
    dataTypeProvider: R2dbcDataTypeProvider
) : R2dbcMariaDbDialect, R2dbcAbstractDialect(dataTypeProvider)

fun R2dbcMariaDbDialect(dataTypeProvider: R2dbcDataTypeProvider? = null): R2dbcMariaDbDialect {
    return R2dbcDialects.get(MariaDbDialect.driver, dataTypeProvider) as R2dbcMariaDbDialect
}