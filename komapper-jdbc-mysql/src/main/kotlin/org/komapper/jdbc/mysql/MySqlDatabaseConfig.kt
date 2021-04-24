package org.komapper.jdbc.mysql

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.jdbc.SimpleDataSource
import javax.sql.DataSource

open class MySqlDatabaseConfig(
    dataSource: DataSource,
    dialect: MySqlDialect = MySqlDialect()
) :
    DefaultDatabaseConfig(dataSource, dialect) {

    constructor(
        url: String,
        user: String = "",
        password: String = "",
    ) : this(SimpleDataSource(url, user, password))
}
