package org.komapper.transaction

import org.komapper.core.Logger
import org.komapper.jdbc.DatabaseSession
import org.komapper.jdbc.spi.DatabaseSessionFactory
import javax.sql.DataSource

class TransactionDatabaseSessionFactory : DatabaseSessionFactory {
    override fun create(dataSource: DataSource, logger: Logger): DatabaseSession {
        return TransactionDatabaseSession(dataSource, logger)
    }
}
