package org.komapper.jdbc

import org.komapper.core.Dialect
import org.komapper.core.ExecutionOptionsProvider
import java.sql.SQLException

/**
 * Represents a dialect for JDBC access.
 */
interface JdbcDialect : Dialect {
    fun createExecutor(
        config: JdbcDatabaseConfig,
        executionOptionProvider: ExecutionOptionsProvider,
        generatedColumn: String? = null,
    ): JdbcExecutor {
        return DefaultJdbcExecutor(config, executionOptionProvider, generatedColumn)
    }

    /**
     * Returns whether the exception indicates that the sequence already exists.
     */
    fun isSequenceExistsError(exception: SQLException): Boolean = false

    /**
     * Returns whether the exception indicates that the sequence does not exist.
     */
    fun isSequenceNotExistsError(exception: SQLException): Boolean = false

    /**
     * Returns whether the exception indicates that the table already exists.
     */
    fun isTableExistsError(exception: SQLException): Boolean = false

    /**
     * Returns whether the exception indicates that the table does not exist.
     */
    fun isTableNotExistsError(exception: SQLException): Boolean = false

    /**
     * Returns whether the exception indicates unique constraint violation.
     *
     * @param exception the exception
     * @return whether the exception indicates unique constraint violation
     */
    fun isUniqueConstraintViolationError(exception: SQLException): Boolean

    /**
     * Returns whether the [java.sql.Statement.RETURN_GENERATED_KEYS] flag is supported.
     *
     * @return whether the RETURN_GENERATED_KEYS flat is supported
     */
    fun supportsReturnGeneratedKeysFlag(): Boolean = true
}
