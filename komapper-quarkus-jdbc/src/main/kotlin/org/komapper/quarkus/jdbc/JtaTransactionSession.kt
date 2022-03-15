package org.komapper.quarkus.jdbc

import org.komapper.jdbc.JdbcSession
import org.komapper.tx.core.TransactionOperator
import java.sql.Connection
import javax.sql.DataSource
import javax.transaction.TransactionManager

class JtaTransactionSession(
    transactionManager: TransactionManager,
    private val dataSource: DataSource
) : JdbcSession {
    override val transactionOperator: TransactionOperator = JtaTransactionOperator(transactionManager)

    override fun getConnection(): Connection {
        return dataSource.connection
    }

    override fun releaseConnection(connection: Connection) {
        connection.close()
    }
}