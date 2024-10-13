package org.komapper.dialect.oracle.jdbc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import oracle.jdbc.OraclePreparedStatement
import org.komapper.core.Statement
import org.komapper.jdbc.DefaultJdbcExecutor
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor
import java.sql.PreparedStatement
import java.sql.ResultSet

class OracleJdbcExecutor(
    private val config: JdbcDatabaseConfig,
    private val executor: DefaultJdbcExecutor,
) : JdbcExecutor by executor {
    override fun <T, R> executeReturning(
        statement: Statement,
        transform: (JdbcDataOperator, ResultSet) -> T,
        collect: suspend (Flow<T>) -> R,
    ): R {
        return executor.withExceptionTranslator {
            @Suppress("NAME_SHADOWING")
            val statement = executor.inspect(statement)
            config.session.useConnection { con ->
                executor.prepare(con, statement).use { ps ->
                    executor.setUp(ps)
                    executor.log(statement)
                    executor.bind(ps, statement)
                    register(ps, statement)
                    val count = ps.executeUpdate()
                    if (count > 0) {
                        val ops = ps.unwrap(OraclePreparedStatement::class.java)
                        ops.returnResultSet.use { rs ->
                            val sequence = sequence {
                                while (rs.next()) {
                                    this.yield(transform(config.dataOperator, rs))
                                }
                            }
                            runBlocking {
                                collect(sequence.asFlow())
                            }
                        }
                    } else {
                        runBlocking {
                            collect(emptyFlow())
                        }
                    }
                }
            }
        }
    }

    private fun register(ps: PreparedStatement, statement: Statement) {
        val base = statement.args.size
        statement.returnParamTypes.forEachIndexed { index, type ->
            config.dataOperator.registerReturnParameter<Any>(ps, base + index + 1, type)
        }
    }
}
