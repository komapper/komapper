package integration.jdbc.postgresql

import integration.jdbc.JdbcEnv
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(JdbcEnv::class)
class PostgreSqlDataTypeTest(private val db: JdbcDatabase) {
    @Test
    fun json() {
        val m = Meta.jsonData
        val data = JsonData(
            1,
            Json(
                """
                {"a": 100, "b": "Hello"}
                """.trimIndent(),
            ),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)

        val result = db.runQuery {
            QueryDsl.fromTemplate("select value->'b' as x from json_data")
                .select { it.get<Json>("x", typeOf<Json>())!! }
                .first()
        }
        assertEquals("\"Hello\"", result.data)
    }
}
