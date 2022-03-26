package org.komapper.dialect.postgresql.jdbc

import org.komapper.dialect.postgresql.PostgreSqlDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcDialects
import java.sql.SQLException

interface JdbcPostgreSqlDialect : JdbcDialect, PostgreSqlDialect {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.sqlState == PostgreSqlDialect.UNIQUE_CONSTRAINT_VIOLATION_STATE_CODE
        }
    }
}

internal class JdbcPostgreSqlDialectImpl(
    dataTypeProvider: JdbcDataTypeProvider
) : JdbcPostgreSqlDialect, JdbcAbstractDialect(dataTypeProvider)

fun JdbcPostgreSqlDialect(dataTypeProvider: JdbcDataTypeProvider? = null): JdbcPostgreSqlDialect {
    return JdbcDialects.get(PostgreSqlDialect.driver, dataTypeProvider) as JdbcPostgreSqlDialect
}
