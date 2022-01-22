package integration.r2dbc

import integration.address
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class SelectOffsetLimitTest(private val db: R2dbcDatabase) {

    // TODO
    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun offset() = inTransaction(db) {
        val a = Meta.address
        val list = db.runQuery { QueryDsl.from(a).offset(10) }
        assertEquals(5, list.size)
    }

    // TODO
    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun limit() = inTransaction(db) {
        val a = Meta.address
        val list = db.runQuery { QueryDsl.from(a).limit(3) }
        assertEquals(3, list.size)
    }

    @Test
    fun orderBy_offset() = inTransaction(db) {
        val a = Meta.address
        val list = db.runQuery { QueryDsl.from(a).orderBy(a.addressId).offset(10) }
        assertEquals(5, list.size)
    }

    @Test
    fun orderBy_limit() = inTransaction(db) {
        val a = Meta.address
        val list = db.runQuery { QueryDsl.from(a).orderBy(a.addressId).limit(3) }
        assertEquals(3, list.size)
    }

    @Test
    fun orderBy_offset_limit() = inTransaction(db) {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a)
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
