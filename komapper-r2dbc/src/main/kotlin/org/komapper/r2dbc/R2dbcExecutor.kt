package org.komapper.r2dbc

import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.komapper.core.ExecutionOptionsProvider
import org.komapper.core.Statement
import org.komapper.core.UniqueConstraintException
import org.reactivestreams.Publisher

internal class R2dbcExecutor(
    private val config: R2dbcDatabaseConfig,
    executionOptionsProvider: ExecutionOptionsProvider,
    private val generatedColumn: String? = null
) {

    private val executionOptions = config.executionOptions + executionOptionsProvider.getExecutionOptions()

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    fun <T> executeQuery(
        statement: Statement,
        transform: (R2dbcDialect, Row) -> T
    ): Flow<T> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return config.session.connection.toFlow().flatMapConcat { con ->
            con.use {
                val r2dbcStmt = prepare(con, statement)
                setUp(r2dbcStmt)
                bind(r2dbcStmt, statement)
                r2dbcStmt.execute().toFlow().flatMapConcat { result ->
                    result.map { row, _ ->
                        transform(config.dialect, row)
                    }.toFlow()
                }
            }
        }.onStart {
            log(statement)
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun executeUpdate(statement: Statement): Pair<Int, LongArray> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return config.session.connection.toFlow().flatMapConcat { con ->
            con.use {
                val r2dbcStmt = prepare(con, statement)
                setUp(r2dbcStmt)
                bind(r2dbcStmt, statement)
                r2dbcStmt.execute().toFlow().flatMapConcat { result ->
                    if (generatedColumn == null) {
                        result.rowsUpdated.toFlow().map { count -> count to longArrayOf() }
                    } else {
                        val generatedKeys = fetchGeneratedKeys(result).toList().toLongArray()
                        flowOf(generatedKeys.size to generatedKeys)
                    }
                }
            }
        }.onStart {
            log(statement)
        }.catch {
            handleException(it)
        }.single()
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun execute(statement: Statement) {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return config.session.connection.toFlow().flatMapConcat { con ->
            con.use {
                asSql(statement).split(";")
                    .asSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { sql -> con.createStatement(sql).execute().toFlow() }
                    .map { flow -> flow.flatMapConcat { result -> result.rowsUpdated.toFlow() } }
                    .reduce { acc, flow -> acc.flatMapConcat { flow } }
            }
        }.onStart {
            log(statement)
        }.catch {
            handleException(it)
        }.collect()
    }

    private fun handleException(cause: Throwable) {
        if (cause is R2dbcDataIntegrityViolationException) {
            throw UniqueConstraintException(cause)
        } else {
            throw cause
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

    private fun prepare(con: io.r2dbc.spi.Connection, statement: Statement): io.r2dbc.spi.Statement {
        val sql = asSql(statement)
        val r2dbcStmt = con.createStatement(sql)
        return if (generatedColumn != null) {
            r2dbcStmt.returnGeneratedValues(generatedColumn)
        } else {
            r2dbcStmt
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

    private fun fetchGeneratedKeys(result: Result): Flow<Long> {
        return result.map { row, _ ->
            when (val value = row.get(0)) {
                is Number -> value.toLong()
                else -> error("Generated value is not Number. generatedColumn=$generatedColumn, value=$value, valueType=${value::class.qualifiedName}")
            }
        }.toFlow()
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> Publisher<T>.toFlow(): Flow<T> {
    val publisher = this as Publisher<*>
    return publisher.asFlow() as Flow<T>
}

private fun <T> io.r2dbc.spi.Closeable.use(block: (io.r2dbc.spi.Closeable) -> Flow<T>): Flow<T> {
    return runCatching(block)
        .onSuccess { flow ->
            flow.onCompletion { close() }
        }.onFailure {
            close()
        }.getOrThrow()
}
