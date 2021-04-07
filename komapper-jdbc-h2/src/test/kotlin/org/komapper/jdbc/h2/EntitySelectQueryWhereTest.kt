package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.Subquery
import org.komapper.core.dsl.desc
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope.Companion.plus

@ExtendWith(Env::class)
class EntitySelectQueryWhereTest(private val db: Database) {

    @Test
    fun isNull() {
        val e = Employee.metamodel()
        val list = db.execute {
            EntityQuery.from(e).where {
                e.managerId.isNull()
            }
        }
        assertEquals(listOf(9), list.map { it.employeeId })
    }

    @Test
    fun isNotNull() {
        val e = Employee.metamodel()
        val list = db.execute {
            EntityQuery.from(e).where {
                e.managerId.isNotNull()
            }
        }
        assertTrue(9 !in list.map { it.employeeId })
    }

    @Test
    fun between() {
        val a = Address.metamodel()
        val idList = db.execute {
            EntityQuery.from(a).where {
                a.addressId between 5..10
            }.orderBy(a.addressId)
        }
        assertEquals((5..10).toList(), idList.map { it.addressId })
    }

    @Test
    fun notBetween() {
        val a = Address.metamodel()
        val idList = db.execute {
            EntityQuery.from(a).where {
                a.addressId notBetween 5..10
            }.orderBy(a.addressId)
        }
        val ids = (1..4) + (11..15)
        assertEquals(ids.toList(), idList.map { it.addressId })
    }

    @Test
    fun inList() {
        val a = Address.metamodel()
        val list = db.execute {
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
        val list = db.execute {
            EntityQuery.from(a).where {
                a.addressId notInList (1..9).toList()
            }.orderBy(a.addressId)
        }
        assertEquals((10..15).toList(), list.map { it.addressId })
    }

    @Test
    fun inList_empty() {
        val a = Address.metamodel()
        val list = db.execute {
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
                    Subquery.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }.select(a.addressId)
                }
            }
        val list = db.execute { query }
        assertEquals(5, list.size)
    }

    @Test
    fun notInList_SubQuery() {
        val e = Employee.metamodel()
        val a = Address.metamodel()
        val query =
            EntityQuery.from(e).where {
                e.addressId notInList {
                    Subquery.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }.select(a.addressId)
                }
            }
        val list = db.execute { query }
        assertEquals(9, list.size)
    }

    @Test
    fun exists() {
        val e = Employee.metamodel()
        val a = Address.metamodel()
        val query =
            EntityQuery.from(e).where {
                exists {
                    Subquery.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }
                }
            }
        val list = db.execute { query }
        assertEquals(5, list.size)
    }

    @Test
    fun notExists() {
        val e = Employee.metamodel()
        val a = Address.metamodel()
        val query =
            EntityQuery.from(e).where {
                notExists {
                    Subquery.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }
                }
            }
        val list = db.execute { query }
        assertEquals(9, list.size)
    }

    @Test
    fun not() {
        val a = Address.metamodel()
        val idList = db.execute {
            EntityQuery.from(a).where {
                a.addressId greater 5
                not {
                    a.addressId greaterEq 10
                }
            }.orderBy(a.addressId)
        }.map { it.addressId }
        assertEquals((6..9).toList(), idList)
    }

    @Test
    fun and() {
        val a = Address.metamodel()
        val list = db.execute {
            EntityQuery.from(a).where {
                a.addressId greater 1
                and {
                    a.addressId greater 1
                }
            }.orderBy(a.addressId.desc())
                .limit(2)
                .offset(5)
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
    fun or() {
        val a = Address.metamodel()
        val list = db.execute {
            EntityQuery.from(a).where {
                a.addressId greaterEq 1
                or {
                    a.addressId greaterEq 1
                }
            }.orderBy(a.addressId.desc())
                .limit(2)
                .offset(5)
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
    fun composition() {
        val a = Address.metamodel()
        val w1: WhereDeclaration = {
            a.addressId eq 1
        }
        val w2: WhereDeclaration = {
            a.version eq 1
        }
        val list = db.execute { EntityQuery.from(a).where(w1 + w2) }
        assertEquals(1, list.size)
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
 */
}
