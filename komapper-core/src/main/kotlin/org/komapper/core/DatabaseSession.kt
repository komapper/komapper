package org.komapper.core

import java.sql.Connection
import javax.sql.DataSource

interface DatabaseSession {
    val connection: Connection
}

class DefaultDatabaseSession(private val dataSource: DataSource) : DatabaseSession {
    override val connection: Connection get() = dataSource.connection
}
