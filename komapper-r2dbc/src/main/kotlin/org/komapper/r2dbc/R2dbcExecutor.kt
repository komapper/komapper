package org.komapper.r2dbc

import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import org.komapper.core.ExecutionOptionProvider
import org.komapper.core.LogCategory
import org.komapper.core.Statement
import org.reactivestreams.Publisher

class R2dbcExecutor(
    private val config: R2dbcDatabaseConfig,
    executionOptionProvider: ExecutionOptionProvider,
    private val generatedColumn: String? = null
) {

    private val executionOption = config.executionOption + executionOptionProvider.getExecutionOption()

    // TODO error handling
    // TODO connection closing
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    suspend fun <T> executeQuery(
        statement: Statement,
        transform: (row: Row, metadata: RowMetadata) -> T
    ): Flow<T> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        val con = config.session.getConnection()
        val r2dbcStmt = con.prepare(statement)
        r2dbcStmt.setUp()
        r2dbcStmt.bind(statement)
        return r2dbcStmt.execute().toFlow()
            .flatMapConcat { result ->
                result.map(transform).toFlow()
            }.onStart {
                log(statement)
            }
            .onCompletion {
                con.close().awaitFirstOrNull()
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> Publisher<T>.toFlow(): Flow<T> {
        val publisher = this as Publisher<*>
        return publisher.asFlow() as Flow<T>
    }

    // TODO
    suspend fun executeUpdate(statement: Statement): Pair<Int, LongArray> {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        val con = config.session.getConnection()
        log(statement)
        val r2dbcStmt = con.prepare(statement)
        r2dbcStmt.setUp()
        r2dbcStmt.bind(statement)
        val result = r2dbcStmt.execute().awaitFirst()
        val count = result.rowsUpdated.awaitFirst()
        val generatedKeys = result.fetchGeneratedKeys()
        return count to generatedKeys
    }

    suspend fun execute(statement: Statement) {
        @Suppress("NAME_SHADOWING")
        val statement = inspect(statement)
        val con = config.session.getConnection()
        log(statement)
        val batch = con.createBatch()
        for (sql in statement.sql.split(";")) {
            batch.add(sql.trim())
        }
        batch.execute().awaitLast()
    }

    //    private fun <T> executeWithExceptionCheck(block: () -> T): T {
//        return try {
//            block()
//        } catch (e: SQLException) {
//            if (config.dialect.isUniqueConstraintViolation(e)) {
//                throw UniqueConstraintException(e)
//            } else {
//                throw e
//            }
//        }
//    }
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

    private suspend fun Result.fetchGeneratedKeys(): LongArray {
        return if (generatedColumn != null) {
            val keys = this.map { row, _ ->
                // TODO
                when (val value = row.get(0)) {
                    is Number -> value.toLong()
                    else -> error("illegal value: $value")
                }
            }.toFlow()
            keys.toList(mutableListOf()).toLongArray()
        } else {
            longArrayOf()
        }
    }
}
