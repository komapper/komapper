package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.jdbc.Database
import org.komapper.jdbc.dsl.SqlDsl

@ExtendWith(Env::class)
class SqlSelectQueryJoinTest(private val db: Database) {

    @Test
    fun innerJoin() {
        val a = Address.meta
        val e = Employee.meta
        val list = db.runQuery {
            SqlDsl.from(a).innerJoin(e) {
                a.addressId eq e.addressId
            }
        }
        assertEquals(14, list.size)
    }

    @Test
    fun leftJoin() {
        val a = Address.meta
        val e = Employee.meta
        val list = db.runQuery {
            SqlDsl.from(a).leftJoin(e) {
                a.addressId eq e.addressId
            }
        }
        assertEquals(15, list.size)
    }
}
