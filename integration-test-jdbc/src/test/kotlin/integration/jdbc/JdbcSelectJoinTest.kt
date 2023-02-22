package integration.jdbc

import integration.core.address
import integration.core.department
import integration.core.departments
import integration.core.employee
import integration.core.employees
import integration.core.manager
import integration.core.person
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.asContext
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcSelectJoinTest(private val db: JdbcDatabase) {

    @Test
    fun innerJoin() {
        val a = Meta.address
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(a).innerJoin(e) {
                a.addressId eq e.addressId
            }
        }
        assertEquals(14, list.size)
    }

    @Test
    fun leftJoin() {
        val a = Meta.address
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(a).leftJoin(e) {
                a.addressId eq e.addressId
            }
        }
        assertEquals(15, list.size)
    }

    @Test
    fun innerJoin_multiConditions() {
        val employee = Meta.employee
        val manager = Meta.manager
        val list = db.runQuery {
            QueryDsl.from(employee).innerJoin(manager) {
                employee.managerId eq manager.employeeId
                manager.managerId.isNull()
            }
        }
        println(list)
        assertEquals(3, list.size)
    }

    @Test
    fun include_no_association() {
        val a = Meta.address
        val e = Meta.employee
        val d = Meta.department
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.include()
        }

        assertTrue(store.contains(e))
        assertTrue(!store.contains(a))
        assertTrue(!store.contains(d))

        assertTrue(store.oneToMany(a, d).isEmpty())
        assertTrue(store.oneToMany(a, e).isEmpty())
        assertTrue(store.oneToMany(e, a).isEmpty())
        assertTrue(store.oneToMany(e, d).isEmpty())
        assertTrue(store.oneToMany(d, a).isEmpty())
        assertTrue(store.oneToMany(d, e).isEmpty())
    }

    @Test
    fun include_one_association() {
        val a = Meta.address
        val e = Meta.employee
        val d = Meta.department
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.include(a)
        }

        assertTrue(a in store)
        assertTrue(e in store)
        assertTrue(d !in store)

        assertTrue(store.oneToMany(a, d).isEmpty())
        assertTrue(store.oneToMany(a, e).isNotEmpty())
        assertTrue(store.oneToMany(e, a).isNotEmpty())
        assertTrue(store.oneToMany(e, d).isEmpty())
        assertTrue(store.oneToMany(d, a).isEmpty())
        assertTrue(store.oneToMany(d, e).isEmpty())
    }

    @Test
    fun include_two_associations() {
        val a = Meta.address
        val e = Meta.employee
        val d = Meta.department
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.include(a, d)
        }

        assertTrue(a in store)
        assertTrue(e in store)
        assertTrue(d in store)

        assertTrue(store.oneToMany(a, d).isNotEmpty())
        assertTrue(store.oneToMany(a, e).isNotEmpty())
        assertTrue(store.oneToMany(e, a).isNotEmpty())
        assertTrue(store.oneToMany(e, d).isNotEmpty())
        assertTrue(store.oneToMany(d, a).isNotEmpty())
        assertTrue(store.oneToMany(d, e).isNotEmpty())
    }

    @Test
    fun includeAll() {
        val a = Meta.address
        val e = Meta.employee
        val d = Meta.department
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.includeAll()
        }

        assertTrue(a in store)
        assertTrue(e in store)
        assertTrue(d in store)

        assertTrue(store.oneToMany(a, d).isNotEmpty())
        assertTrue(store.oneToMany(a, e).isNotEmpty())
        assertTrue(store.oneToMany(e, a).isNotEmpty())
        assertTrue(store.oneToMany(e, d).isNotEmpty())
        assertTrue(store.oneToMany(d, a).isNotEmpty())
        assertTrue(store.oneToMany(d, e).isNotEmpty())
    }

    @Test
    fun oneToMany() {
        val d = Meta.department
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(d).innerJoin(e) {
                d.departmentId eq e.departmentId
            }.includeAll()
        }

        val map = store.oneToMany(d, e)
        assertEquals(3, map.size)
        val employees1 = map.filterKeys { it.departmentId == 1 }.values.first()
        val employees2 = map.filterKeys { it.departmentId == 2 }.values.first()
        val employees3 = map.filterKeys { it.departmentId == 3 }.values.first()
        assertEquals(3, employees1.size)
        assertEquals(5, employees2.size)
        assertEquals(6, employees3.size)
    }

    @Test
    fun oneToManyById() {
        val d = Meta.department
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(d).innerJoin(e) {
                d.departmentId eq e.departmentId
            }.includeAll()
        }

        val map = store.oneToManyById(d, e)
        assertEquals(3, map.size)
        val employees1 = map[1]
        val employees2 = map[2]
        val employees3 = map[3]
        assertNotNull(employees1)
        assertNotNull(employees2)
        assertNotNull(employees3)
        assertEquals(3, employees1.size)
        assertEquals(5, employees2.size)
        assertEquals(6, employees3.size)
    }

    @Test
    fun oneToOne() {
        val a = Meta.address
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.includeAll()
        }

        val map = store.oneToOne(e, a)
        assertEquals(14, map.size)
        assertTrue(map.values.all { it != null })

        val address1 = map.filterKeys { it.employeeId == 1 }.values.first()
        assertNotNull(address1)
        assertEquals(1, address1.addressId)
    }

    @Test
    fun oneToOneById() {
        val a = Meta.address
        val e = Meta.employee
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.includeAll()
        }

        val map = store.oneToOneById(e, a)
        assertEquals(14, map.size)
        assertTrue(map.values.all { it != null })

        val address1 = map[1]
        assertNotNull(address1)
        assertEquals(1, address1.addressId)
    }

    @Test
    fun oneToMany_selfJoin() {
        val e = Meta.employee
        val m = Meta.manager
        val store = db.runQuery {
            QueryDsl.from(m).innerJoin(e) {
                m.employeeId eq e.managerId
            }.includeAll()
        }

        val managers = store[m]
        assertEquals(6, managers.size)
        assertEquals(managers.map { it.employeeId }.toSet(), setOf(4, 6, 7, 8, 9, 13))

        val oneToMany = store.oneToMany(m, e)
        assertTrue(oneToMany.keys.containsAll(managers))
        assertTrue(managers.containsAll(oneToMany.keys))
    }

    @Test
    fun get() {
        val a = Meta.address
        val e = Meta.employee
        val d = Meta.department
        val store = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.orderBy(d.departmentId).includeAll()
        }
        assertTrue(a in store)
        assertTrue(e in store)
        assertTrue(d in store)
        assertFalse(Meta.person in store)
        assertEquals(14, store[a].size)
        assertEquals(14, store[e].size)
        assertEquals(3, store[d].size)
    }

    @Test
    fun navigation_oneToMany_oneToOne() {
        val d = Meta.department
        val e = Meta.employee
        val a = Meta.address
        val store = db.runQuery {
            QueryDsl.from(d)
                .innerJoin(e) {
                    d.departmentId eq e.departmentId
                }.innerJoin(a) {
                    e.addressId eq a.addressId
                }.includeAll()
        }
        for (department in store.departments()) {
            val employees = department.employees(store)
            for (employee in employees) {
                val address = employee.address(store)
                println("department=${department.departmentName}, employee=${employee.employeeName}, address=${address?.street}")
            }
        }
        val addresses = store[d].flatMap { it.employees(store) }.mapNotNull { it.address(store) }
        assertEquals(14, addresses.size)
    }

    @Test
    fun navigation_manyToOne_oneToOne() {
        val d = Meta.department
        val e = Meta.employee
        val a = Meta.address
        val store = db.runQuery {
            QueryDsl.from(d)
                .innerJoin(e) {
                    d.departmentId eq e.departmentId
                }.innerJoin(a) {
                    e.addressId eq a.addressId
                }.includeAll()
        }
        for (employee in store[e]) {
            val department = employee.department(store)
            val address = employee.address(store)
            println("department=${department?.departmentName}, employee=${employee.employeeName}, address=${address?.street}")
        }
        val departments = store[e].mapNotNull { it.department(store) }.distinct()
        assertEquals(3, departments.size)
    }

    @Test
    fun navigation_using_context_receiver() {
        val d = Meta.department
        val e = Meta.employee
        val a = Meta.address
        val store = db.runQuery {
            QueryDsl.from(d)
                .innerJoin(e) {
                    d.departmentId eq e.departmentId
                }.innerJoin(a) {
                    e.addressId eq a.addressId
                }.includeAll()
        }
        with(store.asContext()) {
            assertEquals(3, departments().size)
            for (department in departments()) {
                for (employee in department.employees()) {
                    val address = employee.address()
                    println("department=${department.departmentName}, employee=${employee.employeeName}, address=${address?.street}")
                }
            }
        }
    }

    @Test
    fun navigation_selfJoin_manyToOne() {
        val e = Meta.employee
        val m = Meta.manager
        val store = db.runQuery {
            QueryDsl.from(e)
                .leftJoin(m) {
                    e.managerId eq m.employeeId
                }.includeAll()
        }
        for (employee in store[e]) {
            val manager = employee.manager(store)
            println("employee=${employee.employeeName}, manager=${manager?.employeeName}")
        }
        val managers = store[e].mapNotNull { it.manager(store) }.distinct()
        assertEquals(6, managers.size)
    }

    @Test
    fun navigation_selfJoin_oneToMany() {
        val e = Meta.employee
        val m = Meta.manager
        val store = db.runQuery {
            QueryDsl.from(e)
                .leftJoin(m) {
                    e.managerId eq m.employeeId
                }.includeAll()
        }
        for (manager in store[m]) {
            for (employee in manager.employees(store)) {
                println("employee=${employee.employeeName}, manager=${manager.employeeName}")
            }
        }
        val employees = store[m].flatMap { it.employees(store) }
        assertEquals(13, employees.size)
    }
}
