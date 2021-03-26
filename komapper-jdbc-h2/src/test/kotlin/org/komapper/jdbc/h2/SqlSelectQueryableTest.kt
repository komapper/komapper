package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.query.SqlQuery
import org.komapper.core.query.avg
import org.komapper.core.query.count
import org.komapper.core.query.desc
import org.komapper.core.query.max
import org.komapper.core.query.min
import org.komapper.core.query.sum

@ExtendWith(Env::class)
class SqlSelectQueryableTest(private val db: Database) {

    @Test
    fun find() {
        val a = Address.metamodel()
        val address = db.find(a) { a.addressId eq 1 }
        Assertions.assertNotNull(address)
    }

    @Test
    fun find_multipleCondition() {
        val a = Address.metamodel()
        val address = db.find(a) { a.addressId eq 1; a.version eq 1 }
        Assertions.assertNotNull(address)
    }

    @Test
    fun first() {
        val a = Address.metamodel()
        val address = db.first {
            SqlQuery.from(a).where { a.addressId eq 1 }
        }
        Assertions.assertNotNull(address)
    }

    @Test
    fun list() {
        val a = Address.metamodel()
        val addressList = db.list {
            SqlQuery.from(a).where { a.addressId eq 1 }
        }
        Assertions.assertNotNull(addressList)
    }

    @Test
    fun list_single() {
        val a = Address.metamodel()
        val streetList = db.list {
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
    fun list_pair() {
        val a = Address.metamodel()
        val pairList = db.list {
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
    fun passQuery() {
        val a = Address.metamodel()
        val query = SqlQuery.from(a)
            .where { a.addressId greaterEq 1 }
            .orderBy(a.addressId.desc())
            .limit(2)
            .offset(5)
        val list = db.list(query)
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }

    @Test
    fun join() {
        val a = Address.metamodel()
        val e = Employee.metamodel()
        val list = db.list {
            SqlQuery.from(a).innerJoin(e) {
                a.addressId eq e.addressId
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, list.size)
    }

    @Test
    fun offset() {
        val a = Address.metamodel()
        val list = db.list {
            SqlQuery.from(a).offset(10)
        }
        assertEquals(5, list.size)
    }

    @Test
    fun limit() {
        val a = Address.metamodel()
        val list = db.list {
            SqlQuery.from(a).limit(3)
        }
        assertEquals(3, list.size)
    }

    @Test
    fun offset_limit() {
        val a = Address.metamodel()
        val list = db.list {
            SqlQuery.from(a)
                .orderBy(a.addressId)
                .offset(10)
                .limit(3)
        }
        assertEquals(3, list.size)
        assertEquals(11, list[0].addressId)
        assertEquals(12, list[1].addressId)
        assertEquals(13, list[2].addressId)
    }

    /*
    @Test
    fun inList() {
        val a = Address.metamodel()
        val list = db.list {
            EntityQuery.from(a).where {
                a.addressId inList listOf(9, 10)
            }.orderBy(a.addressId.desc())
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }

    @Test
    fun notInList() {
        val a = Address.metamodel()
        val list = db.list {
            EntityQuery.from(a).where {
                a.addressId notInList (1..9).toList()
            }.orderBy(a.addressId)
        }
        assertEquals((10..15).toList(), list.map { it.addressId })
    }

    @Test
    fun in_empty() {
        val a = Address.metamodel()
        val list = db.list {
            EntityQuery.from(a).where {
                a.addressId inList emptyList()
            }.orderBy(a.addressId.desc())
        }
        assertTrue(list.isEmpty())
    }

    @Test
    fun inList_SubQuery() {
        val e = Employee.metamodel()
        val a = Address.metamodel()
        val query =
            EntityQuery.from(e).where {
                e.addressId inList {
                    EntitySubQuery.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }.select(a.addressId)
                }
            }
        val list = db.list(query)
        assertEquals(5, list.size)
    }

    @Test
    fun notInList_SubQuery() {
        val e = Employee.metamodel()
        val a = Address.metamodel()
        val query =
            EntityQuery.from(e).where {
                e.addressId notInList {
                    EntitySubQuery.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }.select(a.addressId)
                }
            }
        val list = db.list(query)
        assertEquals(9, list.size)
    }

    @Test
    fun exists() {
        val e = Employee.metamodel()
        val a = Address.metamodel()
        val query =
            EntityQuery.from(e).where {
                exists(a).where {
                    e.addressId eq a.addressId
                    e.employeeName like "%S%"
                }
            }
        val list = db.list(query)
        assertEquals(5, list.size)
    }

    @Test
    fun notExists() {
        val e = Employee.metamodel()
        val a = Address.metamodel()
        val query =
            EntityQuery.from(e).where {
                notExists(a).where {
                    e.addressId eq a.addressId
                    e.employeeName like "%S%"
                }
            }
        val list = db.list(query)
        assertEquals(9, list.size)
    }
    
     */

    @Test
    fun forUpdate() {
        val a = Address.metamodel()
        val list = db.list {
            SqlQuery.from(a).where { a.addressId greaterEq 1 }
                .orderBy(a.addressId.desc())
                .limit(2)
                .offset(5)
                .forUpdate()
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }

    @Test
    fun aggregate_avg() {
        val a = Address.metamodel()
        val avg = db.first {
            SqlQuery.from(a).select(avg(a.addressId))
        }
        assertEquals(8.0, avg, 0.0)
    }

    @Test
    fun aggregate_countAsterisk() {
        val a = Address.metamodel()
        val count = db.first {
            SqlQuery.from(a).select(count())
        }
        assertEquals(15, count)
    }

    @Test
    fun aggregate_count() {
        val a = Address.metamodel()
        val count = db.first {
            SqlQuery.from(a).select(count(a.street))
        }
        assertEquals(15, count)
    }

    @Test
    fun aggregate_sum() {
        val a = Address.metamodel()
        val sum = db.first {
            SqlQuery.from(a).select(sum(a.addressId))
        }
        assertEquals(120, sum)
    }

    @Test
    fun aggregate_max() {
        val a = Address.metamodel()
        val max = db.first {
            SqlQuery.from(a).select(max(a.addressId))
        }
        assertEquals(15, max)
    }

    @Test
    fun aggregate_min() {
        val a = Address.metamodel()
        val min = db.first {
            SqlQuery.from(a).select(min(a.addressId))
        }
        assertEquals(1, min)
    }

    @Test
    fun having() {
        val e = Employee.metamodel()
        val list = db.list {
            SqlQuery.from(e)
                .groupBy(e.departmentId)
                .having {
                    count(e.employeeId) greaterEq 4L
                }
                .orderBy(e.departmentId)
                .select(e.departmentId, count(e.employeeId))
        }
        assertEquals(listOf(2 to 5L, 3 to 6L), list)
    }

    @Test
    fun having_empty_groupBy() {
        val e = Employee.metamodel()
        val list = db.list {
            SqlQuery.from(e)
                .having {
                    count(e.employeeId) greaterEq 4L
                }
                .orderBy(e.departmentId)
                .select(e.departmentId, count(e.employeeId))
        }
        assertEquals(listOf(2 to 5L, 3 to 6L), list)
    }

/*


@Test
fun distinct() {
    val list = db.select<NoId> {
        distinct()
    }
    assertEquals(
        listOf(
            NoId(1, 1)
        ), list
    )
}

@Test
fun like() {
    val idList = db.select<Address> {
        where {
            like(Address::street, "STREET 1_")
        }
        orderBy {
            asc(Address::addressId)
        }
    }.map { it.addressId }
    assertEquals((10..15).toList(), idList)
}

@Test
fun notLike() {
    val idList = db.select<Address> {
        where {
            notLike(Address::street, "STREET 1_")
        }
        orderBy {
            asc(Address::addressId)
        }
    }.map { it.addressId }
    assertEquals((1..9).toList(), idList)
}

@Test
fun noArg() {
    val list = db.select<Address>()
    assertEquals(15, list.size)
}




@Test
fun in2() {

    val list = db.select<Address> {
        where {
            in2(Address::addressId, Address::street, listOf(9 to "STREET 9", 10 to "STREET 10"))
        }
        orderBy {
            desc(Address::addressId)
        }
    }
    assertEquals(
        listOf(
            Address(10, "STREET 10", 1),
            Address(9, "STREET 9", 1)
        ), list
    )
}

@Test
fun notIn2() {

    val idList = db.select<Address> {
        where {
            notIn2(Address::addressId, Address::street, listOf(1 to "STREET 1", 2 to "STREET 2"))
        }
        orderBy {
            asc(Address::addressId)
        }
    }.map { it.addressId }
    assertEquals((3..15).toList(), idList)
}

@Test
fun in2_empty() {

    val list = db.select<Address> {
        where {
            in2(Address::addressId, Address::street, emptyList())
        }
        orderBy {
            desc(Address::addressId)
        }
    }
    assertTrue(list.isEmpty())
}

@Test
fun in3() {

    val list = db.select<Address> {
        where {
            in3(
                Address::addressId,
                Address::street,
                Address::version,
                listOf(
                    Triple(9, "STREET 9", 1),
                    Triple(10, "STREET 10", 1)
                )
            )
        }
        orderBy {
            desc(Address::addressId)
        }
    }
    assertEquals(
        listOf(
            Address(10, "STREET 10", 1),
            Address(9, "STREET 9", 1)
        ), list
    )
}

@Test
fun notIn3() {

    val idList = db.select<Address> {
        where {
            notIn3(
                Address::addressId, Address::street, Address::version,
                listOf(
                    Triple(1, "STREET 1", 1),
                    Triple(2, "STREET 2", 1)
                )
            )
        }
        orderBy {
            asc(Address::addressId)
        }
    }.map { it.addressId }
    assertEquals((3..15).toList(), idList)
}

@Test
fun in3_empty() {

    val list = db.select<Address> {
        where {
            in3(Address::addressId, Address::street, Address::version, emptyList())
        }
        orderBy {
            desc(Address::addressId)
        }
    }
    assertTrue(list.isEmpty())
}

@Test
fun between() {

    val idList = db.select<Address> {
        where {
            between(Address::addressId, 5, 10)
        }
        orderBy {
            asc(Address::addressId)
        }
    }.map { it.addressId }
    assertEquals((5..10).toList(), idList)
}


@Test
fun join() {
    val addressMap = mutableMapOf<Employee, Address>()
    val departmentMap = mutableMapOf<Employee, List<Department>>()

    val employees = db.select<Employee> { e ->
        val a = leftJoin<Address> { a ->
            eq(e[Employee::addressId], a[Address::addressId])
            oneToOne { employee, address -> addressMap[employee] = address!! }
        }
        innerJoin<Department> { d ->
            eq(e[Employee::departmentId], d[Department::departmentId])
            oneToMany { employee, departments -> departmentMap[employee] = departments }
        }
        where {
            ge(a[Address::addressId], 1)
        }
        orderBy {
            desc(a[Address::addressId])
        }
        limit(2)
        offset(5)
    }
    assertEquals(2, employees.size)
    assertEquals(2, addressMap.size)
    assertEquals(2, departmentMap.size)
    assertEquals(listOf(9, 8), employees.map { it.employeeId })
}

@Test
fun joinOnly() {
    val employees = db.select<Employee> { e ->
        val a = leftJoin<Address> { a ->
            eq(e[Employee::addressId], a[Address::addressId])
        }
        innerJoin<Department> { d ->
            eq(e[Employee::departmentId], d[Department::departmentId])
        }
        where {
            ge(a[Address::addressId], 1)
        }
        orderBy {
            desc(a[Address::addressId])
        }
        limit(2)
        offset(5)
    }
    assertEquals(2, employees.size)
    assertEquals(listOf(9, 8), employees.map { it.employeeId })
}

@Test
fun innerJoin_oneToOne() {
    val map = mutableMapOf<Int, Boolean>()
    val employees = db.select<Employee> { e ->
        innerJoin<Address> { a ->
            eq(e[Employee::addressId], a[Address::addressId])
            oneToOne { employee, address -> map[employee.employeeId] = address == null }
        }
        orderBy {
            asc(e[Employee::employeeId])
        }
        limit(3)
    }
    assertEquals(listOf(1, 2, 3), employees.map { it.employeeId })
    assertEquals(map, mapOf(1 to false, 2 to false, 3 to false))
}

@Test
fun leftJoin_oneToOne() {
    val map = mutableMapOf<Int, Boolean>()
    val employees = db.select<Employee> { e ->
        leftJoin<Address> { a ->
            eq(e[Employee::addressId], a[Address::addressId])
            oneToOne { employee, address -> map[employee.employeeId] = address == null }
        }
        orderBy {
            asc(e[Employee::employeeId])
        }
        limit(3)
    }
    assertEquals(listOf(1, 2, 3), employees.map { it.employeeId })
    assertEquals(map, mapOf(1 to false, 2 to false, 3 to false))
}

@Test
fun innerJoin_oneToMany() {
    val map = mutableMapOf<Int, Int>()
    val departments = db.select<Department> { d ->
        innerJoin<Employee> { e ->
            eq(d[Department::departmentId], e[Employee::departmentId])
            oneToMany { department, employees -> map[department.departmentId] = employees.size }
        }
        orderBy {
            asc(d[Department::departmentId])
        }
    }
    assertEquals(listOf(1, 2, 3), departments.map { it.departmentId })
    assertEquals(map, mapOf(1 to 3, 2 to 5, 3 to 6))
}

@Test
fun leftJoin_oneToMany() {
    val map = mutableMapOf<Int, Int>()
    val departments = db.select<Department> { d ->
        leftJoin<Employee> { e ->
            eq(d[Department::departmentId], e[Employee::departmentId])
            oneToMany { department, employees -> map[department.departmentId] = employees.size }
        }
        orderBy {
            asc(d[Department::departmentId])
        }
    }
    assertEquals(listOf(1, 2, 3, 4), departments.map { it.departmentId })
    assertEquals(map, mapOf(1 to 3, 2 to 5, 3 to 6, 4 to 0))
}

@Test
fun leftJoin_orderByJoinedEntity_oneToMany() {
    val map = mutableMapOf<Int, Int>()
    val departments = db.select<Department> { d ->
        val e = leftJoin<Employee> { e ->
            eq(d[Department::departmentId], e[Employee::departmentId])
            oneToMany { department, employees -> map[department.departmentId] = employees.size }
        }
        orderBy {
            asc(e[Employee::employeeId])
        }
    }
    println(departments.map { it.departmentId })
    assertEquals(listOf(4, 2, 3, 1), departments.map { it.departmentId })
    assertEquals(map, mapOf(4 to 0, 2 to 5, 3 to 6, 1 to 3))
}


@Test
fun embedded() {

    val list = db.select<Employee> {
        where {
            ge(
                EmployeeDetail::salary,
                BigDecimal(
                    "2000.00"
                )
            )
        }
    }
    assertEquals(6, list.size)
}

@Test
fun nestedEmbedded() {

    val list = db.select<Worker> {
        where {
            ge(
                WorkerSalary::salary,
                BigDecimal(
                    "2000.00"
                )
            )
        }
    }
    assertEquals(6, list.size)
}



 */
}
