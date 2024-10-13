package org.komapper.r2dbc

import io.r2dbc.spi.R2dbcException
import org.komapper.core.Dialect
import org.komapper.core.StatementPart

/**
 * Represents a dialect for R2DBC access.
 */
interface R2dbcDialect : Dialect {
    fun getBinder(): Binder {
        return DefaultBinder
    }

    override fun createBindVariable(index: Int, value: StatementPart.Value): CharSequence {
        val binder = getBinder()
        return binder.createBindVariable(index, value)
    }

    /**
     * Returns whether the exception indicates that the sequence already exists.
     */
    fun isSequenceExistsError(exception: R2dbcException): Boolean = false

    /**
     * Returns whether the exception indicates that the sequence does not exist.
     */
    fun isSequenceNotExistsError(exception: R2dbcException): Boolean = false

    /**
     * Returns whether the exception indicates that the table already exists.
     */
    fun isTableExistsError(exception: R2dbcException): Boolean = false

    /**
     * Returns whether the exception indicates that the table does not exist.
     */
    fun isTableNotExistsError(exception: R2dbcException): Boolean = false

    /**
     * Returns whether the exception indicates unique constraint violation.
     *
     * @param exception the exception
     * @return whether the exception indicates unique constraint violation
     */
    fun isUniqueConstraintViolationError(exception: R2dbcException): Boolean

    override fun supportsBatchExecutionReturningGeneratedValues(): Boolean = false
}
