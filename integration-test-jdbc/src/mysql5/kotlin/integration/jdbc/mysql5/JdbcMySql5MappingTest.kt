package integration.jdbc.mysql5

import integration.jdbc.JdbcEnv
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test

@ExtendWith(JdbcEnv::class)
class JdbcMySql5MappingTest(private val db: JdbcDatabase) {
    @Test
    fun test() {
        db.runQuery {
            QueryDsl.create(Meta.jdbcMySql5Mapping)
        }
        db.runQuery {
            QueryDsl.from(Meta.jdbcMySql5Mapping)
        }
        db.runQuery {
            QueryDsl.drop(Meta.jdbcMySql5Mapping)
        }
    }
}
