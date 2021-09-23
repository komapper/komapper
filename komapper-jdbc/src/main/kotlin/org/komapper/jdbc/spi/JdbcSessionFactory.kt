package org.komapper.jdbc.spi

import org.komapper.core.Logger
import org.komapper.core.ThreadSafe
import org.komapper.jdbc.JdbcSession
import javax.sql.DataSource

@ThreadSafe
interface JdbcSessionFactory {
    fun create(dataSource: DataSource, logger: Logger): JdbcSession
}
