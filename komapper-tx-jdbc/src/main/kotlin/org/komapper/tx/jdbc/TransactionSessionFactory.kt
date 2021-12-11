package org.komapper.tx.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.jdbc.JdbcSession
import org.komapper.jdbc.spi.JdbcSessionFactory
import javax.sql.DataSource

class TransactionSessionFactory : JdbcSessionFactory {
    override fun create(dataSource: DataSource, loggerFacade: LoggerFacade): JdbcSession {
        return TransactionSession(dataSource, loggerFacade)
    }
}
