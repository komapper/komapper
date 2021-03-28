package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery

@ExtendWith(Env::class)
class SqlDeleteQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.metamodel()
        val count = db.execute {
            SqlQuery.delete(a).where { a.addressId eq 15 }
        }
        assertEquals(1, count)
    }
}
