package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.concat
import org.komapper.core.dsl.execute
import org.komapper.core.dsl.plus

@ExtendWith(Env::class)
class SqlUpdateQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.alias
        val count = db.execute {
            SqlQuery.update(a).set {
                a.street set "STREET 16"
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.execute {
            SqlQuery.first(a) {
                a.addressId eq 1
            }
        }
        assertEquals("STREET 16", address.street)
    }

    @Test
    fun arithmetic_add() {
        val a = Address.alias
        val count = db.execute {
            SqlQuery.update(a).set {
                a.version set (a.version + 10)
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.execute {
            SqlQuery.first(a) {
                a.addressId eq 1
            }
        }
        assertEquals(11, address.version)
    }

    @Test
    fun string_concat() {
        val a = Address.alias
        val count = db.execute {
            SqlQuery.update(a).set {
                a.street set (concat(concat("[", a.street), "]"))
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.execute {
            SqlQuery.first(a) {
                a.addressId eq 1
            }
        }
        assertEquals("[STREET 1]", address.street)
    }

    @Test
    fun allowEmptyWhereClause_default() {
        val e = Employee.alias
        val ex = assertThrows<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.execute {
                SqlQuery.update(e).set {
                    e.employeeName set "ABC"
                }
            }
        }
        assertEquals("Empty where clause is not allowed.", ex.message)
    }

    @Test
    fun allowEmptyWhereClause_true() {
        val e = Employee.alias
        val count = db.execute {
            SqlQuery.update(e).set {
                e.employeeName set "ABC"
            }.option { it.copy(allowEmptyWhereClause = true) }
        }
        assertEquals(14, count)
    }
}
