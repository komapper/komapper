package org.komapper.jdbc.spi

import org.komapper.core.Logger
import org.komapper.core.ThreadSafe
import org.komapper.core.spi.Prioritized
import org.komapper.jdbc.JdbcSession
import javax.sql.DataSource

@ThreadSafe
interface JdbcSessionFactory : Prioritized {
    fun create(dataSource: DataSource, logger: Logger): JdbcSession
}
