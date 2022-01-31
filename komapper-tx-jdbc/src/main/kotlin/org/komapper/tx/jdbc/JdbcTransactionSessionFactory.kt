package org.komapper.tx.jdbc

import org.komapper.core.LoggerFacade
import org.komapper.jdbc.JdbcSession
import org.komapper.jdbc.spi.JdbcSessionFactory
import javax.sql.DataSource

class JdbcTransactionSessionFactory : JdbcSessionFactory {
    override fun create(dataSource: DataSource, loggerFacade: LoggerFacade): JdbcSession {
        return JdbcTransactionSession(dataSource, loggerFacade)
    }
}
