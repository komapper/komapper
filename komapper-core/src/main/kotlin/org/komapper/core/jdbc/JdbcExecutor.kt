package org.komapper.core.jdbc

import org.komapper.core.DatabaseConfig
import org.komapper.core.UniqueConstraintException
import org.komapper.core.config.Dialect
import org.komapper.core.config.JdbcOption
import org.komapper.core.data.Statement
import org.komapper.core.data.Value
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

internal class JdbcExecutor(
    private val config: DatabaseConfig,
    jdbcOption: JdbcOption,
    private val prepare: (Connection, String) -> PreparedStatement = { con, sql ->
        con.prepareStatement(sql)
    }
) {
    private val jdbcOption = config.jdbcOption + jdbcOption

    fun <T> executeQuery(
        statement: Statement,
        handler: (rs: ResultSet) -> T
    ): T {
        config.session.getConnection().use { con ->
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
        provider: (Dialect, ResultSet) -> T,
        transformer: (Sequence<T>) -> R
    ): R {
        config.session.getConnection().use { con ->
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
                            return provider(config.dialect, rs).also { hasNext = rs.next() }
                        }
                    }
                    return transformer(iterator.asSequence())
                }
            }
        }
    }

    fun executeUpdate(statement: Statement): Pair<Int, LongArray> {
        return executeWithExceptionCheck {
            config.session.getConnection().use { con ->
                log(statement)
                prepare(con, statement.sql).use { ps ->
                    ps.setUp()
                    ps.bind(statement.values)
                    val count = ps.executeUpdate()
                    val keys = ps.fetchGeneratedKeys()
                    count to keys
                }
            }
        }
    }

    fun executeBatch(statements: List<Statement>): Pair<IntArray, LongArray> {
        require(statements.isNotEmpty())
        return executeWithExceptionCheck {
            config.session.getConnection().use { con ->
                val firstStatement = statements.first()
                log(firstStatement)
                prepare(con, firstStatement.sql).use { ps ->
                    ps.setUp()
                    val batchSize = jdbcOption.batchSize?.let { if (it > 0) it else null } ?: 10
                    val allCounts = IntArray(statements.size)
                    val allKeys = LongArray(statements.size)
                    var offset = 0
                    for ((i, statement) in statements.withIndex()) {
                        if (i > 0) {
                            log(statement)
                        }
                        ps.bind(statement.values)
                        ps.addBatch()
                        if (i == statements.size - 1 || batchSize > 0 && (i + 1) % batchSize == 0) {
                            val counts = ps.executeBatch()
                            val keys = ps.fetchGeneratedKeys()
                            counts.copyInto(allCounts, offset)
                            keys.copyInto(allKeys, offset)
                            offset = i + 1
                        }
                    }
                    ps.bind(firstStatement.values)
                    allCounts to allKeys
                }
            }
        }
    }

    fun execute(statement: Statement) {
        executeWithExceptionCheck {
            config.session.getConnection().use { con ->
                log(statement)
                con.createStatement().use { s ->
                    s.setUp()
                    s.execute(statement.sql)
                }
            }
        }
    }

    private fun <T> executeWithExceptionCheck(block: () -> T): T {
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
        jdbcOption.fetchSize?.let { if (it > 0) this.fetchSize = it }
        jdbcOption.maxRows?.let { if (it > 0) this.maxRows = it }
        jdbcOption.queryTimeoutSeconds?.let { if (it > 0) this.queryTimeout = it }
    }

    private fun PreparedStatement.bind(values: List<Value>) {
        values.forEachIndexed { index, value ->
            config.dialect.setValue(this, index + 1, value.any, value.klass)
        }
    }

    private fun PreparedStatement.fetchGeneratedKeys(): LongArray {
        return this.generatedKeys.use { rs ->
            val keys = mutableListOf<Long>()
            while (rs.next()) {
                val key = rs.getLong(1)
                keys.add(key)
            }
            keys.toLongArray()
        }
    }
}
