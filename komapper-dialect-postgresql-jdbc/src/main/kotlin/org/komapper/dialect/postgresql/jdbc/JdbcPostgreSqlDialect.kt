package org.komapper.dialect.postgresql.jdbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import java.sql.SQLException

open class JdbcPostgreSqlDialect(
    dataTypeProvider: JdbcDataTypeProvider
) : PostgreSqlDialect, JdbcAbstractDialect(dataTypeProvider) {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.sqlState == PostgreSqlDialect.UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE
        }
    }
}
