package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class SqlDeleteQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.alias
        val count = db.execute {
            SqlQuery.delete(a).where { a.addressId eq 15 }
        }
        assertEquals(1, count)
    }

    @Test
    fun allowEmptyWhereClause_default() {
        val e = Employee.alias
        val ex = assertThrows<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.execute {
                SqlQuery.delete(e)
            }
        }
        assertEquals("Empty where clause is not allowed.", ex.message)
    }

    @Test
    fun allowEmptyWhereClause_true() {
        val e = Employee.alias
        val count = db.execute {
            SqlQuery.delete(e).option { it.copy(allowEmptyWhereClause = true) }
        }
        assertEquals(14, count)
    }
}
