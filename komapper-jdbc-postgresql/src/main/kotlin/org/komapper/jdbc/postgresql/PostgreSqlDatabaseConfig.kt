package org.komapper.jdbc.postgresql

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.jdbc.SimpleDataSource
import javax.sql.DataSource

open class PostgreSqlDatabaseConfig(
    dataSource: DataSource,
    dialect: PostgreSqlDialect = PostgreSqlDialect()
) :
    DefaultDatabaseConfig(dataSource, dialect) {

    constructor(
        url: String,
        user: String = "",
        password: String = "",
    ) : this(SimpleDataSource(url, user, password))
}
