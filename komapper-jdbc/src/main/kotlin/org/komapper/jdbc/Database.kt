package org.komapper.jdbc

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.query.QueryScope
import org.komapper.jdbc.dsl.runner.JdbcQueryRunner
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

    fun <T> runQuery(block: QueryScope.() -> org.komapper.core.dsl.query.Query<T>): T {
        val runner = getQueryRunner(block)
        return runner.run(config)
    }

    fun <T> dryRunQuery(block: QueryScope.() -> org.komapper.core.dsl.query.Query<T>): String {
        val runner = getQueryRunner(block)
        return runner.dryRun(config)
    }

    fun <T> getQueryRunner(block: QueryScope.() -> org.komapper.core.dsl.query.Query<T>): JdbcQueryRunner<T> {
        val query = block(QueryScope)
        return query.accept(JdbcQueryVisitor()) as? JdbcQueryRunner<T> ?: TODO()
    }

}

internal class DatabaseImpl(
    override val config: DatabaseConfig
) : Database {
    override val dataFactory: DataFactory
        get() = config.dataFactory
}
