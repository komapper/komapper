package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class SqlSelectQueryOffsetLimitTest(private val db: Database) {

    @Test
    fun offset() {
        val a = Address.meta
        val list = db.runQuery { SqlDsl.from(a).offset(10) }
        assertEquals(5, list.size)
    }

    @Test
    fun limit() {
        val a = Address.meta
        val list = db.runQuery { SqlDsl.from(a).limit(3) }
        assertEquals(3, list.size)
    }

    @Test
    fun offset_limit() {
        val a = Address.meta
        val list = db.runQuery {
            SqlDsl.from(a)
                .orderBy(a.addressId)
                .offset(10)
                .limit(3)
        }
        assertEquals(3, list.size)
        assertEquals(11, list[0].addressId)
        assertEquals(12, list[1].addressId)
        assertEquals(13, list[2].addressId)
    }
}
