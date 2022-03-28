package integration.jdbc.mariadb

import integration.jdbc.JdbcEnv
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase

@ExtendWith(JdbcEnv::class)
class JdbcMariaDbMappingTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        db.runQuery {
            QueryDsl.create(Meta.jdbcMariaDbMapping)
        }
        db.runQuery {
            QueryDsl.from(Meta.jdbcMariaDbMapping)
        }
        db.runQuery {
            QueryDsl.drop(Meta.jdbcMariaDbMapping)
        }
    }
}
