package integration.r2dbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class SqlSelectQueryOffsetLimitTest(private val db: R2dbcDatabase) {

    @Test
    fun offset() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery { SqlDsl.from(a).offset(10) }.toList()
        assertEquals(5, list.size)
    }

    @Test
    fun limit() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery { SqlDsl.from(a).limit(3) }.toList()
        assertEquals(3, list.size)
    }

    @Test
    fun offset_limit() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            SqlDsl.from(a)
                .orderBy(a.addressId)
                .offset(10)
                .limit(3)
        }.toList()
        assertEquals(3, list.size)
        assertEquals(11, list[0].addressId)
        assertEquals(12, list[1].addressId)
        assertEquals(13, list[2].addressId)
    }
}
