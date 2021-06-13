package org.komapper.tx.jdbc

import org.komapper.core.Logger
import org.komapper.jdbc.JdbcDatabaseSession
import org.komapper.jdbc.spi.DatabaseSessionFactory
import javax.sql.DataSource

class TransactionDatabaseSessionFactory : DatabaseSessionFactory {
    override fun create(dataSource: DataSource, logger: Logger): JdbcDatabaseSession {
        return TransactionDatabaseSession(dataSource, logger)
    }
}
