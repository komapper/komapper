package org.komapper.core.spi

import org.komapper.core.DatabaseSession
import org.komapper.core.Logger
import javax.sql.DataSource

interface DatabaseSessionFactory {
    fun create(dataSource: DataSource, logger: Logger): DatabaseSession
}
