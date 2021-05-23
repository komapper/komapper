package org.komapper.jdbc

import org.komapper.core.ThreadSafe
import java.sql.Connection
import javax.sql.DataSource

@ThreadSafe
interface DatabaseSession {
    val connection: Connection
}

class DefaultDatabaseSession(private val dataSource: DataSource) : DatabaseSession {
    override val connection: Connection get() = dataSource.connection
}
