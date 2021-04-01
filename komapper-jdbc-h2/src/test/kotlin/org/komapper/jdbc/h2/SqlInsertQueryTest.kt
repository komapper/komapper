package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery

@ExtendWith(Env::class)
class SqlInsertQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.metamodel()
        val (count, keys) = db.execute {
            SqlQuery.insert(a) {
                a.addressId set 19
                a.street set "STREET 16"
                a.version set 0
            }
        }
        assertEquals(1, count)
        assertEquals(0, keys.size)
    }

    @Test
    fun generatedKeys() {
        val a = IdentityStrategy.metamodel()
        val (count, keys) = db.execute {
            SqlQuery.insert(a) {
                a.id set 10
                a.value set "test"
            }
        }
        assertEquals(1, count)
        assertEquals(1, keys.size)
    }
}
