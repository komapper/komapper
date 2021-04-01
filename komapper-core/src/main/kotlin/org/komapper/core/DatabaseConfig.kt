package org.komapper.core

import org.komapper.core.config.ClockProvider
import org.komapper.core.config.DefaultClockProvider
import org.komapper.core.config.DefaultSession
import org.komapper.core.config.Dialect
import org.komapper.core.config.JdbcOption
import org.komapper.core.config.Logger
import org.komapper.core.config.Session
import org.komapper.core.config.StdOutLogger
import org.komapper.core.config.TransactionalSession
import org.komapper.core.jdbc.SimpleDataSource
import javax.sql.DataSource

/**
 * A database configuration.
 *
 * @property name the name of this configuration. The name is used as a key to manage sequence values. The name must be unique.
 * @property dialect the dialect
 * @property logger the logger
 * @property clockProvider the clock provider
 * @property jdbcOption the jdbc configuration
 * @property session the session
 */
interface DatabaseConfig {
    val name: String
    val dialect: Dialect
    val logger: Logger
    val clockProvider: ClockProvider
    val jdbcOption: JdbcOption
    val session: Session
}

open class DefaultDatabaseConfig(
    dataSource: DataSource,
    override val dialect: Dialect,
    enableTransaction: Boolean = false
) :
    DatabaseConfig {

    constructor(
        dialect: Dialect,
        url: String,
        user: String = "",
        password: String = "",
        enableTransaction: Boolean = false
    ) : this(SimpleDataSource(url, user, password), dialect, enableTransaction)

    override val name: String = System.identityHashCode(object {}).toString()
    override val logger: Logger = StdOutLogger()
    override val clockProvider = DefaultClockProvider()
    override val jdbcOption: JdbcOption = JdbcOption(batchSize = 10)
    override val session: Session by lazy {
        if (enableTransaction) {
            TransactionalSession(dataSource, logger)
        } else {
            DefaultSession(dataSource)
        }
    }
}
