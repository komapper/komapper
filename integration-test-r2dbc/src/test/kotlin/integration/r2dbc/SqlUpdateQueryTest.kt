package integration.r2dbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.concat
import org.komapper.core.dsl.plus
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcSqlDsl

@ExtendWith(Env::class)
class SqlUpdateQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun test() = inTransaction(db) {
        val a = Address.meta
        val count = db.runQuery {
            R2dbcSqlDsl.update(a).set {
                a.street set "STREET 16"
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            R2dbcSqlDsl.from(a).first {
                a.addressId eq 1
            }
        }
        assertEquals("STREET 16", address.street)
    }

    @Test
    fun setIfNotNull() = inTransaction(db) {
        val a = Address.meta
        val count = db.runQuery {
            R2dbcSqlDsl.update(a).set {
                a.street setIfNotNull null
                a.version set 10
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            R2dbcSqlDsl.from(a).first {
                a.addressId eq 1
            }
        }
        assertEquals("STREET 1", address.street)
        assertEquals(10, address.version)
    }

    @Test
    fun arithmetic_add() = inTransaction(db) {
        val a = Address.meta
        val count = db.runQuery {
            R2dbcSqlDsl.update(a).set {
                a.version set (a.version + 10)
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            R2dbcSqlDsl.from(a).first {
                a.addressId eq 1
            }
        }
        assertEquals(11, address.version)
    }

    @Test
    fun string_concat() = inTransaction(db) {
        val a = Address.meta
        val count = db.runQuery {
            R2dbcSqlDsl.update(a).set {
                a.street set (concat(concat("[", a.street), "]"))
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            R2dbcSqlDsl.from(a).first {
                a.addressId eq 1
            }
        }
        assertEquals("[STREET 1]", address.street)
    }

    @Test
    fun allowEmptyWhereClause_default() = inTransaction(db) {
        val e = Employee.meta
        val ex = assertThrows<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                R2dbcSqlDsl.update(e).set {
                    e.employeeName set "ABC"
                }
            }
        }
        assertEquals("Empty where clause is not allowed.", ex.message)
    }

    @Test
    fun allowEmptyWhereClause_true() = inTransaction(db) {
        val e = Employee.meta
        val count = db.runQuery {
            R2dbcSqlDsl.update(e).set {
                e.employeeName set "ABC"
            }.option { it.copy(allowEmptyWhereClause = true) }
        }
        assertEquals(14, count)
    }
}
