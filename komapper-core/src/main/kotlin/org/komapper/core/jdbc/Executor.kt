package org.komapper.core.jdbc

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.UniqueConstraintException
import org.komapper.core.data.Statement
import org.komapper.core.data.Value
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

internal class Executor(
    private val config: DefaultDatabaseConfig,
    private val prepare: (Connection, String) -> PreparedStatement = { con, sql ->
        con.prepareStatement(sql)
    }
) {

    fun <T> executeQuery(
        statement: Statement,
        handler: (rs: ResultSet) -> List<T>
    ): List<T> {
        config.connection.use { con ->
            log(statement)
            prepare(con, statement.sql).use { ps ->
                ps.setUp()
                ps.bind(statement.values)
                ps.executeQuery().use { rs ->
                    return handler(rs)
                }
            }
        }
    }

    fun <T, R> executeQuery(
        statement: Statement,
        provider: (ResultSet) -> T,
        transformer: (Sequence<T>) -> R
    ): R {
        config.connection.use { con ->
            log(statement)
            prepare(con, statement.sql).use { ps ->
                ps.setUp()
                ps.bind(statement.values)
                ps.executeQuery().use { rs ->
                    val iterator = object : Iterator<T> {
                        var hasNext = rs.next()
                        override fun hasNext(): Boolean {
                            return hasNext
                        }

                        override fun next(): T {
                            return provider(rs).also { hasNext = rs.next() }
                        }
                    }
                    return transformer(iterator.asSequence())
                }
            }
        }
    }

    fun <T> executeUpdate(statement: Statement, block: (PreparedStatement, Int) -> T): T {
        return tryExecute {
            config.connection.use { con ->
                log(statement)
                prepare(con, statement.sql).use { ps ->
                    ps.setUp()
                    ps.bind(statement.values)
                    block(ps, ps.executeUpdate())
                }
            }
        }
    }

    fun execute(statement: Statement) {
        tryExecute {
            config.connection.use { con ->
                log(statement)
                con.createStatement().use { s ->
                    s.setUp()
                    s.execute(statement.sql)
                }
            }
        }
    }

    private fun <T> tryExecute(block: () -> T): T {
        return try {
            block()
        } catch (e: SQLException) {
            if (config.dialect.isUniqueConstraintViolation(e)) {
                throw UniqueConstraintException(e)
            } else {
                throw e
            }
        }
    }

    private fun log(statement: Statement) = config.logger.logStatement(statement)

    private fun java.sql.Statement.setUp() {
        config.fetchSize?.let { if (it > 0) this.fetchSize = it }
        config.maxRows?.let { if (it > 0) this.maxRows = it }
        config.queryTimeoutSeconds?.let { if (it > 0) this.queryTimeout = it }
    }

    private fun PreparedStatement.bind(values: List<Value>) {
        values.forEachIndexed { index, value ->
            config.dialect.setValue(this, index + 1, value.any, value.klass)
        }
    }
}
