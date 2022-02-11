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
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
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
        }.onStart {
            log(statement)
        }.catch {
            translateThrowable(it)
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun executeUpdate(statement: Statement): Pair<Int, LongArray> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return config.session.connection.asFlow().flatMapConcat { con ->
            con.use {
                val r2dbcStmt = prepare(con, statement)
                setUp(r2dbcStmt)
                bind(r2dbcStmt, statement)
                r2dbcStmt.execute().asFlow().flatMapConcat { result ->
                    if (generatedColumn == null) {
                        result.rowsUpdated.asFlow().map { count -> count to longArrayOf() }
                    } else {
                        val generatedKeys = fetchGeneratedKeys(result).toList().toLongArray()
                        flowOf(generatedKeys.size to generatedKeys)
                    }
                }
            }
        }.onStart {
            log(statement)
        }.catch {
            translateThrowable(it)
        }.single()
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun executeBatch(
        statements: List<Statement>,
        // TODO
        customizeBatchCounts: (IntArray) -> IntArray = { it }
    ): List<Pair<Int, Long?>> {
        require(statements.isNotEmpty())
        @Suppress("NAME_SHADOWING")
        val statements = statements.map { inspect(it) }
        val batchSize = executionOptions.batchSize?.let { if (it > 0) it else null } ?: 10
        val batchedStatementsList = statements.chunked(batchSize)
        return withThrowableTranslator {
            val countAndKeyList = mutableListOf<Pair<Int, Long?>>()
            config.session.connection.awaitSingle().use2 { con ->
                for (batchedStatements in batchedStatementsList) {
                    val firstStatement = batchedStatements.first()
                    val r2dbcStmt = prepare(con, firstStatement)
                    setUp(r2dbcStmt)
                    val iterator = batchedStatements.iterator()
                    while (iterator.hasNext()) {
                        val statement = iterator.next()
                        log(statement)
                        bind(r2dbcStmt, statement)
                        if (iterator.hasNext()) {
                            r2dbcStmt.add()
                        }
                    }
                    for (result in r2dbcStmt.execute().asFlow().toList()) {
                        val pairs: List<Pair<Int, Long?>> = if (generatedColumn == null) {
                            val counts = result.rowsUpdated.asFlow().toList()
                            counts.map { it to null }
                        } else {
                            val generatedKeys = fetchGeneratedKeys(result).toList()
                            generatedKeys.map { 1 to it }
                        }
                        countAndKeyList.addAll(pairs)
                    }
                }
            }
            countAndKeyList
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun execute(statements: List<Statement>, predicate: (Result.Message) -> Boolean = { true }) {
        @Suppress("NAME_SHADOWING")
        val statements = statements.map { inspect(it) }
        return config.session.connection.asFlow().flatMapConcat { con ->
            con.use {
                val batch = con.createBatch()
                for (statement in statements) {
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
            }
        }.onStart {
            statements.forEach(::log)
        }.catch {
            translateThrowable(it)
        }.collect()
    }

    private suspend fun <T> withThrowableTranslator(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: R2dbcDataIntegrityViolationException) {
            throw UniqueConstraintException(e)
        } catch (e: RuntimeException) {
            throw e
        } catch (cause: Throwable) {
            throw RuntimeException(cause)
        }
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

    private fun executeUpdate(r2dbcStmt: io.r2dbc.spi.Statement): Flow<Pair<Int, LongArray>> {
        return r2dbcStmt.execute().asFlow().flatMapConcat { result ->
            if (generatedColumn == null) {
                result.rowsUpdated.asFlow().map { count -> count to longArrayOf() }
            } else {
                val generatedKeys = fetchGeneratedKeys(result).toList().toLongArray()
                flowOf(generatedKeys.size to generatedKeys)
            }
        }
    }

    private fun fetchGeneratedKeys(result: Result): Flow<Long> {
        return result.map { row, _ ->
            when (val value = row.get(0)) {
                is Number -> value.toLong()
                else -> error("Generated value is not Number. generatedColumn=$generatedColumn, value=$value, valueType=${value::class.qualifiedName}")
            }
        }.asFlow()
    }
}

private fun <T> io.r2dbc.spi.Closeable.use(block: (io.r2dbc.spi.Closeable) -> Flow<T>): Flow<T> {
    return runCatching(block)
        .onSuccess { flow ->
            flow.onCompletion { close() }
        }.onFailure {
            close()
        }.getOrThrow()
}

// TODO
private suspend fun <T : io.r2dbc.spi.Closeable, R> T.use2(block: suspend (T) -> R): R {
    val result = runCatching {
        block(this)
    }
    runCatching {
        close().awaitFirstOrNull()
    }.onFailure {
        if (result.isSuccess) throw it
    }
    return result.getOrThrow()
}

private object Null
