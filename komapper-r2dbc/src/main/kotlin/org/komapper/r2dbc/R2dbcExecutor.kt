package org.komapper.r2dbc

import io.r2dbc.spi.R2dbcDataIntegrityViolationException
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
        return config.session.getConnection().toFlow()
            .flatMapConcat { con ->
                val r2dbcStmt = con.prepare(statement)
                r2dbcStmt.setUp()
                r2dbcStmt.bind(statement)
                r2dbcStmt.execute().toFlow()
            }.flatMapConcat { result ->
                result.map(transform).toFlow()
            }.onStart {
                log(statement)
            }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun executeUpdate(statement: Statement): Pair<Int, LongArray> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return config.session.getConnection().toFlow()
            .flatMapConcat { con ->
                val r2dbcStmt = con.prepare(statement)
                r2dbcStmt.setUp()
                r2dbcStmt.bind(statement)
                r2dbcStmt.execute().toFlow()
            }.flatMapConcat { result ->
                result.rowsUpdated.toFlow().map { count ->
                    val generatedKeys = result.fetchGeneratedKeys().toList(mutableListOf()).toLongArray()
                    count to generatedKeys
                }
            }.onStart {
                log(statement)
            }.catch {
                handleException(it)
            }.first()
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun execute(statement: Statement) {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        return config.session.getConnection().toFlow()
            .flatMapConcat { con ->
                val batch = con.createBatch()
                for (sql in statement.sql.split(";")) {
                    batch.add(sql.trim())
                }
                batch.execute().toFlow()
            }.flatMapConcat { result ->
                result.rowsUpdated.toFlow()
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
        val suppressLogging = executionOption.suppressLogging ?: false
        if (!suppressLogging) {
            config.logger.debug(LogCategory.SQL.value) { statement.sql }
            config.logger.trace(LogCategory.SQL_WITH_ARGS.value) { statement.sqlWithArgs }
        }
    }

    private fun io.r2dbc.spi.Connection.prepare(statement: Statement): io.r2dbc.spi.Statement {
        val s = this.createStatement(statement.sql)
        if (generatedColumn != null) {
            s.returnGeneratedValues(generatedColumn)
        }
        return s
    }

    private fun io.r2dbc.spi.Statement.setUp() {
        executionOption.fetchSize?.let { if (it > 0) this.fetchSize(it) }
    }

    private fun io.r2dbc.spi.Statement.bind(statement: Statement) {
        statement.values.forEachIndexed { index, value ->
            config.dialect.setValue(this, index, value.any, value.klass)
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
            emptyFlow()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> Publisher<T>.toFlow(): Flow<T> {
        val publisher = this as Publisher<*>
        return publisher.asFlow() as Flow<T>
    }
}
