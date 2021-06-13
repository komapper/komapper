package org.komapper.jdbc

import org.komapper.core.ThreadSafe
import java.sql.Connection
import javax.sql.DataSource

@ThreadSafe
interface JdbcDatabaseSession {
    val connection: Connection
}

class DefaultJdbcDatabaseSession(private val dataSource: DataSource) : JdbcDatabaseSession {
    override val connection: Connection get() = dataSource.connection
}
