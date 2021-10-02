package integration.r2dbc

import integration.Address
import integration.Department
import integration.Employee
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.count
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class SqlSelectQuerySelectTest(private val db: R2dbcDatabase) {

    @Test
    fun selectColumns() = inTransaction(db) {
        val a = Address.meta
        val streetList = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.street)
        }.toList()
        assertEquals(listOf("STREET 1", "STREET 2"), streetList)
    }

    @Test
    fun selectColumns_first() = inTransaction(db) {
        val a = Address.meta
        val value = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.street)
                .first()
        }
        assertEquals("STREET 1", value)
    }

    @Test
    fun selectColumnsAsPair() = inTransaction(db) {
        val a = Address.meta
        val pairList = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street)
        }.toList()
        assertEquals(listOf(1 to "STREET 1", 2 to "STREET 2"), pairList)
    }

    @Test
    fun selectColumnsAsTriple() = inTransaction(db) {
        val a = Address.meta
        val tripleList = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version)
        }.toList()
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1)
            ),
            tripleList
        )
    }

    @Test
    fun selectColumnsAsRecord() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version, concat(a.street, " test"))
        }.toList()
        assertEquals(2, list.size)
        val record0 = list[0]
        assertEquals(1, record0[a.addressId])
        assertEquals("STREET 1", record0[a.street])
        assertEquals(1, record0[a.version])
        assertEquals("STREET 1 test", record0[concat(a.street, " test")])
        val record1 = list[1]
        assertEquals(2, record1[a.addressId])
        assertEquals("STREET 2", record1[a.street])
        assertEquals(1, record1[a.version])
        assertEquals("STREET 2 test", record1[concat(a.street, " test")])
    }

    @Test
    fun selectEntity() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val list: List<Address> = db.runQuery {
            SqlDsl.from(a)
                .leftJoin(e) {
                    a.addressId eq e.addressId
                }
                .orderBy(a.addressId)
        }.toList()
        assertEquals(15, list.size)
    }

    @Test
    fun selectEntitiesAsPair_leftJoin() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val list: List<Pair<Address, Employee?>> = db.runQuery {
            SqlDsl.from(a)
                .leftJoin(e) {
                    a.addressId eq e.addressId
                }
                .orderBy(a.addressId)
                .select(e)
        }.toList()
        assertEquals(15, list.size)
        assertNotNull(list[14].first)
        assertNull(list[14].second)
    }

    @Test
    fun selectEntitiesAsPair_innerJoin() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val list: List<Pair<Address, Employee?>> = db.runQuery {
            SqlDsl.from(a).innerJoin(e) {
                a.addressId eq e.addressId
            }.select(e)
        }.toList()
        assertEquals(14, list.size)
        assertTrue(list.all { (_, employee) -> employee != null })
    }

    @Test
    fun selectEntitiesAsTriple() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val d = Department.meta
        val list = db.runQuery {
            SqlDsl.from(a)
                .innerJoin(e) {
                    a.addressId eq e.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.select(e, d)
        }.toList()
        assertEquals(14, list.size)
        assertTrue(list.all { (_, employee, department) -> employee != null && department != null })
    }

    @Test
    fun selectEntitiesAsRecord() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val d = Department.meta
        val list = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .innerJoin(e) {
                    a.addressId eq e.addressId
                }
                .innerJoin(d) {
                    e.departmentId eq d.departmentId
                }
                .orderBy(a.addressId)
                .select(a, e, d)
        }.toList()
        assertEquals(2, list.size)
        val record0 = list[0]
        assertTrue(record0[a] is Address)
        assertTrue(record0[e] is Employee)
        assertTrue(record0[d] is Department)
    }

    @Test
    fun selectColumnsAsPair_scalar() = inTransaction(db) {
        val d = Department.meta
        val e = Employee.meta
        val subquery = SqlDsl.from(e).where { d.departmentId eq e.departmentId }.select(count())
        val list = db.runQuery {
            SqlDsl.from(d)
                .orderBy(d.departmentId)
                .select(d.departmentName, subquery)
        }.toList()
        assertEquals(4, list.size)
        assertEquals("ACCOUNTING" to 3L, list[0])
        assertEquals("RESEARCH" to 5L, list[1])
        assertEquals("SALES" to 6L, list[2])
        assertEquals("OPERATIONS" to 0L, list[3])
    }
}
