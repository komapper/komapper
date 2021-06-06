package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.concat
import org.komapper.core.dsl.count
import org.komapper.jdbc.Database

@ExtendWith(Env::class)
class SqlSelectQuerySelectTest(private val db: Database) {

    @Test
    fun selectProperty() {
        val a = Address.meta
        val streetList = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.street)
        }
        assertEquals(listOf("STREET 1", "STREET 2"), streetList)
    }

    @Test
    fun selectProperty_first() {
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
    fun selectPropertiesAsPair() {
        val a = Address.meta
        val pairList = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street)
        }
        assertEquals(listOf(1 to "STREET 1", 2 to "STREET 2"), pairList)
    }

    @Test
    fun selectPropertiesAsTriple() {
        val a = Address.meta
        val tripleList = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1)
            ),
            tripleList
        )
    }

    @Test
    fun selectPropertiesAsRecord() {
        val a = Address.meta
        val list = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version, concat(a.street, " test"))
        }
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
    fun selectEntity() {
        val a = Address.meta
        val e = Employee.meta
        val list: List<Address> = db.runQuery {
            SqlDsl.from(a)
                .leftJoin(e) {
                    a.addressId eq e.addressId
                }
                .orderBy(a.addressId)
        }
        assertEquals(15, list.size)
    }

    @Test
    fun selectEntitiesAsPair_leftJoin() {
        val a = Address.meta
        val e = Employee.meta
        val list: List<Pair<Address, Employee?>> = db.runQuery {
            SqlDsl.from(a)
                .leftJoin(e) {
                    a.addressId eq e.addressId
                }
                .orderBy(a.addressId)
                .select(e)
        }
        assertEquals(15, list.size)
        assertNotNull(list[14].first)
        assertNull(list[14].second)
    }

    @Test
    fun selectEntitiesAsPair_innerJoin() {
        val a = Address.meta
        val e = Employee.meta
        val list: List<Pair<Address, Employee?>> = db.runQuery {
            SqlDsl.from(a).innerJoin(e) {
                a.addressId eq e.addressId
            }.select(e)
        }
        assertEquals(14, list.size)
        assertTrue(list.all { (_, employee) -> employee != null })
    }

    @Test
    fun selectEntitiesAsTriple() {
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
        }
        assertEquals(14, list.size)
        assertTrue(list.all { (_, employee, department) -> employee != null && department != null })
    }

    @Test
    fun selectEntitiesAsRecord() {
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
        }
        assertEquals(2, list.size)
        val record0 = list[0]
        assertTrue(record0[a] is Address)
        assertTrue(record0[e] is Employee)
        assertTrue(record0[d] is Department)
    }

    @Test
    fun selectProperty2() {
        val d = Department.meta
        val e = Employee.meta
        val subquery = SqlDsl.from(e).where { d.departmentId eq e.departmentId }.select(count())
        val list = db.runQuery {
            SqlDsl.from(d)
                .orderBy(d.departmentId)
                .select(d.departmentName, subquery)
        }
        println(list)
    }
}
