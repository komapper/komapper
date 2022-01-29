package integration.r2dbc

import integration.Direction
import integration.enumTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@Tag("lowPriority")
@ExtendWith(Env::class)
class R2dbcDataTypeTest(val db: R2dbcDatabase) {

    @Test
    fun enum() = inTransaction(db) {
        val m = Meta.enumTest
        val data = integration.EnumTest(1, Direction.EAST)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }
}
