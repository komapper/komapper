package org.komapper.jdbc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.komapper.core.ExecutionOptionsProvider
import org.komapper.core.LogCategory
import org.komapper.core.Statement
import org.komapper.core.UniqueConstraintException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

internal class JdbcExecutor(
    private val config: JdbcDatabaseConfig,
    executionOptionProvider: ExecutionOptionsProvider,
    private val requiresGeneratedKeys: Boolean = false
) {

    private val executionOptions = config.executionOptions + executionOptionProvider.getExecutionOptions()

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
        transform: (JdbcDialect, ResultSet) -> T,
        collect: suspend (Flow<T>) -> R
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
                            return transform(config.dialect, rs).also { hasNext = rs.next() }
                        }
                    }
                    runBlocking {
                        collect(iterator.asFlow())
                    }
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
                    val batchSize = executionOptions.batchSize?.let { if (it > 0) it else null } ?: 10
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
                    s.execute(statement.toSql())
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
        val suppressLogging = executionOptions.suppressLogging ?: false
        if (!suppressLogging) {
            config.logger.debug(LogCategory.SQL.value) {
                statement.toSql()
            }
            config.logger.trace(LogCategory.SQL_WITH_ARGS.value) {
                statement.toSqlWithArgs(config.dialect::formatValue)
            }
        }
    }

    private fun Connection.prepare(statement: Statement): PreparedStatement {
        val sql = statement.toSql()
        return if (requiresGeneratedKeys) {
            this.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
        } else {
            this.prepareStatement(sql)
        }
    }

    private fun java.sql.Statement.setUp() {
        executionOptions.fetchSize?.let { if (it > 0) this.fetchSize = it }
        executionOptions.maxRows?.let { if (it > 0) this.maxRows = it }
        executionOptions.queryTimeoutSeconds?.let { if (it > 0) this.queryTimeout = it }
    }

    private fun PreparedStatement.bind(statement: Statement) {
        statement.args.forEachIndexed { index, value ->
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
