package org.komapper.core

import kotlin.reflect.KType

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
    fun sqlWithArgs(statement: Statement, format: (Any?, KType, Boolean) -> CharSequence)

    /**
     * Logs the beginning of transaction.
     *
     * @param transaction the transaction
     */
    fun begin(transaction: String)

    /**
     * Logs the commit of transaction.
     *
     * @param transaction the transaction
     */
    fun commit(transaction: String)

    /**
     * Logs the commit failure of transaction.
     *
     * @param transaction the transaction
     * @param cause the cause of failure
     */
    fun commitFailed(transaction: String, cause: Throwable)

    /**
     * Logs the rollback of transaction.
     *
     * @param transaction the transaction
     */
    fun rollback(transaction: String)

    /**
     * Logs the rollback failure of transaction.
     *
     * @param transaction the transaction
     * @param cause the cause of failure
     */
    fun rollbackFailed(transaction: String, cause: Throwable)

    /**
     * Logs the suspending of transaction.
     *
     * @param transaction the transaction
     */
    fun suspend(transaction: String)

    /**
     * Logs the resuming of transaction.
     *
     * @param transaction the transaction
     */
    fun resume(transaction: String)

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

    override fun sqlWithArgs(statement: Statement, format: (Any?, KType, Boolean) -> CharSequence) {
        logger.trace(LogCategory.SQL_WITH_ARGS) {
            statement.toSqlWithArgs(format)
        }
    }

    override fun begin(transaction: String) {
        logger.trace(LogCategory.TRANSACTION) {
            "Begin: $transaction"
        }
    }

    override fun commit(transaction: String) {
        logger.trace(LogCategory.TRANSACTION) {
            "Commit: $transaction"
        }
    }

    override fun commitFailed(transaction: String, cause: Throwable) {
        logger.trace(LogCategory.TRANSACTION) {
            "Commit failed: $transaction, $cause"
        }
    }

    override fun rollback(transaction: String) {
        logger.trace(LogCategory.TRANSACTION) {
            "Rollback: $transaction"
        }
    }

    override fun rollbackFailed(transaction: String, cause: Throwable) {
        logger.trace(LogCategory.TRANSACTION) {
            "Rollback failed: $transaction, $cause"
        }
    }

    override fun suspend(transaction: String) {
        logger.trace(LogCategory.TRANSACTION) {
            "Suspend: $transaction"
        }
    }

    override fun resume(transaction: String) {
        logger.trace(LogCategory.TRANSACTION) {
            "Resume: $transaction"
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
