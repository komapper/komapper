package integration.jdbc.oracle

import integration.jdbc.JdbcEnv
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test

@ExtendWith(JdbcEnv::class)
class JdbcOracleMappingTest(private val db: JdbcDatabase) {
    @Test
    fun test() {
        db.runQuery {
            QueryDsl.create(Meta.jdbcOracleMapping)
        }
        db.runQuery {
            QueryDsl.from(Meta.jdbcOracleMapping)
        }
        db.runQuery {
            QueryDsl.drop(Meta.jdbcOracleMapping)
        }
    }
}
