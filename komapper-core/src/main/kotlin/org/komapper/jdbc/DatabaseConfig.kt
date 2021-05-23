package org.komapper.jdbc

import org.komapper.core.ClockProvider
import org.komapper.core.DefaultClockProvider
import org.komapper.core.JdbcOption
import org.komapper.core.Logger
import org.komapper.core.StdOutLogger
import org.komapper.core.TemplateStatementBuilder
import org.komapper.core.ThreadSafe
import org.komapper.core.spi.DefaultStatementInspector
import org.komapper.core.spi.LoggerFactory
import org.komapper.core.spi.StatementInspector
import org.komapper.core.spi.TemplateStatementBuilderFactory
import org.komapper.jdbc.spi.DatabaseSessionFactory
import java.util.ServiceLoader
import java.util.UUID
import javax.sql.DataSource

/**
 * A database configuration.
 *
 * @property id the id of this configuration. The id is used as a key to manage sequence values. The name must be unique.
 * @property dialect the dialect
 * @property logger the logger
 * @property clockProvider the clock provider
 * @property jdbcOption the jdbc configuration
 * @property session the session
 */
@ThreadSafe
interface DatabaseConfig {
    val id: UUID
    val dialect: JdbcDialect
    val clockProvider: ClockProvider
    val jdbcOption: JdbcOption
    val logger: Logger
    val session: DatabaseSession
    val statementInspector: StatementInspector
    val dataFactory: DataFactory
    val templateStatementBuilder: TemplateStatementBuilder
}

open class DefaultDatabaseConfig(
    dataSource: DataSource,
    override val dialect: JdbcDialect
) : DatabaseConfig {

    constructor(
        url: String,
        user: String = "",
        password: String = "",
        dataTypes: List<DataType<*>> = emptyList()
    ) : this(SimpleDataSource(url, user, password), JdbcDialect.load(url, dataTypes))

    constructor(
        url: String,
        user: String = "",
        password: String = "",
        dialect: JdbcDialect
    ) : this(SimpleDataSource(url, user, password), dialect)

    override val id: UUID = UUID.randomUUID()
    override val clockProvider: ClockProvider = DefaultClockProvider()
    override val jdbcOption: JdbcOption = JdbcOption(batchSize = 10)
    override val logger: Logger by lazy {
        val loader = ServiceLoader.load(LoggerFactory::class.java)
        val factory = loader.firstOrNull()
        factory?.create() ?: StdOutLogger()
    }

    override val session: DatabaseSession by lazy {
        val loader = ServiceLoader.load(DatabaseSessionFactory::class.java)
        val factory = loader.firstOrNull()
        factory?.create(dataSource, logger) ?: DefaultDatabaseSession(dataSource)
    }

    override val statementInspector: StatementInspector by lazy {
        val loader = ServiceLoader.load(StatementInspector::class.java)
        loader.firstOrNull() ?: DefaultStatementInspector()
    }

    override val dataFactory: DataFactory by lazy {
        DefaultDataFactory(session)
    }

    override val templateStatementBuilder: TemplateStatementBuilder by lazy {
        val loader = ServiceLoader.load(TemplateStatementBuilderFactory::class.java)
        val factory = loader.firstOrNull()
            ?: error(
                "TemplateStatementBuilderFactory is not found. " +
                    "Add komapper-template dependency or override the templateStatementBuilder property."
            )
        factory.create(dialect)
    }
}

object DryRunDatabaseConfig : DatabaseConfig {
    override val id: UUID
        get() = throw UnsupportedOperationException()
    override val dialect: JdbcDialect = DryRunJdbcDialect
    override val logger: Logger
        get() = throw UnsupportedOperationException()
    override val clockProvider: ClockProvider
        get() = throw UnsupportedOperationException()
    override val jdbcOption: JdbcOption
        get() = throw UnsupportedOperationException()
    override val session: DatabaseSession
        get() = throw UnsupportedOperationException()
    override val statementInspector: StatementInspector
        get() = throw UnsupportedOperationException()
    override val dataFactory: DataFactory
        get() = throw UnsupportedOperationException()
    override val templateStatementBuilder: TemplateStatementBuilder
        get() = throw UnsupportedOperationException()
}
