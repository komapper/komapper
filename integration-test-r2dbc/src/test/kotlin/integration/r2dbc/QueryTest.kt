package integration.r2dbc

import integration.Address
import integration.Employee
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.andThen
import org.komapper.core.dsl.query.dryRun
import org.komapper.core.dsl.query.flatMap
import org.komapper.core.dsl.query.flatZip
import org.komapper.core.dsl.query.map
import org.komapper.core.dsl.query.zip
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class QueryTest(private val db: R2dbcDatabase) {

    @Test
    fun plus() = inTransaction(db) {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        val q1 = QueryDsl.insert(a).single(address)
        val q2 = QueryDsl.insert(a).values {
            a.addressId set 17
            a.street set "STREET 17"
            a.version set 0
        }
        val q3 = QueryDsl.from(a).where { a.addressId inList listOf(16, 17) }
        val list = db.runQuery { q1.andThen(q2).andThen(q3) }.toList()
        assertEquals(2, list.size)
        println(q1.andThen(q2).andThen(q3).dryRun())
    }

    @Test
    fun map() = inTransaction(db) {
        val a = Address.meta
        val query = QueryDsl.from(a).map { it.map { address -> address.copy(version = 100) } }
        val list = db.runQuery { query }
        assertTrue(list.all { it.version == 100 })
    }

    @Test
    fun zip() = inTransaction(db) {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        val q1 = QueryDsl.insert(a).single(address)
        val q2 = QueryDsl.from(a)
        val q3 = q1.zip(q2)
        val (first, second) = db.runQuery { q3 }
        assertEquals(address, first)
        assertEquals(16, second.size)
        println(q3.dryRun())
    }

    @Test
    fun flatMap() = inTransaction(db) {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        val query = QueryDsl.insert(a).single(address).flatMap {
            val addressId = it.addressId
            val e = Employee.meta
            QueryDsl.from(e).where { e.addressId less addressId }
        }
        val list = db.runQuery { query }.toList()
        assertEquals(14, list.size)
    }

    @Test
    fun flatZip() = inTransaction(db) {
        val a = Address.meta
        val address = Address(16, "STREET 16", 0)
        val query = QueryDsl.insert(a).single(address).flatZip {
            val addressId = it.addressId
            val e = Employee.meta
            QueryDsl.from(e).where { e.addressId less addressId }
        }
        val (newAddress, flow) = db.runQuery { query }
        assertEquals(16, newAddress.addressId)
        assertEquals(14, flow.count())
    }
}
