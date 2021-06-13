package integration.r2dbc

import integration.Address
import integration.Employee
import integration.meta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.query.dryRun
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class QueryTest(private val db: R2dbcDatabase) {

    @Test
    fun plus() = inTransaction(db) {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        val q1 = EntityDsl.insert(a).single(address)
        val q2 = SqlDsl.insert(a).values {
            a.addressId set 17
            a.street set "STREET 17"
            a.version set 0
        }
        val q3 = EntityDsl.from(a).where { a.addressId inList listOf(16, 17) }
        val list = db.runQuery { q1 + q2 + q3 }.toList()
        assertEquals(2, list.size)
        println((q1 + q2 + q3).dryRun())
    }

    @Test
    fun flatMap() = inTransaction(db) {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        val query = EntityDsl.insert(a).single(address).flatMap {
            val addressId = it.addressId
            val e = Employee.meta
            EntityDsl.from(e).where { e.addressId less addressId }
        }
        val list = db.runQuery { query }.toList()
        assertEquals(14, list.size)
    }

    @Test
    fun flatZip() = inTransaction(db) {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        val query = EntityDsl.insert(a).single(address).flatZip {
            val addressId = it.addressId
            val e = Employee.meta
            EntityDsl.from(e).where { e.addressId less addressId }
        }
        val (newAddress, flow) = db.runQuery { query }
        assertEquals(16, newAddress.addressId)
        assertEquals(14, flow.count())
    }
}
