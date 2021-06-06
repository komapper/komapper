package integration.r2dbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class SqlSelectQueryJoinTest(private val db: R2dbcDatabase) {

    @Test
    fun innerJoin() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val list = db.runQuery {
            SqlDsl.from(a).innerJoin(e) {
                a.addressId eq e.addressId
            }
        }.toList()
        assertEquals(14, list.size)
    }

    @Test
    fun leftJoin() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val list = db.runQuery {
            SqlDsl.from(a).leftJoin(e) {
                a.addressId eq e.addressId
            }
        }.toList()
        assertEquals(15, list.size)
    }
}
