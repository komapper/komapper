package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.r2dbc.Binder
import org.komapper.r2dbc.IndexedBinder
import org.komapper.r2dbc.R2dbcDialect

interface PostgreSqlR2dbcDialect : PostgreSqlDialect, R2dbcDialect {

    override fun getBinder(): Binder {
        return IndexedBinder
    }

    override fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean {
        return exception.sqlState == PostgreSqlDialect.UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE
    }

    override fun supportsBatchExecutionOfParameterizedStatement(): Boolean = false
}

private object PostgreSqlR2dbcDialectImpl : PostgreSqlR2dbcDialect

fun PostgreSqlR2dbcDialect(): PostgreSqlR2dbcDialect {
    return PostgreSqlR2dbcDialectImpl
}
