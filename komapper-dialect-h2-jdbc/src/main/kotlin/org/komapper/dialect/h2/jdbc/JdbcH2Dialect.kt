package org.komapper.dialect.h2.jdbc

import org.komapper.dialect.h2.H2Dialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcDialects
import java.sql.SQLException

interface JdbcH2Dialect : JdbcDialect, H2Dialect {

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == H2Dialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }
}

internal class JdbcH2DialectImpl(
    dataTypeProvider: JdbcDataTypeProvider
) : JdbcH2Dialect, JdbcAbstractDialect(dataTypeProvider)

fun JdbcH2Dialect(dataTypeProvider: JdbcDataTypeProvider? = null): JdbcH2Dialect {
    return JdbcDialects.get(H2Dialect.driver, dataTypeProvider) as JdbcH2Dialect
}