package org.komapper.dialect.mysql.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcDialects

interface R2dbcMySqlDialect : R2dbcDialect, MySqlDialect {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode in MySqlDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}

internal class R2dbcMySqlDialectImpl(
    dataTypeProvider: R2dbcDataTypeProvider
) : R2dbcMySqlDialect, R2dbcAbstractDialect(dataTypeProvider)

fun R2dbcMySqlDialect(dataTypeProvider: R2dbcDataTypeProvider? = null): R2dbcMySqlDialect {
    return R2dbcDialects.get(MySqlDialect.driver, dataTypeProvider) as R2dbcMySqlDialect
}