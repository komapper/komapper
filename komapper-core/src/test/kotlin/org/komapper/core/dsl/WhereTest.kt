package org.komapper.core.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.EntityQuery
import org.komapper.core.Subquery

internal class WhereTest {

    @Test
    fun eq() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.id eq 1
            a.id eq a.version
            1 eq a.id
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID = ? and t0_.ID = t0_.VERSION and ? = t0_.ID",
            query.peek().sql
        )
    }

    @Test
    fun eq_null() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.id eq null
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_",
            query.peek().sql
        )
    }

    @Test
    fun notEq() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.id notEq 1
            a.id notEq a.version
            1 notEq a.id
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID <> ? and t0_.ID <> t0_.VERSION and ? <> t0_.ID",
            query.peek().sql
        )
    }

    @Test
    fun isNull() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.street.isNull()
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.STREET is null",
            query.peek().sql
        )
    }

    @Test
    fun isNotNull() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.street.isNotNull()
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.STREET is not null",
            query.peek().sql
        )
    }

    @Test
    fun like() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.street like "%abc"
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.STREET like '%abc'",
            query.peek().log
        )
    }

    @Test
    fun like_escape() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.street like "%abc".escape()
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.STREET like '\\%abc'",
            query.peek().log
        )
    }

    @Test
    fun like_prefix() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.street like "ab%c".asPrefix()
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.STREET like 'ab\\%c%'",
            query.peek().log
        )
    }

    @Test
    fun like_infix() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.street like "ab%c".asInfix()
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.STREET like '%ab\\%c%'",
            query.peek().log
        )
    }

    @Test
    fun like_suffix() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.street like "ab%c".asSuffix()
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.STREET like '%ab\\%c'",
            query.peek().log
        )
    }

    @Test
    fun notLike() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.street notLike "%abc"
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.STREET not like '%abc'",
            query.peek().log
        )
    }

    @Test
    fun between() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.id between 1..3
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID between ? and ?",
            query.peek().sql
        )
    }

    @Test
    fun notBetween() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.id notBetween 1..3
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID not between ? and ?",
            query.peek().sql
        )
    }

    @Test
    fun inList() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.id inList listOf(1, 2, 3)
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID in (?, ?, ?)",
            query.peek().sql
        )
    }

    @Test
    fun inList_subQuery() {
        val a = Address.metamodel()
        val e = Emp.metamodel()
        val query = EntityQuery.from(a).where {
            a.id inList {
                Subquery.from(e).select(e.id)
            }
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID in (select t1_.ID from EMP t1_)",
            query.peek().sql
        )
    }

    @Test
    fun notInList() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.id notInList listOf(1, 2, 3)
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID not in (?, ?, ?)",
            query.peek().sql
        )
    }

    @Test
    fun exists() {
        val a = Address.metamodel()
        val e = Emp.metamodel()
        val query = EntityQuery.from(a).where {
            exists {
                Subquery.from(e).where { a.id eq e.addressId }.select(e.id)
            }
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where exists (select t1_.ID from EMP t1_ where t0_.ID = t1_.ADDRESS_ID)",
            query.peek().sql
        )
    }

    @Test
    fun notExists() {
        val a = Address.metamodel()
        val e = Emp.metamodel()
        val query = EntityQuery.from(a).where {
            notExists {
                Subquery.from(e).where {
                    a.id eq e.addressId
                }.select(e.id)
            }
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where not exists (select t1_.ID from EMP t1_ where t0_.ID = t1_.ADDRESS_ID)",
            query.peek().sql
        )
    }

    @Test
    fun and() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.id eq 1
            and {
                a.street eq "a"
                a.version eq 1
            }
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID = ? and (t0_.STREET = ? and t0_.VERSION = ?)",
            query.peek().sql
        )
    }

    @Test
    fun or() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.id eq 1
            or {
                a.street eq "a"
                a.version eq 1
            }
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID = ? or (t0_.STREET = ? and t0_.VERSION = ?)",
            query.peek().sql
        )
    }

    @Test
    fun not() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where {
            a.id eq 1
            not {
                a.street eq "a"
                a.version eq 1
            }
        }
        assertEquals(
            "select t0_.ID, t0_.STREET, t0_.VERSION from ADDRESS t0_ where t0_.ID = ? and not (t0_.STREET = ? and t0_.VERSION = ?)",
            query.peek().sql
        )
    }
}
