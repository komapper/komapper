package org.komapper.r2dbc

import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.komapper.core.ExecutionOptionsProvider
import org.komapper.core.Statement
import org.komapper.core.UniqueConstraintException

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
        return config.session.connection.asFlow().flatMapConcat { con ->
            con.use {
                val r2dbcStmt = prepare(con, statement)
                setUp(r2dbcStmt)
                log(statement)
                bind(r2dbcStmt, statement)
                r2dbcStmt.execute().asFlow().flatMapConcat { result ->
                    result.map { row, _ ->
                        transform(config.dialect, row) ?: Null
                    }.asFlow().map {
                        val nullable = if (it is Null) null else it
                        @Suppress("UNCHECKED_CAST")
                        nullable as T
                    }
                }
            }
        }.catch {
            translateThrowable(it)
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun executeUpdate(statement: Statement): Pair<Int, List<Long>> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return config.session.connection.asFlow().flatMapConcat { con ->
            val r2dbcStmt = prepare(con, statement)
            setUp(r2dbcStmt)
            log(statement)
            bind(r2dbcStmt, statement)
            r2dbcStmt.execute().asFlow().map { result ->
                if (generatedColumn == null) {
                    val count = result.rowsUpdated.asFlow().single()
                    count to emptyList()
                } else {
                    val generatedKeys = fetchGeneratedKeys(result)
                    generatedKeys.size to generatedKeys
                }
            }
        }.catch {
            translateThrowable(it)
        }.single()
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun executeBatch(statements: List<Statement>): List<Pair<Int, Long?>> {
        require(statements.isNotEmpty())
        @Suppress("NAME_SHADOWING")
        val statements = statements.map { inspect(it) }
        val batchSize = executionOptions.batchSize?.let { if (it > 0) it else null } ?: 10
        val batchStatementsList = statements.chunked(batchSize)
        return config.session.connection.asFlow().flatMapConcat { con ->
            val batchResults = batchStatementsList.map { batchStatements ->
                val iterator = batchStatements.iterator()
                val first = iterator.next()
                val r2dbcStmt = prepare(con, first)
                setUp(r2dbcStmt)
                log(first)
                bind(r2dbcStmt, first)
                if (iterator.hasNext()) {
                    r2dbcStmt.add()
                }
                while (iterator.hasNext()) {
                    val statement = iterator.next()
                    log(statement)
                    bind(r2dbcStmt, statement)
                    if (iterator.hasNext()) {
                        r2dbcStmt.add()
                    }
                }
                r2dbcStmt.execute().asFlow().map { result ->
                    if (generatedColumn == null) {
                        val counts = result.rowsUpdated.asFlow().toList()
                        counts.map { it to null }
                    } else {
                        val generatedKeys = fetchGeneratedKeys(result)
                        generatedKeys.map { 1 to it }
                    }
                }
            }
            flow {
                for (batchResult in batchResults) {
                    batchResult.collect { countAndKeyPairs ->
                        for (countAndKey in countAndKeyPairs) {
                            emit(countAndKey)
                        }
                    }
                }
            }
        }.catch {
            translateThrowable(it)
        }.toList()
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun execute(statements: List<Statement>, predicate: (Result.Message) -> Boolean = { true }) {
        @Suppress("NAME_SHADOWING")
        val statements = statements.map { inspect(it) }
        config.session.connection.asFlow().flatMapConcat { con ->
            val batch = con.createBatch()
            for (statement in statements) {
                log(statement)
                val sql = asSql(statement)
                batch.add(sql)
            }
            batch.execute().asFlow().flatMapConcat { result ->
                result.filter {
                    when (it) {
                        is Result.Message -> predicate(it)
                        else -> true
                    }
                }.rowsUpdated.asFlow()
            }
        }.catch {
            translateThrowable(it)
        }.collect()
    }

    /**
     * Translates a [Throwable] to a [RuntimeException].
     */
    private fun translateThrowable(cause: Throwable) {
        when (cause) {
            is R2dbcDataIntegrityViolationException -> throw UniqueConstraintException(cause)
            is RuntimeException -> throw cause
            else -> throw RuntimeException(cause)
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

    private suspend fun fetchGeneratedKeys(result: Result): List<Long> {
        return result.map { row, _ ->
            when (val value = row.get(0)) {
                is Number -> value.toLong()
                else -> error("Generated value is not Number. generatedColumn=$generatedColumn, value=$value, valueType=${value::class.qualifiedName}")
            }
        }.asFlow().toList()
    }
}

private fun <T> io.r2dbc.spi.Closeable.use(block: (io.r2dbc.spi.Closeable) -> Flow<T>): Flow<T> {
    return runCatching {
        block(this)
    }.onSuccess { flow ->
        flow.onCompletion { close() }
    }.onFailure {
        close()
    }.getOrThrow()
}

private object Null
