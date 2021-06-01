package integration.r2dbc

import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcEntityDsl

@ExtendWith(Env::class)
class EntitySelectQueryJoinTest(private val db: R2dbcDatabase) {

    @Test
    fun innerJoin() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val list = db.runQuery {
            R2dbcEntityDsl.from(a).innerJoin(e) {
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
            R2dbcEntityDsl.from(a).leftJoin(e) {
                a.addressId eq e.addressId
            }
        }.toList()
        assertEquals(15, list.size)
    }

    @Test
    fun innerJoin_multiConditions() = inTransaction(db) {
        val employee = Employee.meta
        val manager = Employee.newMeta()
        val list = db.runQuery {
            R2dbcEntityDsl.from(employee).innerJoin(manager) {
                employee.managerId eq manager.employeeId
                manager.managerId.isNull()
            }
        }.toList()
        println(list)
        assertEquals(3, list.size)
    }
}
