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
            SqlQuery.insert(a).values {
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
            SqlQuery.insert(a).values {
                a.id set 10
                a.value set "test"
            }
        }
        assertEquals(1, count)
        assertEquals(1, keys.size)
    }

    @Test
    fun select() {
        val a = Address.metamodel()
        val aa = Address.metamodel(table = "ADDRESS_ARCHIVE")
        val (count) = db.execute {
            SqlQuery.insert(aa).select {
                SqlQuery.from(a).where { a.addressId between 1..5 }
            }
        }
        assertEquals(5, count)
    }
}
