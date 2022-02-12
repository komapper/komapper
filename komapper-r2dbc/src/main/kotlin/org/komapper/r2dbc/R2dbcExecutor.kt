package org.komapper.r2dbc

import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
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
                log(statement)
                bind(r2dbcStmt, statement)
                val result = r2dbcStmt.execute().awaitSingle()
                result.map { row, _ ->
                    transform(config.dialect, row) ?: Null
                }.asFlow().map {
                    val nullable = if (it is Null) null else it
                    @Suppress("UNCHECKED_CAST")
                    nullable as T
                }
            }
        }.catch {
            translateThrowable(it)
        }
    }

    suspend fun executeUpdate(statement: Statement): Pair<Int, List<Long>> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return withThrowableTranslator {
            config.session.connection.awaitSingle().use { con ->
                val r2dbcStmt = prepare(con, statement)
                setUp(r2dbcStmt)
                log(statement)
                bind(r2dbcStmt, statement)
                val result = r2dbcStmt.execute().awaitSingle()
                if (generatedColumn == null) {
                    val count = result.rowsUpdated.awaitSingle()
                    count to emptyList()
                } else {
                    val generatedKeys = fetchGeneratedKeys(result)
                    generatedKeys.size to generatedKeys
                }
            }
        }
    }

    suspend fun executeBatch(
        statements: List<Statement>,
        customizeBatchCount: (Int) -> Int = { it }
    ): List<Pair<Int, Long?>> {
        require(statements.isNotEmpty())
        @Suppress("NAME_SHADOWING")
        val statements = statements.map { inspect(it) }
        val batchSize = executionOptions.batchSize?.let { if (it > 0) it else null } ?: 10
        val batchStatementsList = statements.chunked(batchSize)
        return withThrowableTranslator {
            val countAndKeyList = mutableListOf<Pair<Int, Long?>>()
            config.session.connection.awaitSingle().use { con ->
                for (batchStatements in batchStatementsList) {
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
                    for (result in r2dbcStmt.execute().asFlow().toList()) {
                        val pairs: List<Pair<Int, Long?>> = if (generatedColumn == null) {
                            val counts = result.rowsUpdated.asFlow().toList().map(customizeBatchCount)
                            counts.map { it to null }
                        } else {
                            val generatedKeys = fetchGeneratedKeys(result)
                            generatedKeys.map { 1 to it }
                        }
                        countAndKeyList.addAll(pairs)
                    }
                }
            }
            countAndKeyList
        }
    }

    suspend fun execute(statements: List<Statement>, predicate: (Result.Message) -> Boolean = { true }) {
        @Suppress("NAME_SHADOWING")
        val statements = statements.map { inspect(it) }
        return withThrowableTranslator {
            config.session.connection.awaitSingle().use { con ->
                val batch = con.createBatch()
                for (statement in statements) {
                    log(statement)
                    val sql = asSql(statement)
                    batch.add(sql)
                }
                batch.execute().asFlow().collect { result ->
                    result.filter {
                        when (it) {
                            is Result.Message -> predicate(it)
                            else -> true
                        }
                    }.rowsUpdated.asFlow().collect()
                }
            }
        }
    }

    private suspend fun <T> withThrowableTranslator(block: suspend () -> T): T {
        return runCatching {
            block()
        }.onFailure {
            translateThrowable(it)
        }.getOrThrow()
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

private suspend fun <T : io.r2dbc.spi.Closeable, R> T.use(block: suspend (T) -> R): R {
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
