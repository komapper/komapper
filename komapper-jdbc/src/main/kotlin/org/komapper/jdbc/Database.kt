package org.komapper.jdbc

import org.komapper.core.ThreadSafe
import org.komapper.jdbc.dsl.query.Query
import org.komapper.jdbc.dsl.query.QueryScope
import javax.sql.DataSource

/**
 * A database.
 */
@ThreadSafe
interface Database {

    companion object {
        /**
         * @param config the database configuration
         */
        fun create(config: DatabaseConfig): Database {
            return DatabaseImpl(config)
        }

        fun create(
            dataSource: DataSource,
            dialect: JdbcDialect,
        ): Database {
            return create(DefaultDatabaseConfig(dataSource, dialect))
        }

        fun create(
            url: String,
            user: String = "",
            password: String = "",
            dataTypes: List<DataType<*>> = emptyList()
        ): Database {
            return create(DefaultDatabaseConfig(url, user, password, dataTypes))
        }

        fun create(
            url: String,
            user: String = "",
            password: String = "",
            dialect: JdbcDialect
        ): Database {
            return create(DefaultDatabaseConfig(url, user, password, dialect))
        }
    }

    val config: DatabaseConfig
    val dataFactory: DataFactory

    fun <T> runQuery(block: QueryScope.() -> Query<T>): T {
        return block(QueryScope).run(this.config)
    }
}

internal class DatabaseImpl(
    override val config: DatabaseConfig
) : Database {
    override val dataFactory: DataFactory
        get() = config.dataFactory
}
