package integration.r2dbc

import integration.Address
import integration.Employee
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.operator.desc
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.query.andThen
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class SqlSelectQueryWhereTest(private val db: R2dbcDatabase) {

    @Test
    fun isNull() = inTransaction(db) {
        val e = Employee.meta
        val list = db.runQuery {
            QueryDsl.from(e).where {
                e.managerId.isNull()
            }
        }.toList()
        assertEquals(listOf(9), list.map { it.employeeId })
    }

    @Test
    fun isNotNull() = inTransaction(db) {
        val e = Employee.meta
        val list = db.runQuery {
            QueryDsl.from(e).where {
                e.managerId.isNotNull()
            }
        }.toList()
        assertTrue(9 !in list.map { it.employeeId })
    }

    @Test
    fun between() = inTransaction(db) {
        val a = Address.meta
        val idList = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId between 5..10
            }.orderBy(a.addressId)
        }.toList()
        assertEquals((5..10).toList(), idList.map { it.addressId })
    }

    @Test
    fun notBetween() = inTransaction(db) {
        val a = Address.meta
        val idList = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId notBetween 5..10
            }.orderBy(a.addressId)
        }.toList()
        val ids = (1..4) + (11..15)
        assertEquals(ids.toList(), idList.map { it.addressId })
    }

    @Test
    fun like() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street like "STREET 1_"
            }.orderBy(a.addressId)
        }.toList()
        assertEquals((10..15).toList(), list.map { it.addressId })
    }

    @Test
    fun like_asPrefix() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street like "STREET 1".asPrefix()
            }.orderBy(a.addressId)
        }.toList()
        assertEquals(listOf(1) + (10..15), list.map { it.addressId })
    }

    @Test
    fun like_asInfix() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street like "T 1".asInfix()
            }.orderBy(a.addressId)
        }.toList()
        assertEquals(listOf(1) + (10..15), list.map { it.addressId })
    }

    @Test
    fun like_asSuffix() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street like "1".asSuffix()
            }.orderBy(a.addressId)
        }.toList()
        assertEquals(listOf(1, 11), list.map { it.addressId })
    }

    @Test
    fun like_escape() = inTransaction(db) {
        val a = Address.meta
        val insertQuery = QueryDsl.insert(a).single(Address(16, "\\STREET _16%", 1))
        val selectQuery = QueryDsl.from(a).where {
            a.street like escape("\\S") + text("%") + escape("T _16%")
        }.orderBy(a.addressId)
        val list = db.runQuery {
            insertQuery.andThen(selectQuery)
        }.toList()
        assertEquals(listOf(16), list.map { it.addressId })
    }

    @Test
    fun like_escapeWithEscapeSequence() = inTransaction(db) {
        val a = Address.meta
        val insertQuery = QueryDsl.insert(a).single(Address(16, "\\STREET _16%", 1))
        val selectQuery = QueryDsl.from(a).where {
            a.street like escape("\\S") + text("%") + escape("T _16%")
        }.orderBy(a.addressId).options {
            it.copy(escapeSequence = "|")
        }
        val list = db.runQuery {
            insertQuery.andThen(selectQuery)
        }.toList()
        assertEquals(listOf(16), list.map { it.addressId })
    }

    @Test
    fun notLike() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notLike "STREET 1_"
            }.orderBy(a.addressId)
        }.toList()
        assertEquals((1..9).toList(), list.map { it.addressId })
    }

    @Test
    fun notLike_asPrefix() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notLike "STREET 1".asPrefix()
            }.orderBy(a.addressId)
        }.toList()
        assertEquals((2..9).toList(), list.map { it.addressId })
    }

    @Test
    fun notLike_asInfix() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notLike "T 1".asInfix()
            }.orderBy(a.addressId)
        }.toList()
        assertEquals((2..9).toList(), list.map { it.addressId })
    }

    @Test
    fun notLike_asSuffix() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notLike "1".asSuffix()
            }.orderBy(a.addressId)
        }.toList()
        assertEquals(((2..10) + (12..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun notLike_escape() = inTransaction(db) {
        val a = Address.meta
        val insertQuery = QueryDsl.insert(a).single(Address(16, "\\STREET _16%", 1))
        val selectQuery = QueryDsl.from(a).where {
            a.street notLike escape("\\S") + text("%") + escape("T _16%")
        }.orderBy(a.addressId)
        val list = db.runQuery {
            insertQuery.andThen(selectQuery)
        }.toList()
        assertEquals((1..15).toList(), list.map { it.addressId })
    }

    @Test
    fun startsWith() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street startsWith "STREET 1"
            }.orderBy(a.addressId)
        }.toList()
        assertEquals(listOf(1) + (10..15), list.map { it.addressId })
    }

    @Test
    fun startsWith_escape() = inTransaction(db) {
        val a = Address.meta
        val insertQuery = QueryDsl.insert(a).single(Address(16, "STREET 1%6", 1))
        val selectQuery = QueryDsl.from(a).where {
            a.street startsWith "STREET 1%"
        }.orderBy(a.addressId)
        val list = db.runQuery { insertQuery.andThen(selectQuery) }.toList()
        assertEquals(listOf(16), list.map { it.addressId })
    }

    @Test
    fun notStartsWith() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notStartsWith "STREET 1"
            }.orderBy(a.addressId)
        }.toList()
        assertEquals((2..9).toList(), list.map { it.addressId })
    }

    @Test
    fun contains() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street contains "T 1"
            }.orderBy(a.addressId)
        }.toList()
        assertEquals(listOf(1) + (10..15), list.map { it.addressId })
    }

    @Test
    fun notContains() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notContains "T 1"
            }.orderBy(a.addressId)
        }.toList()
        assertEquals((2..9).toList(), list.map { it.addressId })
    }

    @Test
    fun endsWith() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street endsWith "1"
            }.orderBy(a.addressId)
        }.toList()
        assertEquals(listOf(1, 11), list.map { it.addressId })
    }

    @Test
    fun notEndsWith() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notEndsWith "1"
            }.orderBy(a.addressId)
        }.toList()
        assertEquals(((2..10) + (12..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun inList() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId inList listOf(9, 10)
            }.orderBy(a.addressId.desc())
        }.toList()
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }

    @Test
    fun notInList() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId notInList (1..9).toList()
            }.orderBy(a.addressId)
        }.toList()
        assertEquals((10..15).toList(), list.map { it.addressId })
    }

    @Test
    fun inList_empty() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId inList emptyList()
            }.orderBy(a.addressId.desc())
        }.toList()
        assertTrue(list.isEmpty())
    }

    @Test
    fun inList_SubQuery() = inTransaction(db) {
        val e = Employee.meta
        val a = Address.meta
        val query =
            QueryDsl.from(e).where {
                e.addressId inList {
                    QueryDsl.from(a)
                        .where {
                            e.addressId eq a.addressId
                            e.employeeName like "%S%"
                        }.select(a.addressId)
                }
            }
        val list = db.runQuery { query }.toList()
        assertEquals(5, list.size)
    }

    @Test
    fun notInList_SubQuery() = inTransaction(db) {
        val e = Employee.meta
        val a = Address.meta
        val query =
            QueryDsl.from(e).where {
                e.addressId notInList {
                    QueryDsl.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }.select(a.addressId)
                }
            }
        val list = db.runQuery { query }.toList()
        assertEquals(9, list.size)
    }

    @Test
    fun inList2() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId to a.version inList2 listOf(9 to 1, 10 to 1)
            }.orderBy(a.addressId.desc())
        }.toList()
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }

    @Test
    fun notInList2() = inTransaction(db) {
        val seq = sequence {
            var i = 0
            while (++i < 10) yield(i to 1)
        }
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId to a.version notInList2 seq.toList()
            }.orderBy(a.addressId)
        }.toList()
        assertEquals((10..15).toList(), list.map { it.addressId })
    }

    @Test
    fun inList2_SubQuery() = inTransaction(db) {
        val e = Employee.meta
        val a = Address.meta
        val query =
            QueryDsl.from(e).where {
                e.addressId to e.version inList2 {
                    QueryDsl.from(a)
                        .where {
                            e.addressId eq a.addressId
                            e.employeeName like "%S%"
                        }.select(a.addressId, a.version)
                }
            }
        val list = db.runQuery { query }.toList()
        assertEquals(5, list.size)
    }

    @Test
    fun notInList_SubQuery2() = inTransaction(db) {
        val e = Employee.meta
        val a = Address.meta
        val query =
            QueryDsl.from(e).where {
                e.addressId to e.version notInList2 {
                    QueryDsl.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }.select(a.addressId, a.version)
                }
            }
        val list = db.runQuery { query }.toList()
        assertEquals(9, list.size)
    }

    @Test
    fun exists() = inTransaction(db) {
        val e = Employee.meta
        val a = Address.meta
        val query =
            QueryDsl.from(e).where {
                exists {
                    QueryDsl.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }
                }
            }
        val list = db.runQuery { query }.toList()
        assertEquals(5, list.size)
    }

    @Test
    fun notExists() = inTransaction(db) {
        val e = Employee.meta
        val a = Address.meta
        val query =
            QueryDsl.from(e).where {
                notExists {
                    QueryDsl.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }
                }
            }
        val list = db.runQuery { query }.toList()
        assertEquals(9, list.size)
    }

    @Test
    fun not() = inTransaction(db) {
        val a = Address.meta
        val idList = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId greater 5
                not {
                    a.addressId greaterEq 10
                }
            }.orderBy(a.addressId)
        }.map { it.addressId }.toList()
        assertEquals((6..9).toList(), idList)
    }

    @Test
    fun and() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId greater 1
                and {
                    a.addressId greater 1
                }
            }.orderBy(a.addressId.desc())
                .limit(2)
                .offset(5)
        }.toList()
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }

    @Test
    fun or() = inTransaction(db) {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId greaterEq 1
                or {
                    a.addressId greaterEq 1
                }
            }.orderBy(a.addressId.desc())
                .limit(2)
                .offset(5)
        }.toList()
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }

    @Test
    fun composition() = inTransaction(db) {
        val a = Address.meta
        val w1: WhereDeclaration = {
            a.addressId eq 1
        }
        val w2: WhereDeclaration = {
            a.version eq 1
        }
        val list = db.runQuery { QueryDsl.from(a).where(w1 + w2) }.toList()
        assertEquals(1, list.size)
    }
}
