package org.komapper.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.R2dbcException
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
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
import java.util.Optional

internal class R2dbcExecutor(
    private val config: R2dbcDatabaseConfig,
    executionOptionsProvider: ExecutionOptionsProvider,
    private val generatedColumn: String? = null,
) {
    private val executionOptions = config.executionOptions + executionOptionsProvider.getExecutionOptions()

    fun <T> executeQuery(
        statement: Statement,
        transform: (R2dbcDataOperator, Row) -> T,
    ): Flow<T> {
        return flow<T> {
            @Suppress("NAME_SHADOWING")
            val statement = inspect(statement)
            config.session.useConnection { con ->
                val r2dbcStmt = prepare(con, statement)
                setUp(r2dbcStmt)
                log(statement)
                bind(r2dbcStmt, statement)
                computeExecutionTime(statement) {
                    r2dbcStmt.execute().collect { result ->
                        result.map { row, _ ->
                            val value = transform(config.dataOperator, row)
                            Optional.ofNullable(value)
                        }.collect {
                            val value = it.orElse(null)
                            emit(value)
                        }
                    }
                }
            }
        }.catch {
            translateException(it)
        }
    }

    suspend fun executeUpdate(statement: Statement): Pair<Long, List<Long>> {
        return flow<Pair<Long, List<Long>>> {
            @Suppress("NAME_SHADOWING")
            val statement = inspect(statement)
            config.session.useConnection { con ->
                val r2dbcStmt = prepare(con, statement)
                setUp(r2dbcStmt)
                log(statement)
                bind(r2dbcStmt, statement)
                computeExecutionTime(statement) {
                    r2dbcStmt.execute().collect { result ->
                        if (generatedColumn == null) {
                            result.rowsUpdated.collect { count: Number ->
                                emit(count.toLong() to emptyList())
                            }
                        } else {
                            val keys = fetchGeneratedKeys(result).asFlow().toList()
                            emit(keys.size.toLong() to keys)
                        }
                    }
                }
            }
        }.catch {
            translateException(it)
        }.single()
    }

    suspend fun executeBatch(statements: List<Statement>): List<Pair<Long, Long?>> {
        require(statements.isNotEmpty())
        return flow {
            @Suppress("NAME_SHADOWING")
            val statements = statements.map { inspect(it) }
            val batchSize = executionOptions.getValidBatchSize()
            val batchStatementsList = statements.chunked(batchSize)
            config.session.useConnection { con ->
                for (batchStatements in batchStatementsList) {
                    val firstStatement = batchStatements.first()
                    val r2dbcStmt = prepare(con, firstStatement)
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
                    computeExecutionTime(firstStatement) {
                        r2dbcStmt.execute().collect { result ->
                            if (generatedColumn == null) {
                                result.rowsUpdated.collect { count: Number ->
                                    emit(count.toLong() to null)
                                }
                            } else {
                                fetchGeneratedKeys(result).collect { key ->
                                    emit(1L to key)
                                }
                            }
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
            config.session.useConnection { con ->
                val batch = con.createBatch()
                for (statement in statements) {
                    log(statement)
                    val sql = asSql(statement)
                    batch.add(sql)
                }
                computeExecutionTime(statements.first()) {
                    batch.execute().collect { result ->
                        result.filter {
                            when (it) {
                                is Result.Message -> predicate(it)
                                else -> true
                            }
                        }.rowsUpdated.collect { count: Number ->
                            emit(count.toLong())
                        }
                    }
                }
            }
        }.catch {
            translateException(it)
        }.collect()
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

    private suspend fun <T> computeExecutionTime(statement: Statement, block: suspend () -> T): T {
        val statistics = config.statistics
        if (!statistics.isEnabled()) {
            return block()
        }
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()
        statistics.add(asSql(statement), startTime, endTime)
        return result
    }

    private fun inspect(statement: Statement): Statement {
        return config.statementInspector.inspect(statement)
    }

    private fun log(statement: Statement) {
        val suppressLogging = executionOptions.suppressLogging ?: false
        if (!suppressLogging) {
            config.loggerFacade.sql(statement, config.dialect::createBindVariable)
            config.loggerFacade.sqlWithArgs(statement, config.dataOperator::formatValue)
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
            config.dataOperator.setValue(r2dbcStmt, index, value.any, value.type)
        }
    }

    private fun fetchGeneratedKeys(result: Result): Publisher<Long> {
        return result.map { row, _ ->
            when (val value = row.get(0)) {
                is Number -> value.toLong()
                else -> error(
                    "Generated value is not Number. generatedColumn=$generatedColumn, value=$value, valueType=${value::class.qualifiedName}"
                )
            }
        }
    }
}
