package org.komapper.tx.jdbc

import org.komapper.jdbc.Jdbc
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcSession

/**
 * Begins a JDBC transaction.
 *
 * @param R the return type of the block
 * @param transactionAttribute the transaction attribute
 * @param isolationLevel the isolation level. If null, the default isolation level is determined by the driver.
 * @param block the block executed in the transaction
 * @return the result of the block
 */
fun <R> Jdbc.withTransaction(
    transactionAttribute: JdbcTransactionAttribute = JdbcTransactionAttribute.REQUIRED,
    isolationLevel: JdbcIsolationLevel? = null,
    block: (JdbcUserTransaction) -> R
): R {
    return if (this is JdbcDatabase) {
        val session = this.config.session
        return if (session is JdbcTransactionSession) {
            session.userTransaction.run(transactionAttribute, isolationLevel, block)
        } else {
            withoutTransaction(block)
        }
    } else {
        withoutTransaction(block)
    }
}

private fun <R> withoutTransaction(block: (JdbcUserTransaction) -> R): R {
    val transactionScope = JdbcUserTransactionStub()
    return block(transactionScope)
}

/**
 * The transaction manager.
 */
val JdbcSession.transactionManager: JdbcTransactionManager
    get() {
        return if (this is JdbcTransactionSession) {
            this.transactionManager
        } else {
            invalidSession(this)
        }
    }

private fun invalidSession(session: JdbcSession): Nothing {
    error(
        "DatabaseConfig.session must be an instance of ${JdbcTransactionSession::class.qualifiedName}. " +
            "But it is ${session::class.qualifiedName}"
    )
}
