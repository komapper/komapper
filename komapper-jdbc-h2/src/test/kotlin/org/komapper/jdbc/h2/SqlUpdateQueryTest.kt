package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery

@ExtendWith(Env::class)
class SqlUpdateQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.metamodel()
        val count = db.execute {
            SqlQuery.update(a).set {
                a.street set "STREET 16"
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.find(a) { a.addressId eq 1 }
        assertEquals("STREET 16", address.street)
    }
}
