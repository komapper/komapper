package org.komapper.dialect.oracle.jdbc

import org.komapper.dialect.oracle.OracleDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcDialects
import java.sql.SQLException

interface JdbcOracleDialect : JdbcDialect, OracleDialect {

    override fun isSequenceExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == OracleDialect.NAME_ALREADY_USED_ERROR_CODE
        }
    }

    override fun isSequenceNotExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == OracleDialect.SEQUENCE_NOT_EXISTS_ERROR_CODE
        }
    }

    override fun isTableExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == OracleDialect.NAME_ALREADY_USED_ERROR_CODE
        }
    }

    override fun isTableNotExistsError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == OracleDialect.TABLE_NOT_EXISTS_ERROR_CODE
        }
    }

    override fun isUniqueConstraintViolationError(exception: SQLException): Boolean {
        return exception.filterIsInstance<SQLException>().any {
            it.errorCode == OracleDialect.UNIQUE_CONSTRAINT_VIOLATION_ERROR_CODE
        }
    }

    override fun supportsBatchExecutionReturningGeneratedValues(): Boolean = false

    override fun supportsReturnGeneratedKeysFlag(): Boolean = false
}

internal class JdbcOracleDialectImpl(
    dataTypeProvider: JdbcDataTypeProvider
) : JdbcOracleDialect, JdbcAbstractDialect(dataTypeProvider)

fun JdbcOracleDialect(dataTypeProvider: JdbcDataTypeProvider? = null): JdbcOracleDialect {
    return JdbcDialects.get(OracleDialect.driver, dataTypeProvider) as JdbcOracleDialect
}
