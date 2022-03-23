package org.komapper.dialect.oracle.jdbc

import org.komapper.dialect.oracle.OracleDialect
import org.komapper.jdbc.JdbcAbstractDialect
import org.komapper.jdbc.JdbcDataTypeProvider
import java.sql.SQLException

class JdbcOracleDialect(
    dataTypeProvider: JdbcDataTypeProvider
) : OracleDialect, JdbcAbstractDialect(dataTypeProvider) {

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
