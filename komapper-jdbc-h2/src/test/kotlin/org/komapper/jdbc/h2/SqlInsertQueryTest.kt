package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery

@ExtendWith(Env::class)
class SqlInsertQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.metamodel()
        val (count, id) = db.execute {
            SqlQuery.insert(a).values {
                a.addressId set 19
                a.street set "STREET 16"
                a.version set 0
            }
        }
        assertEquals(1, count)
        assertNull(id)
    }

    @Test
    fun generatedKeys() {
        val a = IdentityStrategy.metamodel()
        val (count, id) = db.execute {
            SqlQuery.insert(a).values {
                a.id set 10
                a.value set "test"
            }
        }
        assertEquals(1, count)
        assertNotNull(id)
    }
}
