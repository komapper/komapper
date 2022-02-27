package org.komapper.quarkus.jdbc

import org.komapper.jdbc.JdbcSession
import org.komapper.jdbc.JdbcTransactionalOperator
import java.sql.Connection
import javax.sql.DataSource
import javax.transaction.TransactionManager

internal class TransactionManagerSession(
    transactionManager: TransactionManager,
    private val dataSource: DataSource
) : JdbcSession {
    override val transactionalOperator: JdbcTransactionalOperator = TransactionManagerOperator(transactionManager)

    override fun getConnection(): Connection {
        return dataSource.connection
    }
}