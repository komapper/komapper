package org.komapper.r2dbc

import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
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
import org.komapper.core.ExecutionOptionProvider
import org.komapper.core.LogCategory
import org.komapper.core.Statement
import org.komapper.core.UniqueConstraintException
import org.reactivestreams.Publisher

// TODO connection closing
class R2dbcExecutor(
    private val config: R2dbcDatabaseConfig,
    executionOptionProvider: ExecutionOptionProvider,
    private val generatedColumn: String? = null
) {

    private val executionOption = config.executionOption + executionOptionProvider.getExecutionOption()

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    fun <T> executeQuery(
        statement: Statement,
        transform: (row: Row, metadata: RowMetadata) -> T
    ): Flow<T> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return config.session.getConnection().toFlow().flatMapConcat { con ->
            val r2dbcStmt = con.prepare(statement)
            r2dbcStmt.setUp()
            r2dbcStmt.bind(statement)
            r2dbcStmt.execute().toFlow().flatMapConcat { result ->
                result.map(transform).toFlow()
            }.onCompletion {
                con.close()
            }
        }.onStart {
            log(statement)
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun executeUpdate(statement: Statement): Pair<Int, LongArray> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return config.session.getConnection().toFlow().flatMapConcat { con ->
            val r2dbcStmt = con.prepare(statement)
            r2dbcStmt.setUp()
            r2dbcStmt.bind(statement)
            r2dbcStmt.execute().toFlow().flatMapConcat { result ->
                // TODO
                val generatedKeys = result.fetchGeneratedKeys().toList().toLongArray()
                result.rowsUpdated.toFlow().map { count ->
                    count to generatedKeys
                }
            }.onCompletion {
                con.close()
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
        return config.session.getConnection().toFlow().flatMapConcat { con ->
            val batch = con.createBatch()
            for (sql in statement.toString().split(";")) {
                batch.add(sql.trim())
            }
            batch.execute().toFlow().flatMapConcat { result ->
                result.rowsUpdated.toFlow()
            }.onCompletion {
                con.close()
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
        val sql = replacePlaceHolders(statement.sql)

        @Suppress("NAME_SHADOWING")
        val statement = Statement(sql, statement.values, statement.sqlWithArgs)
        return config.statementInspector.inspect(statement)
    }

    private fun replacePlaceHolders(sql: List<CharSequence>): List<CharSequence> {
        val bindMarker = config.dialect.getBindMarker()
        return bindMarker.apply(sql)
    }

    private fun log(statement: Statement) {
        val suppressLogging = executionOption.suppressLogging ?: false
        if (!suppressLogging) {
            config.logger.debug(LogCategory.SQL.value) { statement.toString() }
            config.logger.trace(LogCategory.SQL_WITH_ARGS.value) { statement.sqlWithArgs }
        }
    }

    private fun io.r2dbc.spi.Connection.prepare(statement: Statement): io.r2dbc.spi.Statement {
        val r2dbcStmt = this.createStatement(statement.toString())
        return if (generatedColumn != null) {
            r2dbcStmt.returnGeneratedValues(generatedColumn)
        } else {
            r2dbcStmt
        }
    }

    private fun io.r2dbc.spi.Statement.setUp() {
        executionOption.fetchSize?.let { if (it > 0) this.fetchSize(it) }
    }

    private fun io.r2dbc.spi.Statement.bind(statement: Statement) {
        statement.values.forEachIndexed { index, value ->
            val bindMarker = config.dialect.getBindMarker()
            val dataType = config.dialect.getDataType(value.klass) as R2dbcDataType<Any>
            bindMarker.setValue(this, index, value.any, dataType)
        }
    }

    private fun Result.fetchGeneratedKeys(): Flow<Long> {
        return if (generatedColumn != null) {
            this.map { row, _ ->
                // TODO
                when (val value = row.get(0)) {
                    is Number -> value.toLong()
                    else -> error("illegal value: $value")
                }
            }.toFlow()
        } else {
            flowOf()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> Publisher<T>.toFlow(): Flow<T> {
        val publisher = this as Publisher<*>
        return publisher.asFlow() as Flow<T>
    }
}
