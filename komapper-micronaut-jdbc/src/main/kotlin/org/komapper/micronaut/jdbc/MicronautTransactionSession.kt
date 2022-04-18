package org.komapper.micronaut.jdbc

import io.micronaut.transaction.TransactionOperations
import io.micronaut.transaction.jdbc.DataSourceUtils
import org.komapper.jdbc.JdbcSession
import org.komapper.tx.core.TransactionOperator
import java.sql.Connection
import javax.sql.DataSource

class MicronautTransactionSession(
    operations: TransactionOperations<Connection>,
    private val dataSource: DataSource
) :
    JdbcSession {

    override val transactionOperator: TransactionOperator = MicronautTransactionOperator(operations)

    override fun getConnection(): Connection {
        return DataSourceUtils.getConnection(dataSource)
    }

    override fun releaseConnection(connection: Connection) {
        DataSourceUtils.releaseConnection(connection, dataSource)
    }
}
