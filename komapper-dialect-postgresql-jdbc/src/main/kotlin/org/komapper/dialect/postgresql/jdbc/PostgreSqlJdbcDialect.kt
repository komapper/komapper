package org.komapper.dialect.postgresql.jdbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.jdbc.JdbcDialect
import java.sql.SQLException

interface PostgreSqlJdbcDialect : PostgreSqlDialect, JdbcDialect {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.sqlState == PostgreSqlDialect.UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE
        }
    }
}

private object PostgreSqlJdbcDialectImpl : PostgreSqlJdbcDialect

fun PostgreSqlJdbcDialect(): PostgreSqlJdbcDialect {
    return PostgreSqlJdbcDialectImpl
}
