package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.concat

@ExtendWith(Env::class)
class SqlSelectQuerySelectTest(private val db: Database) {

    @Test
    fun selectProperty() {
        val a = Address.metamodel()
        val streetList = db.execute {
            SqlQuery.from(a)
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
        val a = Address.metamodel()
        val value = db.execute {
            SqlQuery.from(a)
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
        val a = Address.metamodel()
        val pairList = db.execute {
            SqlQuery.from(a)
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
        val a = Address.metamodel()
        val tripleList = db.execute {
            SqlQuery.from(a)
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
        val a = Address.metamodel()
        val list = db.execute {
            SqlQuery.from(a)
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
        val a = Address.metamodel()
        val e = Employee.metamodel()
        val list: List<Employee?> = db.execute {
            SqlQuery.from(a)
                .leftJoin(e) {
                    a.addressId eq e.addressId
                }
                .orderBy(a.addressId)
                .select(e)
        }
        assertEquals(15, list.size)
        assertNull(list[14])
    }

    @Test
    fun selectEntitiesAsPair_leftJoin() {
        val a = Address.metamodel()
        val e = Employee.metamodel()
        val list: List<Pair<Address?, Employee?>> = db.execute {
            SqlQuery.from(a)
                .leftJoin(e) {
                    a.addressId eq e.addressId
                }
                .orderBy(a.addressId)
                .select(a, e)
        }
        assertEquals(15, list.size)
        assertNotNull(list[14].first)
        assertNull(list[14].second)
    }

    @Test
    fun selectEntitiesAsPair_innerJoin() {
        val a = Address.metamodel()
        val e = Employee.metamodel()
        val list = db.execute {
            SqlQuery.from(a).innerJoin(e) {
                a.addressId eq e.addressId
            }.select(a, e)
        }
        assertEquals(14, list.size)
        val (address, employee) = list[0]
        assertNotNull(address)
        assertNotNull(employee)
    }

    @Test
    fun selectEntitiesAsTriple() {
        val a = Address.metamodel()
        val e = Employee.metamodel()
        val d = Department.metamodel()
        val list = db.execute {
            SqlQuery.from(a)
                .innerJoin(e) {
                    a.addressId eq e.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.select(a, e, d)
        }
        assertEquals(14, list.size)
        val (address, employee, department) = list[0]
        assertNotNull(address)
        assertNotNull(employee)
        assertNotNull(department)
    }

    @Test
    fun selectEntitiesAsRecord() {
        val a = Address.metamodel()
        val list = db.execute {
            SqlQuery.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a, a, a, a)
        }
        assertEquals(2, list.size)
        val record0 = list[0]
        val address = record0[a]
        assertNotNull(address)
    }
}
