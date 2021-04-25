package org.komapper.core

import org.komapper.core.jdbc.DataType
import javax.sql.DataSource

/**
 * A database.
 *
 * @property config the database configuration
 * @constructor creates a database instance
 */
interface Database : DatabaseConfigHolder {
    val dataFactory: DataFactory

    companion object {
        /**
         * @param config the database configuration
         */
        fun create(config: DatabaseConfig): Database {
            return DatabaseImpl(config)
        }

        fun create(
            dataSource: DataSource,
            dialect: Dialect,
        ): Database {
            return create(DefaultDatabaseConfig(dataSource, dialect))
        }

        fun create(
            url: String,
            user: String = "",
            password: String = "",
            dataTypes: Set<DataType<*>> = emptySet()
        ): Database {
            return create(DefaultDatabaseConfig(url, user, password, dataTypes))
        }

        fun create(
            url: String,
            user: String = "",
            password: String = "",
            dialect: Dialect
        ): Database {
            return create(DefaultDatabaseConfig(url, user, password, dialect))
        }
    }
}

internal class DatabaseImpl(
    override val config: DatabaseConfig
) : Database {
    override val dataFactory: DataFactory
        get() = config.dataFactory
}
