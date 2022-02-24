package org.komapper.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.R2dbcException
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.collect
import org.komapper.core.ExecutionOptionsProvider
import org.komapper.core.Statement
import org.komapper.core.UniqueConstraintException
import org.reactivestreams.Publisher
import kotlin.coroutines.coroutineContext

internal class R2dbcExecutor(
    private val config: R2dbcDatabaseConfig,
    executionOptionsProvider: ExecutionOptionsProvider,
    private val generatedColumn: String? = null
) {

    private val executionOptions = config.executionOptions + executionOptionsProvider.getExecutionOptions()

    fun <T> executeQuery(
        statement: Statement,
        transform: (R2dbcDialect, Row) -> T
    ): Flow<T> {
        return flow {
            @Suppress("NAME_SHADOWING")
            val statement = inspect(statement)
            config.session.getConnection().use { con ->
                val r2dbcStmt = prepare(con, statement)
                setUp(r2dbcStmt)
                log(statement)
                bind(r2dbcStmt, statement)
                r2dbcStmt.execute().collect { result ->
                    result.map { row, _ ->
                        transform(config.dialect, row) ?: Null
                    }.collect {
                        val nullable = if (it is Null) null else it
                        @Suppress("UNCHECKED_CAST")
                        nullable as T
                        emit(nullable)
                    }
                }
            }
        }.catch {
            translateException(it)
        }
    }

    suspend fun executeUpdate(statement: Statement): Pair<Int, List<Long>> {
        return flow<Pair<Int, List<Long>>> {
            @Suppress("NAME_SHADOWING")
            val statement = inspect(statement)
            config.session.getConnection().use { con ->
                val r2dbcStmt = prepare(con, statement)
                setUp(r2dbcStmt)
                log(statement)
                bind(r2dbcStmt, statement)
                r2dbcStmt.execute().collect { result ->
                    if (generatedColumn == null) {
                        result.rowsUpdated.collect { count -> emit(count to emptyList()) }
                    } else {
                        val keys = fetchGeneratedKeys(result).asFlow().toList()
                        emit(keys.size to keys)
                    }
                }
            }
        }.catch {
            translateException(it)
        }.single()
    }

    suspend fun executeBatch(statements: List<Statement>): List<Pair<Int, Long?>> {
        require(statements.isNotEmpty())
        return flow {
            @Suppress("NAME_SHADOWING")
            val statements = statements.map { inspect(it) }
            val batchSize = executionOptions.batchSize?.let { if (it > 0) it else null } ?: 10
            val batchStatementsList = statements.chunked(batchSize)
            config.session.getConnection().use { con ->
                for (batchStatements in batchStatementsList) {
                    val r2dbcStmt = prepare(con, batchStatements.first())
                    setUp(r2dbcStmt)
                    val iterator = batchStatements.iterator()
                    while (iterator.hasNext()) {
                        val statement = iterator.next()
                        log(statement)
                        bind(r2dbcStmt, statement)
                        if (iterator.hasNext()) {
                            r2dbcStmt.add()
                        }
                    }
                    r2dbcStmt.execute().collect { result ->
                        if (generatedColumn == null) {
                            result.rowsUpdated.collect { count -> emit(count to null) }
                        } else {
                            fetchGeneratedKeys(result).collect { key -> emit(1 to key) }
                        }
                    }
                }
            }
        }.catch {
            translateException(it)
        }.toList()
    }

    suspend fun execute(statements: List<Statement>, predicate: (Result.Message) -> Boolean = { true }) {
        flow {
            @Suppress("NAME_SHADOWING")
            val statements = statements.map { inspect(it) }
            config.session.getConnection().use { con ->
                val batch = con.createBatch()
                for (statement in statements) {
                    log(statement)
                    val sql = asSql(statement)
                    batch.add(sql)
                }
                batch.execute().collect { result ->
                    result.filter {
                        when (it) {
                            is Result.Message -> predicate(it)
                            else -> true
                        }
                    }.rowsUpdated.collect {
                        emit(it)
                    }
                }
            }
        }.catch {
            translateException(it)
        }.collect()
    }

    private suspend fun <T> Connection.use(block: suspend CoroutineScope.(Connection) -> T): T {
        val con = this
        return runCatching {
            block(CoroutineScope(coroutineContext), con)
        }.onSuccess {
            con.close()
        }.onFailure { cause ->
            runCatching {
                con.close()
            }.onFailure {
                cause.addSuppressed(cause)
            }
        }.getOrThrow()
    }

    /**
     * Translates a [Exception] to a [RuntimeException].
     */
    private fun translateException(cause: Throwable): Nothing {
        when (cause) {
            is R2dbcException -> {
                if (config.dialect.isUniqueConstraintViolationError(cause)) {
                    throw UniqueConstraintException(cause)
                } else {
                    throw cause
                }
            }
            is RuntimeException -> throw cause
            is Exception -> throw RuntimeException(cause)
            else -> throw cause
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

    private fun prepare(con: Connection, statement: Statement): io.r2dbc.spi.Statement {
        val sql = asSql(statement)
        val r2dbcStmt = con.createStatement(sql)
        return if (generatedColumn == null) {
            r2dbcStmt
        } else {
            r2dbcStmt.returnGeneratedValues(generatedColumn)
        }
    }

    private fun setUp(r2dbcStmt: io.r2dbc.spi.Statement) {
        executionOptions.fetchSize?.let { if (it > 0) r2dbcStmt.fetchSize(it) }
    }

    private fun bind(r2dbcStmt: io.r2dbc.spi.Statement, statement: Statement) {
        statement.args.forEachIndexed { index, value ->
            config.dialect.setValue(r2dbcStmt, index, value.any, value.klass)
        }
    }

    private fun fetchGeneratedKeys(result: Result): Publisher<Long> {
        return result.map { row, _ ->
            when (val value = row.get(0)) {
                is Number -> value.toLong()
                else -> error("Generated value is not Number. generatedColumn=$generatedColumn, value=$value, valueType=${value::class.qualifiedName}")
            }
        }
    }
}

private object Null
