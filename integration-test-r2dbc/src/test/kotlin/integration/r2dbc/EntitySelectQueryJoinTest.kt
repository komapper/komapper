package integration.r2dbc

import integration.Address
import integration.Department
import integration.Employee
import integration.meta
import integration.newMeta
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.r2dbc.R2dbcDatabase

@ExtendWith(Env::class)
class EntitySelectQueryJoinTest(private val db: R2dbcDatabase) {

    @Test
    fun innerJoin() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val list = db.runQuery {
            EntityDsl.from(a).innerJoin(e) {
                a.addressId eq e.addressId
            }
        }.toList()
        assertEquals(14, list.size)
    }

    @Test
    fun leftJoin() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val list = db.runQuery {
            EntityDsl.from(a).leftJoin(e) {
                a.addressId eq e.addressId
            }
        }.toList()
        assertEquals(15, list.size)
    }

    @Test
    fun innerJoin_multiConditions() = inTransaction(db) {
        val employee = Employee.meta
        val manager = Employee.newMeta()
        val list = db.runQuery {
            EntityDsl.from(employee).innerJoin(manager) {
                employee.managerId eq manager.employeeId
                manager.managerId.isNull()
            }
        }.toList()
        println(list)
        assertEquals(3, list.size)
    }

    @Test
    fun association_many_to_one() = inTransaction(db) {
        val e = Employee.meta
        val d = Department.meta
        val list = db.runQuery {
            EntityDsl.from(e).innerJoin(d) {
                e.departmentId eq d.departmentId
            }.associate(e, d) { employee, department ->
                employee.copy(department = department)
            }
        }
        assertEquals(14, list.size)
        Assertions.assertTrue(list.all { it.department != null })
    }

    @Test
    fun association_one_to_many() = inTransaction(db) {
        val d = Department.meta
        val e = Employee.meta
        val list = db.runQuery {
            EntityDsl.from(d).innerJoin(e) {
                d.departmentId eq e.departmentId
            }.associate(d, e) { department, employee ->
                val list = department.employeeList + employee
                department.copy(employeeList = list)
            }
        }
        assertEquals(3, list.size)
        val department1 = list.first { it.departmentId == 1 }
        val department2 = list.first { it.departmentId == 2 }
        val department3 = list.first { it.departmentId == 3 }
        assertEquals(3, department1.employeeList.size)
        assertEquals(5, department2.employeeList.size)
        assertEquals(6, department3.employeeList.size)
    }

    @Test
    fun association_one_to_one() = inTransaction(db) {
        val a = Address.meta
        val e = Employee.meta
        val list = db.runQuery {
            EntityDsl.from(e).innerJoin(a) {
                e.addressId eq a.addressId
            }.associate(e, a) { employee, address ->
                employee.copy(address = address)
            }
        }
        assertEquals(14, list.size)
        Assertions.assertTrue(list.all { it.address != null })
    }
}
