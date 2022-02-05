package integration.r2dbc.postgresql

import integration.r2dbc.Env
import integration.r2dbc.inTransaction
import io.r2dbc.postgresql.codec.Json
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class PostgreSqlDataTypeTest(val db: R2dbcDatabase) {

    @Test
    fun json() = inTransaction(db) {
        val m = Meta.jsonTest
        val data = JsonTest(
            1,
            Json.of(
                """
            {"a": 100, "b": "Hello"}
                """.trimIndent()
            )
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value.asString(), data2.value.asString())

        val result = db.runQuery {
            QueryDsl.fromTemplate("select value->'b' as x from json_test")
                .select { it.asT("x", Json::class)!! }
                .first()
        }
        assertEquals("\"Hello\"", result.asString())
    }
}
