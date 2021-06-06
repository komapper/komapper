package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.SqlDsl
import org.komapper.jdbc.Database

@ExtendWith(Env::class)
class QueryTest(private val db: Database) {

    @Test
    fun plus() {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        val q1 = EntityDsl.insert(a).single(address)
        val q2 = SqlDsl.insert(a).values {
            a.addressId set 17
            a.street set "STREET 17"
            a.version set 0
        }
        val q3 = EntityDsl.from(a).where { a.addressId inList listOf(16, 17) }
        val list = db.runQuery { q1 + q2 + q3 }
        assertEquals(2, list.size)
        println(db.dryRunQuery { q1 + q2 + q3 })
    }

    @Test
    fun flatMap() {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        val query = EntityDsl.insert(a).single(address).flatMap {
            val addressId = it.addressId
            val e = Employee.meta
            EntityDsl.from(e).where { e.addressId less addressId }
        }
        val list = db.runQuery { query }
        assertEquals(14, list.size)
    }

    @Test
    fun flatZip() {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        val query = EntityDsl.insert(a).single(address).flatZip {
            val addressId = it.addressId
            val e = Employee.meta
            EntityDsl.from(e).where { e.addressId less addressId }
        }
        val (newAddress, list) = db.runQuery { query }
        assertEquals(16, newAddress.addressId)
        assertEquals(14, list.size)
    }
}
