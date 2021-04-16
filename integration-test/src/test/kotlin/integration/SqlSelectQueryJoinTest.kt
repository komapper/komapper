package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class SqlSelectQueryJoinTest(private val db: Database) {

    @Test
    fun innerJoin() {
        val a = Address.alias
        val e = Employee.alias
        val list = db.execute {
            SqlQuery.from(a).innerJoin(e) {
                a.addressId eq e.addressId
            }
        }
        assertEquals(14, list.size)
    }

    @Test
    fun leftJoin() {
        val a = Address.alias
        val e = Employee.alias
        val list = db.execute {
            SqlQuery.from(a).leftJoin(e) {
                a.addressId eq e.addressId
            }
        }
        assertEquals(15, list.size)
    }
}
