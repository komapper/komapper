package org.komapper.core

import java.util.UUID
import kotlin.reflect.KClass

/**
 * The facade of logger.
 */
interface LoggerFacade {
    /**
     * Logs the sql statement.
     *
     * @param statement the sql statement
     * @param format the format of the sql statement
     */
    fun sql(statement: Statement, format: (Int, StatementPart.Value) -> CharSequence)

    /**
     * Logs the sql statement with arguments.
     *
     * @param statement the sql statement
     * @param format the format of the sql statement
     */
    fun sqlWithArgs(statement: Statement, format: (Any?, KClass<*>, Boolean) -> CharSequence)

    /**
     * Logs the beginning of transaction.
     *
     * @param transactionId the transaction id
     */
    fun begin(transactionId: UUID)

    /**
     * Logs the commit of transaction.
     *
     * @param transactionId the transaction id
     */
    fun commit(transactionId: UUID)

    /**
     * Logs the commit failure of transaction.
     *
     * @param transactionId the transaction id
     * @param cause the cause of failure
     */
    fun commitFailed(transactionId: UUID, cause: Throwable)

    /**
     * Logs the rollback of transaction.
     *
     * @param transactionId the transaction id
     */
    fun rollback(transactionId: UUID)

    /**
     * Logs the rollback failure of transaction.
     *
     * @param transactionId the transaction id
     * @param cause the cause of failure
     */
    fun rollbackFailed(transactionId: UUID, cause: Throwable)

    /**
     * Logs the suspending of transaction.
     *
     * @param transactionId the transaction id
     */
    fun suspend(transactionId: UUID)

    /**
     * Logs the resuming of transaction.
     *
     * @param transactionId the transaction id
     */
    fun resume(transactionId: UUID)

    fun trace(message: () -> String)

    fun debug(message: () -> String)

    fun info(message: () -> String)

    fun warn(message: () -> String)

    fun error(message: () -> String)
}

/**
 * The default implementation of [LoggerFacade].
 */
class DefaultLoggerFacade(private val logger: Logger) : LoggerFacade {
    override fun sql(statement: Statement, format: (Int, StatementPart.Value) -> CharSequence) {
        logger.debug(LogCategory.SQL) {
            statement.toSql(format)
        }
    }

    override fun sqlWithArgs(statement: Statement, format: (Any?, KClass<*>, Boolean) -> CharSequence) {
        logger.trace(LogCategory.SQL_WITH_ARGS) {
            statement.toSqlWithArgs(format)
        }
    }

    override fun begin(transactionId: UUID) {
        logger.trace(LogCategory.TRANSACTION) {
            "The transaction \"$transactionId\" has begun."
        }
    }

    override fun commit(transactionId: UUID) {
        logger.trace(LogCategory.TRANSACTION) {
            "The transaction \"$transactionId\" has committed."
        }
    }

    override fun commitFailed(transactionId: UUID, cause: Throwable) {
        logger.trace(LogCategory.TRANSACTION) {
            "Commit of the transaction \"$transactionId\" failed. $cause"
        }
    }

    override fun rollback(transactionId: UUID) {
        logger.trace(LogCategory.TRANSACTION) {
            "The transaction \"$transactionId\" has rolled back."
        }
    }

    override fun rollbackFailed(transactionId: UUID, cause: Throwable) {
        logger.trace(LogCategory.TRANSACTION) {
            "Rollback of the transaction \"$transactionId\" failed. $cause"
        }
    }

    override fun suspend(transactionId: UUID) {
        logger.trace(LogCategory.TRANSACTION) {
            "The transaction \"$transactionId\" has suspended."
        }
    }

    override fun resume(transactionId: UUID) {
        logger.trace(LogCategory.TRANSACTION) {
            "The transaction \"$transactionId\" has resumed."
        }
    }

    override fun trace(message: () -> String) {
        logger.trace(LogCategory.OTHER, message)
    }

    override fun debug(message: () -> String) {
        logger.debug(LogCategory.OTHER, message)
    }

    override fun info(message: () -> String) {
        logger.info(LogCategory.OTHER, message)
    }

    override fun warn(message: () -> String) {
        logger.warn(LogCategory.OTHER, message)
    }

    override fun error(message: () -> String) {
        logger.error(LogCategory.OTHER, message)
    }
}
