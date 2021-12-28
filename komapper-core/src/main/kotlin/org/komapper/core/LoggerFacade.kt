package org.komapper.core

import java.util.UUID
import kotlin.reflect.KClass

interface LoggerFacade {
    fun sql(
        statement: Statement,
        format: (Int, StatementPart.PlaceHolder) -> CharSequence = { _, placeHolder -> placeHolder }
    )

    fun sqlWithArgs(statement: Statement, format: (Any?, KClass<*>, Boolean) -> CharSequence)
    fun begin(transactionId: UUID)
    fun commit(transactionId: UUID)
    fun rollback(transactionId: UUID)
}

class DefaultLoggerFacade(private val logger: Logger) : LoggerFacade {
    override fun sql(statement: Statement, format: (Int, StatementPart.PlaceHolder) -> CharSequence) {
        logger.debug(LogCategory.SQL.value) {
            statement.toSql(format)
        }
    }

    override fun sqlWithArgs(statement: Statement, format: (Any?, KClass<*>, Boolean) -> CharSequence) {
        logger.trace(LogCategory.SQL_WITH_ARGS.value) {
            statement.toSqlWithArgs(format)
        }
    }

    override fun begin(transactionId: UUID) {
        logger.trace(LogCategory.TRANSACTION.value) {
            "The transaction \"$transactionId\" has begun."
        }
    }

    override fun commit(transactionId: UUID) {
        logger.trace(LogCategory.TRANSACTION.value) {
            "The transaction \"$transactionId\" has committed."
        }
    }

    override fun rollback(transactionId: UUID) {
        logger.trace(LogCategory.TRANSACTION.value) {
            "The transaction \"$transactionId\" has rolled back."
        }
    }
}
