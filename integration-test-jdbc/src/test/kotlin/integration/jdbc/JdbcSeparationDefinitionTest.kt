package integration.jdbc

import integration.core.Place
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.set
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(JdbcEnv::class)
class JdbcSeparationDefinitionTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val p = Meta.place
        val place = Place(16, "STREET 16", 0)
        db.runQuery { QueryDsl.insert(p).single(place) }
        val list = db.runQuery { QueryDsl.from(p) }
        assertEquals(16, list.size)
    }
}
