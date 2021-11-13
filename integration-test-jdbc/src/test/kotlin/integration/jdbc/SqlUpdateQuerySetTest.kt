package integration.jdbc

import integration.Address
import integration.Employee
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(Env::class)
class SqlUpdateQuerySetTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val a = Address.meta
        val count = db.runQuery {
            QueryDsl.update(a).set {
                a.street set "STREET 16"
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals("STREET 16", address.street)
    }

    @Test
    fun setIfNotNull() {
        val a = Address.meta
        val count = db.runQuery {
            QueryDsl.update(a).set {
                a.street setIfNotNull null
                a.version set 10
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals("STREET 1", address.street)
        assertEquals(10, address.version)
    }

    @Test
    fun arithmetic_add() {
        val a = Address.meta
        val count = db.runQuery {
            QueryDsl.update(a).set {
                a.version set (a.version + 10)
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals(11, address.version)
    }

    @Test
    fun string_concat() {
        val a = Address.meta
        val count = db.runQuery {
            QueryDsl.update(a).set {
                a.street set (concat(concat("[", a.street), "]"))
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals("[STREET 1]", address.street)
    }

    @Test
    fun allowEmptyWhereClause_default() {
        val e = Employee.meta
        val ex = assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                QueryDsl.update(e).set {
                    e.employeeName set "ABC"
                }
            }
        }
        assertEquals("Empty where clause is not allowed.", ex.message)
    }

    @Test
    fun allowEmptyWhereClause_true() {
        val e = Employee.meta
        val count = db.runQuery {
            QueryDsl.update(e).set {
                e.employeeName set "ABC"
            }.options { it.copy(allowEmptyWhereClause = true) }
        }
        assertEquals(14, count)
    }
}
