package org.komapper.tx.jdbc

import org.komapper.core.Logger
import org.komapper.jdbc.JdbcSession
import org.komapper.jdbc.spi.JdbcSessionFactory
import javax.sql.DataSource

class TransactionSessionFactory : JdbcSessionFactory {
    override fun create(dataSource: DataSource, logger: Logger): JdbcSession {
        return TransactionSession(dataSource, logger)
    }
}
