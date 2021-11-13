package integration.jdbc

import integration.Address
import integration.Department
import integration.Employee
import integration.meta
import integration.newMeta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class SqlSelectQueryJoinTest(private val db: JdbcDatabase) {

    @Test
    fun innerJoin() {
        val a = Address.meta
        val e = Employee.meta
        val list = db.runQuery {
            QueryDsl.from(a).innerJoin(e) {
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
            QueryDsl.from(a).leftJoin(e) {
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
            QueryDsl.from(employee).innerJoin(manager) {
                employee.managerId eq manager.employeeId
                manager.managerId.isNull()
            }
        }
        println(list)
        assertEquals(3, list.size)
    }

    @Test
    fun include_one_association() {
        val a = Address.meta
        val e = Employee.meta
        val d = Department.meta
        val entityContext = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.include(a)
        }

        assertTrue(entityContext.contains(a to e))
        assertTrue(!entityContext.contains(a to d))
        assertTrue(entityContext.contains(e to a))
        assertTrue(!entityContext.contains(e to d))
        assertTrue(!entityContext.contains(d to a))
        assertTrue(!entityContext.contains(d to e))

        assertTrue(entityContext.associate(a to d).isEmpty())
        assertTrue(entityContext.associate(a to e).isNotEmpty())
        assertTrue(entityContext.associate(e to a).isNotEmpty())
        assertTrue(entityContext.associate(e to d).isEmpty())
        assertTrue(entityContext.associate(d to a).isEmpty())
        assertTrue(entityContext.associate(d to e).isEmpty())
    }

    @Test
    fun include_two_associations() {
        val a = Address.meta
        val e = Employee.meta
        val d = Department.meta
        val entityContext = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.include(a)
                .include(d)
        }

        assertTrue(entityContext.contains(a to e))
        assertTrue(entityContext.contains(a to d))
        assertTrue(entityContext.contains(e to a))
        assertTrue(entityContext.contains(e to d))
        assertTrue(entityContext.contains(d to a))
        assertTrue(entityContext.contains(d to e))

        assertTrue(entityContext.associate(a to d).isNotEmpty())
        assertTrue(entityContext.associate(a to e).isNotEmpty())
        assertTrue(entityContext.associate(e to a).isNotEmpty())
        assertTrue(entityContext.associate(e to d).isNotEmpty())
        assertTrue(entityContext.associate(d to a).isNotEmpty())
        assertTrue(entityContext.associate(d to e).isNotEmpty())
    }

    @Test
    fun includeAll() {
        val a = Address.meta
        val e = Employee.meta
        val d = Department.meta
        val entityContext = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.includeAll()
        }

        assertTrue(entityContext.contains(a to e))
        assertTrue(entityContext.contains(a to d))
        assertTrue(entityContext.contains(e to a))
        assertTrue(entityContext.contains(e to d))
        assertTrue(entityContext.contains(d to a))
        assertTrue(entityContext.contains(d to e))

        assertTrue(entityContext.associate(a to d).isNotEmpty())
        assertTrue(entityContext.associate(a to e).isNotEmpty())
        assertTrue(entityContext.associate(e to a).isNotEmpty())
        assertTrue(entityContext.associate(e to d).isNotEmpty())
        assertTrue(entityContext.associate(d to a).isNotEmpty())
        assertTrue(entityContext.associate(d to e).isNotEmpty())
    }

    @Test
    fun associate() {
        val d = Department.meta
        val e = Employee.meta
        val entityContext = db.runQuery {
            QueryDsl.from(d).innerJoin(e) {
                d.departmentId eq e.departmentId
            }.includeAll()
        }

        assertTrue(entityContext.contains(d to e))
        val map = entityContext.associate(d to e)
        assertEquals(3, map.size)
        val employees1 = map.filterKeys { it.departmentId == 1 }.values.first()
        val employees2 = map.filterKeys { it.departmentId == 2 }.values.first()
        val employees3 = map.filterKeys { it.departmentId == 3 }.values.first()
        assertEquals(3, employees1.size)
        assertEquals(5, employees2.size)
        assertEquals(6, employees3.size)
    }

    @Test
    fun associateById() {
        val d = Department.meta
        val e = Employee.meta
        val entityContext = db.runQuery {
            QueryDsl.from(d).innerJoin(e) {
                d.departmentId eq e.departmentId
            }.includeAll()
        }

        assertTrue(entityContext.contains(d to e))
        val map = entityContext.associateById(d to e)
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
    fun associate_asOneToOne() {
        val a = Address.meta
        val e = Employee.meta
        val entityContext = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.includeAll()
        }

        assertTrue(entityContext.contains(e to a))
        val map = entityContext.associate(e to a).asOneToOne()
        assertEquals(14, map.size)
        assertTrue(map.values.all { it != null })

        val address1 = map.filterKeys { it.employeeId == 1 }.values.first()
        assertNotNull(address1)
        assertEquals(1, address1.addressId)
    }

    @Test
    fun associateById_asOneToOne() {
        val a = Address.meta
        val e = Employee.meta
        val entityContext = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.includeAll()
        }

        assertTrue(entityContext.contains(e to a))
        val map = entityContext.associateById(e to a).asOneToOne()
        assertEquals(14, map.size)
        assertTrue(map.values.all { it != null })

        val address1 = map[1]
        assertNotNull(address1)
        assertEquals(1, address1.addressId)
    }

    @Test
    fun mainEntities() {
        val a = Address.meta
        val e = Employee.meta
        val d = Department.meta
        val entityContext = db.runQuery {
            QueryDsl.from(e)
                .innerJoin(a) {
                    e.addressId eq a.addressId
                }.innerJoin(d) {
                    e.departmentId eq d.departmentId
                }.includeAll()
        }
        val employees = entityContext.mainEntities
        assertEquals(14, employees.size)
    }
}
