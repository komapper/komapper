package org.komapper.jdbc

import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.jdbc.dsl.runner.JdbcRunner
import org.komapper.jdbc.dsl.visitor.JdbcQueryVisitor
import javax.sql.DataSource

/**
 * Represents a database accessed by JDBC.
 */
interface JdbcDatabase : Jdbc {

    companion object {
        /**
         * Creates a [JdbcDatabase] instance.
         * @param config the database configuration
         */
        fun create(config: JdbcDatabaseConfig): JdbcDatabase {
            return JdbcDatabaseImpl(config)
        }

        /**
         * Creates a [JdbcDatabase] instance.
         * @param dataSource the JDBC data source
         * @param dialect the dialect
         */
        fun create(
            dataSource: DataSource,
            dialect: JdbcDialect,
        ): JdbcDatabase {
            return create(DefaultJdbcDatabaseConfig(dataSource, dialect))
        }

        /**
         * Creates a [JdbcDatabase] instance.
         * @param url the JDBC URL
         * @param user the JDBC user
         * @param password the JDBC password
         */
        fun create(
            url: String,
            user: String = "",
            password: String = "",
            dataTypes: List<JdbcDataType<*>> = emptyList()
        ): JdbcDatabase {
            return create(DefaultJdbcDatabaseConfig(url, user, password, dataTypes))
        }

        /**
         * Creates a [JdbcDatabase] instance.
         * @param url the JDBC URL
         * @param user the JDBC user
         * @param password the JDBC password
         * @param dialect the dialect
         */
        fun create(
            url: String,
            user: String = "",
            password: String = "",
            dialect: JdbcDialect
        ): JdbcDatabase {
            return create(DefaultJdbcDatabaseConfig(url, user, password, dialect))
        }
    }

    /**
     * The database configuration.
     */
    val config: JdbcDatabaseConfig

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
}

internal class JdbcDatabaseImpl(
    override val config: JdbcDatabaseConfig
) : JdbcDatabase {
    override val dataFactory: JdbcDataFactory
        get() = config.dataFactory
}
