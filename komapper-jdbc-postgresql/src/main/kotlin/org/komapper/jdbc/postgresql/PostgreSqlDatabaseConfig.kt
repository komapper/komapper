package org.komapper.jdbc.postgresql

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.jdbc.SimpleDataSource
import javax.sql.DataSource

open class PostgreSqlDatabaseConfig(
    dataSource: DataSource,
    dialect: PostgreSqlDialect = PostgreSqlDialect(),
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
