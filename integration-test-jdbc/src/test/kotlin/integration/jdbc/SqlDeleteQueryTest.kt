package integration.jdbc

import integration.Address
import integration.Employee
import integration.meta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.jdbc.JdbcDatabase

@ExtendWith(Env::class)
class SqlDeleteQueryTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val a = Address.meta
        val count = db.runQuery {
            SqlDsl.delete(a).where { a.addressId eq 15 }
        }
        assertEquals(1, count)
    }

    @Test
    fun allowEmptyWhereClause_default() {
        val e = Employee.meta
        val ex = assertThrows<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                SqlDsl.delete(e)
            }
        }
        assertEquals("Empty where clause is not allowed.", ex.message)
    }

    @Test
    fun allowEmptyWhereClause_true() {
        val e = Employee.meta
        val count = db.runQuery {
            SqlDsl.delete(e).options { it.copy(allowEmptyWhereClause = true) }
        }
        assertEquals(14, count)
    }
}
