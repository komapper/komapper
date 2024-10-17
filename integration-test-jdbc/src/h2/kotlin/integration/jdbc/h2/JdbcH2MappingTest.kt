package integration.jdbc.h2

import integration.jdbc.JdbcEnv
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test

@ExtendWith(JdbcEnv::class)
class JdbcH2MappingTest(private val db: JdbcDatabase) {
    @Test
    fun test() {
        db.runQuery {
            QueryDsl.create(Meta.jdbch2Mapping)
        }
        db.runQuery {
            QueryDsl.from(Meta.jdbch2Mapping)
        }
        db.runQuery {
            QueryDsl.drop(Meta.jdbch2Mapping)
        }
    }
}
