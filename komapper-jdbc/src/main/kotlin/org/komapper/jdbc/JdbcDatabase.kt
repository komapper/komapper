package org.komapper.jdbc

import org.komapper.core.ClockProvider
import org.komapper.core.Database
import org.komapper.core.DefaultClockProvider
import org.komapper.core.ExecutionOptions
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.jdbc.dsl.runner.JdbcRunner
import org.komapper.jdbc.dsl.visitor.JdbcQueryVisitor
import org.komapper.tx.core.EmptyTransactionProperty
import org.komapper.tx.core.TransactionAttribute
import org.komapper.tx.core.TransactionOperator
import org.komapper.tx.core.TransactionProperty
import javax.sql.DataSource

/**
 * Represents a database accessed by JDBC.
 */
interface JdbcDatabase : Database {

    /**
     * The database configuration.
     */
    override val config: JdbcDatabaseConfig

    /**
     * The data factory.
     */
    val dataFactory: JdbcDataFactory

    /**
     * Runs the given [query] and returns the result.
     * @param query the query
     * @return the result represented by the query
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> runQuery(query: Query<T>): T {
        val runner = query.accept(JdbcQueryVisitor) as JdbcRunner<T>
        runner.check(config)
        return runner.run(config)
    }

    /**
     * Runs the given [block] and returns the result.
     * @param block the block that returns a query
     * @return the result represented by the query
     */
    fun <T> runQuery(block: QueryScope.() -> Query<T>): T {
        val query = block(QueryScope)
        return runQuery(query)
    }

    /**
     * Begins a JDBC transaction.
     *
     * @param R the return type of the block
     * @param transactionAttribute the transaction attribute
     * @param transactionProperty the transaction property
     * @param block the block executed in the transaction
     * @return the result of the block
     */
    fun <R> withTransaction(
        transactionAttribute: TransactionAttribute = TransactionAttribute.REQUIRED,
        transactionProperty: TransactionProperty = EmptyTransactionProperty,
        block: (TransactionOperator) -> R
    ): R {
        val tx = config.session.transactionOperator
        return when (transactionAttribute) {
            TransactionAttribute.REQUIRED -> tx.required(transactionProperty, block)
            TransactionAttribute.REQUIRES_NEW -> tx.requiresNew(transactionProperty, block)
        }
    }
}

internal class JdbcDatabaseImpl(
    override val config: JdbcDatabaseConfig
) : JdbcDatabase {
    override val dataFactory: JdbcDataFactory
        get() = config.dataFactory
}

/**
 * Creates a [JdbcDatabase] instance.
 *
 * @param config the database configuration
 */
fun JdbcDatabase(config: JdbcDatabaseConfig): JdbcDatabase {
    return JdbcDatabaseImpl(config)
}

/**
 * Creates a [JdbcDatabase] instance.
 *
 * @param dataSource the JDBC data source
 * @param dialect the dialect
 * @param clockProvider the clock provider
 * @param executionOptions the execution options
 */
fun JdbcDatabase(
    dataSource: DataSource,
    dialect: JdbcDialect,
    clockProvider: ClockProvider = DefaultClockProvider(),
    executionOptions: ExecutionOptions = ExecutionOptions(),
): JdbcDatabase {
    val config = DefaultJdbcDatabaseConfig(dataSource, dialect, clockProvider, executionOptions)
    return JdbcDatabase(config)
}

/**
 * Creates a [JdbcDatabase] instance.
 *
 * @param url the JDBC URL
 * @param user the JDBC user
 * @param password the JDBC password
 * @param clockProvider the clock provider
 * @param executionOptions the execution options
 * @param dataTypes the data types
 */
fun JdbcDatabase(
    url: String,
    user: String = "",
    password: String = "",
    clockProvider: ClockProvider = DefaultClockProvider(),
    executionOptions: ExecutionOptions = ExecutionOptions(),
    dataTypes: List<JdbcDataType<*>> = emptyList()
): JdbcDatabase {
    val dataSource = SimpleDataSource(url, user, password)
    val driver = JdbcDialects.extractJdbcDriver(url)
    val dialect = JdbcDialects.get(driver, dataTypes)
    val config = DefaultJdbcDatabaseConfig(dataSource, dialect, clockProvider, executionOptions)
    return JdbcDatabase(config)
}

/**
 * Creates a [JdbcDatabase] instance.
 *
 * @param url the JDBC URL
 * @param user the JDBC user
 * @param password the JDBC password
 * @param clockProvider the clock provider
 * @param executionOptions the execution options
 * @param dialect the dialect
 */
fun JdbcDatabase(
    url: String,
    user: String = "",
    password: String = "",
    clockProvider: ClockProvider = DefaultClockProvider(),
    executionOptions: ExecutionOptions = ExecutionOptions(),
    dialect: JdbcDialect
): JdbcDatabase {
    val dataSource = SimpleDataSource(url, user, password)
    val config = DefaultJdbcDatabaseConfig(dataSource, dialect, clockProvider, executionOptions)
    return JdbcDatabase(config)
}
