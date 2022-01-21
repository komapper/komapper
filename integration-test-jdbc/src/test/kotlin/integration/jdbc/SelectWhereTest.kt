package integration.jdbc

import integration.Address
import integration.address
import integration.employee
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.operator.desc
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.query.andThen
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class SelectWhereTest(private val db: JdbcDatabase) {

    @Test
    fun isNull() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e).where {
                e.managerId.isNull()
            }
        }
        assertEquals(listOf(9), list.map { it.employeeId })
    }

    @Test
    fun isNotNull() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e).where {
                e.managerId.isNotNull()
            }
        }
        assertTrue(9 !in list.map { it.employeeId })
    }

    @Test
    fun between() {
        val a = Meta.address
        val idList = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId between 5..10
            }.orderBy(a.addressId)
        }
        assertEquals((5..10).toList(), idList.map { it.addressId })
    }

    @Test
    fun notBetween() {
        val a = Meta.address
        val idList = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId notBetween 5..10
            }.orderBy(a.addressId)
        }
        val ids = (1..4) + (11..15)
        assertEquals(ids.toList(), idList.map { it.addressId })
    }

    @Test
    fun like() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street like "STREET 1_"
            }.orderBy(a.addressId)
        }
        assertEquals((10..15).toList(), list.map { it.addressId })
    }

    @Test
    fun like_asPrefix() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street like "STREET 1".asPrefix()
            }.orderBy(a.addressId)
        }
        assertEquals(listOf(1) + (10..15), list.map { it.addressId })
    }

    @Test
    fun like_asInfix() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street like "T 1".asInfix()
            }.orderBy(a.addressId)
        }
        assertEquals(listOf(1) + (10..15), list.map { it.addressId })
    }

    @Test
    fun like_asSuffix() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street like "1".asSuffix()
            }.orderBy(a.addressId)
        }
        assertEquals(listOf(1, 11), list.map { it.addressId })
    }

    @Test
    fun like_escape() {
        val a = Meta.address
        val insertQuery = QueryDsl.insert(a).single(Address(16, "\\STREET _16%", 1))
        val selectQuery = QueryDsl.from(a).where {
            a.street like escape("\\S") + text("%") + escape("T _16%")
        }.orderBy(a.addressId)
        val list = db.runQuery {
            insertQuery.andThen(selectQuery)
        }
        assertEquals(listOf(16), list.map { it.addressId })
    }

    @Test
    fun like_escapeWithEscapeSequence() {
        val a = Meta.address
        val insertQuery = QueryDsl.insert(a).single(Address(16, "\\STREET _16%", 1))
        val selectQuery = QueryDsl.from(a).where {
            a.street like escape("\\S") + text("%") + escape("T _16%")
        }.orderBy(a.addressId).options {
            it.copy(escapeSequence = "|")
        }
        val list = db.runQuery {
            insertQuery.andThen(selectQuery)
        }
        assertEquals(listOf(16), list.map { it.addressId })
    }

    @Test
    fun notLike() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notLike "STREET 1_"
            }.orderBy(a.addressId)
        }
        assertEquals((1..9).toList(), list.map { it.addressId })
    }

    @Test
    fun notLike_asPrefix() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notLike "STREET 1".asPrefix()
            }.orderBy(a.addressId)
        }
        assertEquals((2..9).toList(), list.map { it.addressId })
    }

    @Test
    fun notLike_asInfix() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notLike "T 1".asInfix()
            }.orderBy(a.addressId)
        }
        assertEquals((2..9).toList(), list.map { it.addressId })
    }

    @Test
    fun notLike_asSuffix() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notLike "1".asSuffix()
            }.orderBy(a.addressId)
        }
        assertEquals(((2..10) + (12..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun notLike_escape() {
        val a = Meta.address
        val insertQuery = QueryDsl.insert(a).single(Address(16, "\\STREET _16%", 1))
        val selectQuery = QueryDsl.from(a).where {
            a.street notLike escape("\\S") + text("%") + escape("T _16%")
        }.orderBy(a.addressId)
        val list = db.runQuery {
            insertQuery.andThen(selectQuery)
        }
        assertEquals((1..15).toList(), list.map { it.addressId })
    }

    @Test
    fun startsWith() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street startsWith "STREET 1"
            }.orderBy(a.addressId)
        }
        assertEquals(listOf(1) + (10..15), list.map { it.addressId })
    }

    @Test
    fun startsWith_escape() {
        val a = Meta.address
        val insertQuery = QueryDsl.insert(a).single(Address(16, "STREET 1%6", 1))
        val selectQuery = QueryDsl.from(a).where {
            a.street startsWith "STREET 1%"
        }.orderBy(a.addressId)
        val list = db.runQuery { insertQuery.andThen(selectQuery) }
        assertEquals(listOf(16), list.map { it.addressId })
    }

    @Test
    fun notStartsWith() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notStartsWith "STREET 1"
            }.orderBy(a.addressId)
        }
        assertEquals((2..9).toList(), list.map { it.addressId })
    }

    @Test
    fun contains() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street contains "T 1"
            }.orderBy(a.addressId)
        }
        assertEquals(listOf(1) + (10..15), list.map { it.addressId })
    }

    @Test
    fun notContains() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notContains "T 1"
            }.orderBy(a.addressId)
        }
        assertEquals((2..9).toList(), list.map { it.addressId })
    }

    @Test
    fun endsWith() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street endsWith "1"
            }.orderBy(a.addressId)
        }
        assertEquals(listOf(1, 11), list.map { it.addressId })
    }

    @Test
    fun notEndsWith() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.street notEndsWith "1"
            }.orderBy(a.addressId)
        }
        assertEquals(((2..10) + (12..15)).toList(), list.map { it.addressId })
    }

    @Test
    fun inList() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
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
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId notInList (1..9).toList()
            }.orderBy(a.addressId)
        }
        assertEquals((10..15).toList(), list.map { it.addressId })
    }

    @Test
    fun inList_empty() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId inList emptyList()
            }.orderBy(a.addressId.desc())
        }
        assertTrue(list.isEmpty())
    }

    @Test
    fun inList_SubQuery() {
        val e = Meta.employee
        val a = Meta.address
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
        val list = db.runQuery { query }
        assertEquals(5, list.size)
    }

    @Test
    fun inList_SubQuery_nonLambda() {
        val e = Meta.employee
        val a = Meta.address
        val subquery = QueryDsl.from(a)
            .where {
                e.addressId eq a.addressId
                e.employeeName like "%S%"
            }.select(a.addressId)
        val query =
            QueryDsl.from(e).where {
                e.addressId inList subquery
            }
        val list = db.runQuery { query }
        assertEquals(5, list.size)
    }

    @Test
    fun notInList_SubQuery() {
        val e = Meta.employee
        val a = Meta.address
        val query =
            QueryDsl.from(e).where {
                e.addressId notInList {
                    QueryDsl.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }.select(a.addressId)
                }
            }
        val list = db.runQuery { query }
        assertEquals(9, list.size)
    }

    @Test
    fun notInList_SubQuery_nonLambda() {
        val e = Meta.employee
        val a = Meta.address
        val subquery = QueryDsl.from(a).where {
            e.addressId eq a.addressId
            e.employeeName like "%S%"
        }.select(a.addressId)
        val query =
            QueryDsl.from(e).where {
                e.addressId notInList subquery
            }
        val list = db.runQuery { query }
        assertEquals(9, list.size)
    }

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun inList2() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId to a.version inList2 listOf(9 to 1, 10 to 1)
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

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun notInList2() {
        val seq = sequence {
            var i = 0
            while (++i < 10) yield(i to 1)
        }
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId to a.version notInList2 seq.toList()
            }.orderBy(a.addressId)
        }
        assertEquals((10..15).toList(), list.map { it.addressId })
    }

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun inList2_SubQuery() {
        val e = Meta.employee
        val a = Meta.address
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
        val list = db.runQuery { query }
        assertEquals(5, list.size)
    }

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun inList2_SubQuery_nonLambda() {
        val e = Meta.employee
        val a = Meta.address
        val subquery = QueryDsl.from(a)
            .where {
                e.addressId eq a.addressId
                e.employeeName like "%S%"
            }.select(a.addressId, a.version)
        val query =
            QueryDsl.from(e).where {
                e.addressId to e.version inList2 subquery
            }
        val list = db.runQuery { query }
        assertEquals(5, list.size)
    }

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun notInList_SubQuery2() {
        val e = Meta.employee
        val a = Meta.address
        val query =
            QueryDsl.from(e).where {
                e.addressId to e.version notInList2 {
                    QueryDsl.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }.select(a.addressId, a.version)
                }
            }
        val list = db.runQuery { query }
        assertEquals(9, list.size)
    }

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun notInList_SubQuery2_nonLambda() {
        val e = Meta.employee
        val a = Meta.address
        val subquery = QueryDsl.from(a).where {
            e.addressId eq a.addressId
            e.employeeName like "%S%"
        }.select(a.addressId, a.version)
        val query =
            QueryDsl.from(e).where {
                e.addressId to e.version notInList2 subquery
            }
        val list = db.runQuery { query }
        assertEquals(9, list.size)
    }

    @Test
    fun exists() {
        val e = Meta.employee
        val a = Meta.address
        val query =
            QueryDsl.from(e).where {
                exists {
                    QueryDsl.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }
                }
            }
        val list = db.runQuery { query }
        assertEquals(5, list.size)
    }

    @Test
    fun exists_nonLambda() {
        val e = Meta.employee
        val a = Meta.address
        val subquery = QueryDsl.from(a).where {
            e.addressId eq a.addressId
            e.employeeName like "%S%"
        }
        val query =
            QueryDsl.from(e).where {
                exists(subquery)
            }
        val list = db.runQuery { query }
        assertEquals(5, list.size)
    }

    @Test
    fun notExists() {
        val e = Meta.employee
        val a = Meta.address
        val query =
            QueryDsl.from(e).where {
                notExists {
                    QueryDsl.from(a).where {
                        e.addressId eq a.addressId
                        e.employeeName like "%S%"
                    }
                }
            }
        val list = db.runQuery { query }
        assertEquals(9, list.size)
    }

    @Test
    fun notExists_nonLambda() {
        val e = Meta.employee
        val a = Meta.address
        val subquery = QueryDsl.from(a).where {
            e.addressId eq a.addressId
            e.employeeName like "%S%"
        }
        val query =
            QueryDsl.from(e).where {
                notExists(subquery)
            }
        val list = db.runQuery { query }
        assertEquals(9, list.size)
    }

    @Test
    fun not() {
        val a = Meta.address
        val idList = db.runQuery {
            QueryDsl.from(a).where {
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
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
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
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where {
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
        val a = Meta.address
        val w1: WhereDeclaration = {
            a.addressId eq 1
        }
        val w2: WhereDeclaration = {
            a.version eq 1
        }
        val list = db.runQuery { QueryDsl.from(a).where(w1 + w2) }
        assertEquals(1, list.size)
    }
}
