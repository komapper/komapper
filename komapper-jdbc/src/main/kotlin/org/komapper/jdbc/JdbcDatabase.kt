package org.komapper.jdbc

import org.komapper.core.Table
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.jdbc.dsl.query.MetadataQuery
import org.komapper.jdbc.dsl.runner.JdbcQueryRunner
import org.komapper.jdbc.dsl.visitor.JdbcQueryVisitor
import javax.sql.DataSource

/**
 * A database.
 */
@ThreadSafe
interface JdbcDatabase {

    companion object {
        /**
         * @param config the database configuration
         */
        fun create(config: JdbcDatabaseConfig): JdbcDatabase {
            return JdbcDatabaseImpl(config)
        }

        fun create(
            dataSource: DataSource,
            dialect: JdbcDialect,
        ): JdbcDatabase {
            return create(DefaultJdbcDatabaseConfig(dataSource, dialect))
        }

        fun create(
            url: String,
            user: String = "",
            password: String = "",
            dataTypes: List<JdbcDataType<*>> = emptyList()
        ): JdbcDatabase {
            return create(DefaultJdbcDatabaseConfig(url, user, password, dataTypes))
        }

        fun create(
            url: String,
            user: String = "",
            password: String = "",
            dialect: JdbcDialect
        ): JdbcDatabase {
            return create(DefaultJdbcDatabaseConfig(url, user, password, dialect))
        }
    }

    val config: JdbcDatabaseConfig
    val dataFactory: JdbcDataFactory

    @Suppress("UNCHECKED_CAST")
    fun <T> runQuery(block: QueryScope.() -> Query<T>): T {
        val query = block(QueryScope)
        val runner = query.accept(JdbcQueryVisitor()) as JdbcQueryRunner<T>
        return runner.run(config)
    }

    fun runMetadataQuery(block: QueryScope.() -> MetadataQuery): List<Table> {
        val query = block(QueryScope)
        return query.run(config)
    }
}

internal class JdbcDatabaseImpl(
    override val config: JdbcDatabaseConfig
) : JdbcDatabase {
    override val dataFactory: JdbcDataFactory
        get() = config.dataFactory
}
