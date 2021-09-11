package org.komapper.jdbc

import org.komapper.core.ThreadSafe
import java.sql.Connection
import javax.sql.DataSource

@ThreadSafe
interface JdbcSession {
    val connection: Connection
}

class DefaultJdbcSession(private val dataSource: DataSource) : JdbcSession {
    override val connection: Connection get() = dataSource.connection
}
