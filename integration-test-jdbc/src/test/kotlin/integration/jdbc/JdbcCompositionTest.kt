package integration.jdbc

import integration.core.Address
import integration.core.address
import integration.core.department
import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.andThen
import org.komapper.core.dsl.query.dryRun
import org.komapper.core.dsl.query.flatMap
import org.komapper.core.dsl.query.flatZip
import org.komapper.core.dsl.query.groupBy
import org.komapper.core.dsl.query.having
import org.komapper.core.dsl.query.join
import org.komapper.core.dsl.query.map
import org.komapper.core.dsl.query.on
import org.komapper.core.dsl.query.orderBy
import org.komapper.core.dsl.query.set
import org.komapper.core.dsl.query.values
import org.komapper.core.dsl.query.where
import org.komapper.core.dsl.query.zip
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcCompositionTest(private val db: JdbcDatabase) {

    @Test
    fun plus() {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        val q1 = QueryDsl.insert(a).single(address)
        val q2 = QueryDsl.insert(a).values {
            a.addressId eq 17
            a.street eq "STREET 17"
            a.version eq 0
        }
        val q3 = QueryDsl.from(a).where { a.addressId inList listOf(16, 17) }
        val list = db.runQuery(q1.andThen(q2).andThen(q3))
        assertEquals(2, list.size)
        println(q1.andThen(q2).andThen(q3).dryRun())
    }

    @Test
    fun map() {
        val a = Meta.address
        val query = QueryDsl.from(a).map { it.map { address -> address.copy(version = 100) } }
        val list = db.runQuery(query)
        assertTrue(list.all { it.version == 100 })
    }

    @Test
    fun zip() {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        val q1 = QueryDsl.insert(a).single(address)
        val q2 = QueryDsl.from(a)
        val q3 = q1.zip(q2)
        val (first, second) = db.runQuery(q3)
        assertEquals(address, first)
        assertEquals(16, second.size)
        println(q3.dryRun())
    }

    @Test
    fun flatMap() {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        val query = QueryDsl.insert(a).single(address).flatMap {
            val addressId = it.addressId
            val e = Meta.employee
            QueryDsl.from(e).where { e.addressId less addressId }
        }
        val list = db.runQuery(query)
        assertEquals(14, list.size)
    }

    @Test
    fun flatZip() {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        val query = QueryDsl.insert(a).single(address).flatZip {
            val addressId = it.addressId
            val e = Meta.employee
            QueryDsl.from(e).where { e.addressId less addressId }
        }
        val (newAddress, list) = db.runQuery(query)
        assertEquals(16, newAddress.addressId)
        assertEquals(14, list.size)
    }

    @Test
    fun buildSelectQuery() {
        val a = Meta.address
        val e = Meta.employee
        val d = Meta.department
        val join = join(e) {
            e.addressId eq a.addressId
        }
        val where = where {
            e.managerId.isNull()
        }
        val on = on {
            e.departmentId eq d.departmentId
        }
        val orderBy = orderBy(a.addressId, a.street)
        val list = db.runQuery {
            QueryDsl.from(a).innerJoin(join).innerJoin(d, on).where(where).orderBy(orderBy)
        }
        assertEquals(1, list.size)
        assertEquals(9, list.first().addressId)
    }

    @Test
    fun buildAggregateQuery() {
        val e = Meta.employee
        val having = having {
            count(e.departmentId) greater 3
        }
        val groupBy = groupBy(e.departmentId)
        val orderBy = orderBy(e.departmentId)
        val list = db.runQuery {
            QueryDsl.from(e)
                .having(having)
                .groupBy(groupBy)
                .orderBy(orderBy)
                .select(e.departmentId, count(e.departmentId))
        }
        assertEquals(2, list.size)
        assertEquals(listOf(2 to 5L, 3 to 6L), list)
    }

    @Test
    fun buildUpdateQuery() {
        val a = Meta.address
        val set = set(a) {
            a.street eq "HELLO"
        }
        val where = where {
            a.addressId eq 1
        }
        val count = db.runQuery {
            QueryDsl.update(a).set(set).where(where)
        }
        assertEquals(1, count)
    }

    @Test
    fun buildInsertQuery() {
        val a = Meta.address
        val value = values(a) {
            a.addressId eq 20
            a.street eq "HELLO"
        }
        val (count) = db.runQuery {
            QueryDsl.insert(a).values(value)
        }
        assertEquals(1, count)
    }
}
