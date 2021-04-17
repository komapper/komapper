package org.komapper.jdbc.mysql

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.jdbc.SimpleDataSource
import javax.sql.DataSource

open class MySqlDatabaseConfig(
    dataSource: DataSource,
    dialect: MySqlDialect = MySqlDialect(),
    enableTransaction: Boolean = false
) :
    DefaultDatabaseConfig(dataSource, dialect, enableTransaction) {

    constructor(
        url: String,
        user: String = "",
        password: String = "",
        enableTransaction: Boolean = false
    ) : this(SimpleDataSource(url, user, password), enableTransaction = enableTransaction)
}
