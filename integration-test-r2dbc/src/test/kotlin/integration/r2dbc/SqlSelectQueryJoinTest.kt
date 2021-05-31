package integration.r2dbc

import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcSqlDsl

@ExtendWith(Env::class)
class SqlSelectQueryJoinTest(private val db: R2dbcDatabase) {

    @Test
    fun innerJoin() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val list = db.runQuery {
            R2dbcSqlDsl.from(a).innerJoin(e) {
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
            R2dbcSqlDsl.from(a).leftJoin(e) {
                a.addressId eq e.addressId
            }
        }.toList()
        assertEquals(15, list.size)
    }
}
