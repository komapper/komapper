package org.komapper.dialect.sqlserver.jdbc

import org.komapper.dialect.sqlserver.SqlServerDialect
import org.komapper.jdbc.JdbcDialect
import java.sql.SQLException

interface JdbcSqlServerDialect : JdbcDialect, SqlServerDialect {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == SqlServerDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }

    override fun isSequenceExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == SqlServerDialect.OBJECT_ALREADY_EXISTS_ERROR_CODE
        }
    }

    override fun isTableExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == SqlServerDialect.OBJECT_ALREADY_EXISTS_ERROR_CODE
        }
    }

    override fun supportsBatchExecutionReturningGeneratedValues(): Boolean = false
}

internal object JdbcSqlServerDialectImpl : JdbcSqlServerDialect

fun JdbcSqlServerDialect(): JdbcSqlServerDialect {
    return JdbcSqlServerDialectImpl
}
