package org.komapper.jdbc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.komapper.core.ExecutionOptionsProvider
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
        return withThrowableTranslator {
            config.session.connection.use { con ->
                log(statement)
                prepare(con, statement).use { ps ->
                    setUp(ps)
                    bind(ps, statement)
                    ps.executeQuery().use { rs ->
                        transform(rs)
                    }
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
        return withThrowableTranslator {
            config.session.connection.use { con ->
                prepare(con, statement).use { ps ->
                    setUp(ps)
                    log(statement)
                    bind(ps, statement)
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
    }

    fun executeUpdate(statement: Statement): Pair<Int, List<Long>> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return withThrowableTranslator {
            config.session.connection.use { con ->
                prepare(con, statement).use { ps ->
                    setUp(ps)
                    log(statement)
                    bind(ps, statement)
                    val count = ps.executeUpdate()
                    val keys = fetchGeneratedKeys(ps)
                    count to keys
                }
            }
        }
    }

    fun executeBatch(
        statements: List<Statement>,
        customizeBatchCount: (Int) -> Int = { it }
    ): List<Pair<Int, Long?>> {
        require(statements.isNotEmpty())
        @Suppress("NAME_SHADOWING")
        val statements = statements.map { inspect(it) }
        return withThrowableTranslator {
            config.session.connection.use { con ->
                prepare(con, statements.first()).use { ps ->
                    setUp(ps)
                    val countAndKeyList = mutableListOf<Pair<Int, Long?>>()
                    val batchSize = executionOptions.batchSize?.let { if (it > 0) it else null } ?: 10
                    val batchStatementsList = statements.chunked(batchSize)
                    for (batchStatements in batchStatementsList) {
                        val iterator = batchStatements.iterator()
                        while (iterator.hasNext()) {
                            val statement = iterator.next()
                            log(statement)
                            bind(ps, statement)
                            ps.addBatch()
                        }
                        val counts = ps.executeBatch().map(customizeBatchCount)
                        val pairs = if (requiresGeneratedKeys) {
                            val keys = fetchGeneratedKeys(ps)
                            check(counts.size == keys.size) { "counts.size=${counts.size}, keys.size=${keys.size}" }
                            counts.zip(keys)
                        } else {
                            counts.map { it to null }
                        }
                        countAndKeyList.addAll(pairs)
                    }
                    countAndKeyList
                }
            }
        }
    }

    fun execute(statements: List<Statement>, handler: (SQLException) -> Unit = { throw it }) {
        @Suppress("NAME_SHADOWING")
        val statements = statements.map { inspect(it) }
        withThrowableTranslator {
            config.session.connection.use { con ->
                for (statement in statements) {
                    con.createStatement().use { s ->
                        setUp(s)
                        log(statement)
                        val sql = asSql(statement)
                        try {
                            s.execute(sql)
                        } catch (e: SQLException) {
                            handler(e)
                        }
                    }
                }
            }
        }
    }

    /**
     * Translates a [Throwable] to a [RuntimeException].
     */
    private fun <T> withThrowableTranslator(block: () -> T): T {
        return try {
            block()
        } catch (e: SQLException) {
            if (config.dialect.isUniqueConstraintViolationError(e)) {
                throw UniqueConstraintException(e)
            }
            throw JdbcException(e)
        } catch (e: RuntimeException) {
            throw e
        } catch (cause: Throwable) {
            throw RuntimeException(cause)
        }
    }

    private fun inspect(statement: Statement): Statement {
        return config.statementInspector.inspect(statement)
    }

    private fun log(statement: Statement) {
        val suppressLogging = executionOptions.suppressLogging ?: false
        if (!suppressLogging) {
            config.loggerFacade.sql(statement, config.dialect::createBindVariable)
            config.loggerFacade.sqlWithArgs(statement, config.dialect::formatValue)
        }
    }

    private fun asSql(statement: Statement): String {
        return statement.toSql(config.dialect::createBindVariable)
    }

    private fun prepare(con: Connection, statement: Statement): PreparedStatement {
        val sql = asSql(statement)
        return if (requiresGeneratedKeys) {
            if (config.dialect.supportsReturnGeneratedKeysFlag()) {
                con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
            } else {
                con.prepareStatement(sql, intArrayOf(1))
            }
        } else {
            con.prepareStatement(sql)
        }
    }

    private fun setUp(statement: java.sql.Statement) {
        executionOptions.fetchSize?.let { if (it > 0) statement.fetchSize = it }
        executionOptions.maxRows?.let { if (it > 0) statement.maxRows = it }
        executionOptions.queryTimeoutSeconds?.let { if (it > 0) statement.queryTimeout = it }
    }

    private fun bind(ps: PreparedStatement, statement: Statement) {
        statement.args.forEachIndexed { index, value ->
            config.dialect.setValue(ps, index + 1, value.any, value.klass)
        }
    }

    private fun fetchGeneratedKeys(ps: PreparedStatement): List<Long> {
        return if (requiresGeneratedKeys) {
            ps.generatedKeys.use { rs ->
                val keys = mutableListOf<Long>()
                while (rs.next()) {
                    val key = rs.getLong(1)
                    keys.add(key)
                }
                keys
            }
        } else {
            emptyList()
        }
    }
}
