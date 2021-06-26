package org.komapper.jdbc.spi

import org.komapper.core.Logger
import org.komapper.core.ThreadSafe
import org.komapper.jdbc.JdbcDatabaseSession
import javax.sql.DataSource

@ThreadSafe
interface JdbcDatabaseSessionFactory {
    fun create(dataSource: DataSource, logger: Logger): JdbcDatabaseSession
}
