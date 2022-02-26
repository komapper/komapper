package org.komapper.spring.boot.autoconfigure.jdbc

import org.komapper.jdbc.JdbcSession
import org.komapper.jdbc.ThreadTransaction
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import java.sql.Connection
import javax.sql.DataSource

class JdbcTransactionAwareSession(dataSource: DataSource) : JdbcSession {
    private val dataSourceProxy = when (dataSource) {
        is TransactionAwareDataSourceProxy -> dataSource
        else -> TransactionAwareDataSourceProxy(dataSource)
    }

    override val threadTransaction: ThreadTransaction
        get() = TODO("Not yet implemented")

    override fun getConnection(): Connection {
        return dataSourceProxy.connection
    }
}
