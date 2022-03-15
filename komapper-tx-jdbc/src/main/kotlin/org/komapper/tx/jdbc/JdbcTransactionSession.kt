package org.komapper.tx.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.jdbc.JdbcSession
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionOperator
import org.komapper.tx.core.TransactionProperty
import java.sql.Connection
import javax.sql.DataSource

/**
 * Represents a transactional session for JDBC.
 */
class JdbcTransactionSession(
    dataSource: DataSource,
    loggerFacade: LoggerFacade,
    transactionProperty: TransactionProperty = EmptyTransactionProperty
) : JdbcSession {

    val transactionManager: JdbcTransactionManager =
        JdbcTransactionManagerImpl(dataSource, loggerFacade)

    override val transactionOperator: TransactionOperator =
        JdbcTransactionOperator(transactionManager, transactionProperty)

    override fun getConnection(): Connection {
        return transactionManager.getConnection()
    }

    override fun releaseConnection(connection: Connection) {
        connection.close()
    }
}
