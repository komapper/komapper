package org.komapper.spring.jdbc

import org.komapper.jdbc.JdbcSession
import org.komapper.tx.core.TransactionOperator
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.transaction.PlatformTransactionManager
import java.sql.Connection
import javax.sql.DataSource

class SpringJdbcTransactionSession(
    transactionManager: PlatformTransactionManager,
    override val dataSource: DataSource,
) :
    JdbcSession {
    override val transactionOperator: TransactionOperator = SpringJdbcTransactionOperator(transactionManager)

    override fun getConnection(): Connection {
        return DataSourceUtils.getConnection(dataSource)
    }

    override fun releaseConnection(connection: Connection) {
        DataSourceUtils.releaseConnection(connection, dataSource)
    }
}
