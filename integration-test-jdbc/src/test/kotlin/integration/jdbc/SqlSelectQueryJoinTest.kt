package integration.jdbc

import integration.Address
import integration.Department
import integration.Employee
import integration.meta
import integration.newMeta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class SqlSelectQueryJoinTest(private val db: JdbcDatabase) {

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

    @Test
    fun innerJoin_multiConditions() {
        val employee = Employee.meta
        val manager = Employee.newMeta()
        val list = db.runQuery {
            SqlDsl.from(employee).innerJoin(manager) {
                employee.managerId eq manager.employeeId
                manager.managerId.isNull()
            }
        }
        println(list)
        assertEquals(3, list.size)
    }

    @Test
    fun fetchAll_many_to_one() {
        val e = Employee.meta
        val d = Department.meta
        val aggregate = db.runQuery {
            SqlDsl.from(e).innerJoin(d) {
                e.departmentId eq d.departmentId
            }.includeAll()
        }

        assertTrue(aggregate.hasAssociation(d to e))
        val map = aggregate.oneToMany(d to e)
        assertEquals(3, map.size)
        assertSame(map, aggregate.oneToMany(d to e))
        val employees1 = map.filterKeys { it.departmentId == 1 }.values.first()
        val employees2 = map.filterKeys { it.departmentId == 2 }.values.first()
        val employees3 = map.filterKeys { it.departmentId == 3 }.values.first()
        assertEquals(3, employees1.size)
        assertEquals(5, employees2.size)
        assertEquals(6, employees3.size)
    }

    @Test
    fun fetchAll_one_to_many() {
        val d = Department.meta
        val e = Employee.meta
        val aggregate = db.runQuery {
            SqlDsl.from(d).innerJoin(e) {
                d.departmentId eq e.departmentId
            }.includeAll()
        }

        assertTrue(aggregate.hasAssociation(d to e))
        val map = aggregate.oneToMany(d to e)
        assertEquals(3, map.size)
        val employees1 = map.filterKeys { it.departmentId == 1 }.values.first()
        val employees2 = map.filterKeys { it.departmentId == 2 }.values.first()
        val employees3 = map.filterKeys { it.departmentId == 3 }.values.first()
        assertEquals(3, employees1.size)
        assertEquals(5, employees2.size)
        assertEquals(6, employees3.size)
    }

    @Test
    fun fetchAll_one_to_one() {
        val a = Address.meta
        val e = Employee.meta
        val d = Department.meta
        val aggregate = db.runQuery {
            SqlDsl.from(e).innerJoin(a) {
                e.addressId eq a.addressId
            }.innerJoin(d) {
                e.departmentId eq d.departmentId
            }.includeAll()
        }

        assertTrue(aggregate.hasAssociation(e to a))
        val map = aggregate.oneToOne(e to a)
        assertEquals(14, map.size)
        assertTrue(map.values.all { it != null })

        assertTrue(aggregate.hasAssociation(e to d))
        val map2 = aggregate.oneToOne(e to d)
        assertEquals(14, map.size)
        assertTrue(map2.values.all { it != null })
    }

    @Test
    fun fetchExplicitly_one_to_one() {
        val a = Address.meta
        val e = Employee.meta
        val d = Department.meta
        val aggregate = db.runQuery {
            SqlDsl.from(e).innerJoin(a) {
                e.addressId eq a.addressId
            }.innerJoin(d) {
                e.departmentId eq d.departmentId
            }.include(a)
        }

        assertTrue(aggregate.hasAssociation(e to a))
        val map = aggregate.oneToOne(e to a)
        assertEquals(14, map.size)
        assertTrue(map.values.all { it != null })

        assertFalse(aggregate.hasAssociation(e to d))
        val map2 = aggregate.oneToOne(e to d)
        assertEquals(0, map2.size)
    }
}
