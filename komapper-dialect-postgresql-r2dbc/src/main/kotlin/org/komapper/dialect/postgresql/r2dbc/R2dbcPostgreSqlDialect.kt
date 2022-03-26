package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.r2dbc.Binder
import org.komapper.r2dbc.IndexedBinder
import org.komapper.r2dbc.R2dbcAbstractDialect
import org.komapper.r2dbc.R2dbcDataTypeProvider
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcDialects

interface R2dbcPostgreSqlDialect : R2dbcDialect, PostgreSqlDialect {

    override fun getBinder(): Binder {
        return IndexedBinder
    }

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.sqlState == PostgreSqlDialect.UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}

internal class R2dbcPostgreSqlDialectImpl(
    dataTypeProvider: R2dbcDataTypeProvider
) : R2dbcPostgreSqlDialect, R2dbcAbstractDialect(dataTypeProvider)

fun R2dbcPostgreSqlDialect(dataTypeProvider: R2dbcDataTypeProvider? = null): R2dbcPostgreSqlDialect {
    return R2dbcDialects.get(PostgreSqlDialect.driver, dataTypeProvider) as R2dbcPostgreSqlDialect
}