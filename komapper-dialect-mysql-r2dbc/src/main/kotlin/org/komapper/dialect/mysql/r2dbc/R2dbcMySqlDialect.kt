package org.komapper.dialect.mysql.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider

open class R2dbcMySqlDialect(
    dataTypeProvider: R2dbcDataTypeProvider
) : MySqlDialect, R2dbcAbstractDialect(dataTypeProvider) {

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.errorCode in MySqlDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}
