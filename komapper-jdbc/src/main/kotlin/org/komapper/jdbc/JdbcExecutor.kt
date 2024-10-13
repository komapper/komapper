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

interface JdbcExecutor {
    fun <T> executeQuery(
        statement: Statement,
        transform: (rs: ResultSet) -> T,
    ): T

    fun <T, R> executeQuery(
        statement: Statement,
        transform: (JdbcDataOperator, ResultSet) -> T,
        collect: suspend (Flow<T>) -> R,
    ): R

    fun <T, R> executeReturning(
        statement: Statement,
        transform: (JdbcDataOperator, ResultSet) -> T,
        collect: suspend (Flow<T>) -> R,
    ): R {
        return executeQuery(statement, transform, collect)
    }

    fun executeUpdate(statement: Statement): Pair<Long, List<Long>>

    fun executeBatch(statements: List<Statement>): List<Pair<Long, Long?>>

    fun execute(statements: List<Statement>, handler: (SQLException) -> Unit = { throw it })
}

class DefaultJdbcExecutor(
    private val config: JdbcDatabaseConfig,
    executionOptionProvider: ExecutionOptionsProvider,
    private val generatedColumn: String?,
) : JdbcExecutor {
    private val executionOptions = config.executionOptions + executionOptionProvider.getExecutionOptions()

    override fun <T> executeQuery(
        statement: Statement,
        transform: (rs: ResultSet) -> T,
    ): T {
        return withExceptionTranslator {
            @Suppress("NAME_SHADOWING")
            val statement = inspect(statement)
            config.session.useConnection { con ->
                prepare(con, statement).use { ps ->
                    setUp(ps)
                    log(statement)
                    bind(ps, statement)
                    ps.executeQuery().use { rs ->
                        transform(rs)
                    }
                }
            }
        }
    }

    override fun <T, R> executeQuery(
        statement: Statement,
        transform: (JdbcDataOperator, ResultSet) -> T,
        collect: suspend (Flow<T>) -> R,
    ): R {
        return withExceptionTranslator {
            @Suppress("NAME_SHADOWING")
            val statement = inspect(statement)
            config.session.useConnection { con ->
                prepare(con, statement).use { ps ->
                    setUp(ps)
                    log(statement)
                    bind(ps, statement)
                    ps.executeQuery().use { rs ->
                        val sequence = sequence {
                            while (rs.next()) {
                                this.yield(transform(config.dataOperator, rs))
                            }
                        }
                        runBlocking {
                            collect(sequence.asFlow())
                        }
                    }
                }
            }
        }
    }

    override fun executeUpdate(statement: Statement): Pair<Long, List<Long>> {
        return withExceptionTranslator {
            @Suppress("NAME_SHADOWING")
            val statement = inspect(statement)
            config.session.useConnection { con ->
                prepare(con, statement).use { ps ->
                    setUp(ps)
                    log(statement)
                    bind(ps, statement)
                    val count = ps.executeUpdate()
                    val keys = if (generatedColumn == null) {
                        emptyList()
                    } else {
                        fetchGeneratedKeys(ps)
                    }
                    count.toLong() to keys
                }
            }
        }
    }

    override fun executeBatch(statements: List<Statement>): List<Pair<Long, Long?>> {
        require(statements.isNotEmpty())
        return withExceptionTranslator {
            @Suppress("NAME_SHADOWING")
            val statements = statements.map { inspect(it) }
            config.session.useConnection { con ->
                prepare(con, statements.first()).use { ps ->
                    setUp(ps)
                    val countAndKeyList = mutableListOf<Pair<Long, Long?>>()
                    val batchSize = executionOptions.getValidBatchSize()
                    val batchStatementsList = statements.chunked(batchSize)
                    for (batchStatements in batchStatementsList) {
                        val iterator = batchStatements.iterator()
                        while (iterator.hasNext()) {
                            val statement = iterator.next()
                            log(statement)
                            bind(ps, statement)
                            ps.addBatch()
                        }
                        val counts = ps.executeBatch()
                        val pairs = if (generatedColumn == null) {
                            counts.map { it.toLong() to null }
                        } else {
                            val keys = fetchGeneratedKeys(ps)
                            check(counts.size == keys.size) { "counts.size=${counts.size}, keys.size=${keys.size}" }
                            counts.zip(keys) { a, b -> a.toLong() to b }
                        }
                        countAndKeyList.addAll(pairs)
                    }
                    countAndKeyList
                }
            }
        }
    }

    override fun execute(statements: List<Statement>, handler: (SQLException) -> Unit) {
        withExceptionTranslator {
            @Suppress("NAME_SHADOWING")
            val statements = statements.map { inspect(it) }
            config.session.useConnection { con ->
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
     * Translates a [Exception] to a [RuntimeException].
     */
    fun <T> withExceptionTranslator(block: () -> T): T {
        return try {
            block()
        } catch (e: SQLException) {
            if (config.dialect.isUniqueConstraintViolationError(e)) {
                throw UniqueConstraintException(e)
            }
            throw JdbcException(e)
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(e)
        } catch (cause: Throwable) {
            throw cause
        }
    }

    fun inspect(statement: Statement): Statement {
        return config.statementInspector.inspect(statement)
    }

    fun log(statement: Statement) {
        val suppressLogging = executionOptions.suppressLogging ?: false
        if (!suppressLogging) {
            config.loggerFacade.sql(statement, config.dialect::createBindVariable)
            config.loggerFacade.sqlWithArgs(statement, config.dataOperator::formatValue)
        }
    }

    private fun asSql(statement: Statement): String {
        return statement.toSql(config.dialect::createBindVariable)
    }

    fun prepare(con: Connection, statement: Statement): PreparedStatement {
        val sql = asSql(statement)
        return if (generatedColumn == null) {
            con.prepareStatement(sql)
        } else {
            if (config.dialect.supportsReturnGeneratedKeysFlag()) {
                con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
            } else {
                con.prepareStatement(sql, arrayOf(generatedColumn))
            }
        }
    }

    fun setUp(statement: java.sql.Statement) {
        executionOptions.fetchSize?.let { if (it > 0) statement.fetchSize = it }
        executionOptions.maxRows?.let { if (it > 0) statement.maxRows = it }
        executionOptions.queryTimeoutSeconds?.let { if (it > 0) statement.queryTimeout = it }
    }

    fun bind(ps: PreparedStatement, statement: Statement) {
        statement.args.forEachIndexed { index, value ->
            config.dataOperator.setValue(ps, index + 1, value.any, value.type)
        }
    }

    private fun fetchGeneratedKeys(ps: PreparedStatement): List<Long> {
        return ps.generatedKeys.use { rs ->
            val keys = mutableListOf<Long>()
            while (rs.next()) {
                val key = rs.getLong(1)
                keys.add(key)
            }
            keys
        }
    }
}
