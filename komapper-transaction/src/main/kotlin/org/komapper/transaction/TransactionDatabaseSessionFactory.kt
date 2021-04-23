package org.komapper.transaction

import org.komapper.core.DatabaseSession
import org.komapper.core.Logger
import org.komapper.core.spi.DatabaseSessionFactory
import javax.sql.DataSource

class TransactionDatabaseSessionFactory : DatabaseSessionFactory {
    override fun create(dataSource: DataSource, logger: Logger): DatabaseSession {
        return TransactionDatabaseSession(dataSource, logger)
    }
}
