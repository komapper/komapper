package org.komapper.ext.spring.boot.autoconfigure

import org.komapper.core.DatabaseSession
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import java.sql.Connection
import javax.sql.DataSource

class TransactionAwareDatabaseSession(dataSource: DataSource) : DatabaseSession {
    private val dataSourceProxy = when (dataSource) {
        is TransactionAwareDataSourceProxy -> dataSource
        else -> TransactionAwareDataSourceProxy(dataSource)
    }

    override val connection: Connection
        get() = dataSourceProxy.connection
}
