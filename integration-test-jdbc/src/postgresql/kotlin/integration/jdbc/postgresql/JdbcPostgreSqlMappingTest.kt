package integration.jdbc.postgresql

import integration.jdbc.JdbcEnv
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test

@ExtendWith(JdbcEnv::class)
class JdbcPostgreSqlMappingTest(private val db: JdbcDatabase) {
    @Test
    fun test() {
        db.runQuery {
            QueryDsl.create(Meta.jdbcPostgreSqlMapping)
        }
        db.runQuery {
            QueryDsl.from(Meta.jdbcPostgreSqlMapping)
        }
        db.runQuery {
            QueryDsl.drop(Meta.jdbcPostgreSqlMapping)
        }
    }
}
