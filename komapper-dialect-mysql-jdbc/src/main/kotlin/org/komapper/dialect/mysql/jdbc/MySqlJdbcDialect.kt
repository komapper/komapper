package org.komapper.dialect.mysql.jdbc

import org.komapper.dialect.mysql.MySqlDialect
import org.komapper.dialect.mysql.MySqlVersion
import org.komapper.jdbc.JdbcDialect
import java.sql.SQLException

interface MySqlJdbcDialect : MySqlDialect, JdbcDialect {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode in MySqlDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODES
        }
    }
}

private class MySqlJdbcDialectImpl(override val version: MySqlVersion) : MySqlJdbcDialect

fun MySqlJdbcDialect(version: MySqlVersion = MySqlVersion.V8): MySqlJdbcDialect {
    return MySqlJdbcDialectImpl(version)
}
