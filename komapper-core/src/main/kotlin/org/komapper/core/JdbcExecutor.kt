package org.komapper.core

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

internal class JdbcExecutor(
    private val config: DatabaseConfig,
    jdbcOptionProvider: JdbcOptionProvider,
    private val requiresGeneratedKeys: Boolean = false
) {

    private val jdbcOption = config.jdbcOption + jdbcOptionProvider.getJdbcOption()

    fun <T> executeQuery(
        statement: Statement,
        transform: (rs: ResultSet) -> T
    ): T {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return config.session.connection.use { con ->
            log(statement)
            con.prepare(statement).use { ps ->
                ps.setUp()
                ps.bind(statement)
                ps.executeQuery().use { rs ->
                    transform(rs)
                }
            }
        }
    }

    fun <T, R> executeQuery(
        statement: Statement,
        provide: (Dialect, ResultSet) -> T,
        transform: (Sequence<T>) -> R
    ): R {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return config.session.connection.use { con ->
            log(statement)
            con.prepare(statement).use { ps ->
                ps.setUp()
                ps.bind(statement)
                ps.executeQuery().use { rs ->
                    val iterator = object : Iterator<T> {
                        var hasNext = rs.next()
                        override fun hasNext() = hasNext
                        override fun next(): T {
                            return provide(config.dialect, rs).also { hasNext = rs.next() }
                        }
                    }
                    transform(iterator.asSequence())
                }
            }
        }
    }

    fun executeUpdate(statement: Statement): Pair<Int, LongArray> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return executeWithExceptionCheck {
            config.session.connection.use { con ->
                log(statement)
                con.prepare(statement).use { ps ->
                    ps.setUp()
                    ps.bind(statement)
                    val count = ps.executeUpdate()
                    val keys = ps.fetchGeneratedKeys()
                    count to keys
                }
            }
        }
    }

    fun executeBatch(statements: List<Statement>): Pair<IntArray, LongArray> {
        require(statements.isNotEmpty())
        @Suppress("NAME_SHADOWING")
        val statements = statements.map { inspect(it) }
        return executeWithExceptionCheck {
            config.session.connection.use { con ->
                val firstStatement = statements.first()
                log(firstStatement)
                con.prepare(firstStatement).use { ps ->
                    ps.setUp()
                    val batchSize = jdbcOption.batchSize?.let { if (it > 0) it else null } ?: 10
                    val allCounts = IntArray(statements.size)
                    val allKeys = LongArray(statements.size)
                    var offset = 0
                    for ((i, statement) in statements.withIndex()) {
                        if (i > 0) {
                            log(statement)
                        }
                        ps.bind(statement)
                        ps.addBatch()
                        if (i == statements.size - 1 || batchSize > 0 && (i + 1) % batchSize == 0) {
                            val counts = ps.executeBatch()
                            val keys = ps.fetchGeneratedKeys()
                            counts.copyInto(allCounts, offset)
                            keys.copyInto(allKeys, offset)
                            offset = i + 1
                        }
                    }
                    ps.bind(firstStatement)
                    allCounts to allKeys
                }
            }
        }
    }

    fun execute(statement: Statement) {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        executeWithExceptionCheck {
            config.session.connection.use { con ->
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

    private fun inspect(statement: Statement): Statement {
        return config.statementInspector.inspect(statement)
    }

    private fun log(statement: Statement) {
        val suppressLogging = jdbcOption.suppressLogging ?: false
        if (!suppressLogging) {
            config.logger.debug(LogCategory.SQL.value) { statement.sql }
            config.logger.trace(LogCategory.SQL_WITH_ARGS.value) { statement.sqlWithArgs }
        }
    }

    private fun Connection.prepare(statement: Statement): PreparedStatement {
        return if (requiresGeneratedKeys) {
            this.prepareStatement(statement.sql, java.sql.Statement.RETURN_GENERATED_KEYS)
        } else {
            this.prepareStatement(statement.sql)
        }
    }

    private fun java.sql.Statement.setUp() {
        jdbcOption.fetchSize?.let { if (it > 0) this.fetchSize = it }
        jdbcOption.maxRows?.let { if (it > 0) this.maxRows = it }
        jdbcOption.queryTimeoutSeconds?.let { if (it > 0) this.queryTimeout = it }
    }

    private fun PreparedStatement.bind(statement: Statement) {
        statement.values.forEachIndexed { index, value ->
            config.dialect.setValue(this, index + 1, value.any, value.klass)
        }
    }

    private fun PreparedStatement.fetchGeneratedKeys(): LongArray {
        return if (requiresGeneratedKeys) {
            this.generatedKeys.use { rs ->
                val keys = mutableListOf<Long>()
                while (rs.next()) {
                    val key = rs.getLong(1)
                    keys.add(key)
                }
                keys.toLongArray()
            }
        } else {
            longArrayOf()
        }
    }
}
