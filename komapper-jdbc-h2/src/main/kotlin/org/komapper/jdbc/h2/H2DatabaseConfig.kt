package org.komapper.jdbc.h2

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.jdbc.SimpleDataSource
import javax.sql.DataSource

open class H2DatabaseConfig(dataSource: DataSource, dialect: H2Dialect = H2Dialect()) :
    DefaultDatabaseConfig(dialect, dataSource) {

    constructor(
        url: String,
        user: String = "",
        password: String = ""
    ) : this(SimpleDataSource(url, user, password))
}
