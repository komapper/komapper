package org.komapper.core.query

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.core.DefaultDatabaseConfig

internal class WhereTest {

    private val config = DefaultDatabaseConfig(EmptyDialect(), "")

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
            query.toSql(config)
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
            query.toSql(config)
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
            query.toSql(config)
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
            query.toSql(config)
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
            query.toSql(config)
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
            query.toSql(config)
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
            query.toSql(config)
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
            query.toSql(config)
        )
    }
}
