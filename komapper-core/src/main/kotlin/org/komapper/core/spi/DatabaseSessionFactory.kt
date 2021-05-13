package org.komapper.core.spi

import org.komapper.core.DatabaseSession
import org.komapper.core.Logger
import org.komapper.core.ThreadSafe
import javax.sql.DataSource

@ThreadSafe
interface DatabaseSessionFactory {
    fun create(dataSource: DataSource, logger: Logger): DatabaseSession
}
