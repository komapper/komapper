package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.EntityQuery

@ExtendWith(Env::class)
class EntitySelectQueryJoinTest(private val db: Database) {

    @Test
    fun innerJoin() {
        val a = Address.metamodel()
        val e = Employee.metamodel()
        val list = db.execute {
            EntityQuery.from(a).innerJoin(e) {
                a.addressId eq e.addressId
            }
        }
        assertEquals(14, list.size)
    }

    @Test
    fun leftJoin() {
        val a = Address.metamodel()
        val e = Employee.metamodel()
        val list = db.execute {
            EntityQuery.from(a).leftJoin(e) {
                a.addressId eq e.addressId
            }
        }
        assertEquals(15, list.size)
    }

    @Test
    fun innerJoin_multiConditions() {
        val employee = Employee.metamodel()
        val manager = Employee.metamodel()
        val list = db.execute {
            EntityQuery.from(employee).innerJoin(manager) {
                employee.managerId eq manager.employeeId
                manager.managerId.isNull()
            }
        }
        println(list)
        assertEquals(3, list.size)
    }

    @Test
    fun association_many_to_one() {
        val e = Employee.metamodel()
        val d = Department.metamodel()
        val list = db.execute {
            EntityQuery.from(e).innerJoin(d) {
                e.departmentId eq d.departmentId
            }.associate(e, d) { employee, department ->
                employee.copy(department = department)
            }
        }
        assertEquals(14, list.size)
        assertTrue(list.all { it.department != null })
    }

    @Test
    fun association_one_to_many() {
        val d = Department.metamodel()
        val e = Employee.metamodel()
        val list = db.execute {
            EntityQuery.from(d).innerJoin(e) {
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
    fun association_one_to_one() {
        val a = Address.metamodel()
        val e = Employee.metamodel()
        val list = db.execute {
            EntityQuery.from(e).innerJoin(a) {
                e.addressId eq a.addressId
            }.associate(e, a) { employee, address ->
                employee.copy(address = address)
            }
        }
        assertEquals(14, list.size)
        assertTrue(list.all { it.address != null })
    }
}
