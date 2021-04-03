package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.SqlQuery

@ExtendWith(Env::class)
class SqlFindQueryTest(private val db: Database) {

    @Test
    fun first() {
        val a = Address.metamodel()
        val address = db.execute { SqlQuery.first(a).where { a.addressId eq 1 } }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() {
        val a = Address.metamodel()
        val address = db.execute { SqlQuery.firstOrNull(a).where { a.addressId eq -1 } }
        assertNull(address)
    }

    @Test
    fun first_multipleCondition() {
        val a = Address.metamodel()
        val address = db.execute {
            SqlQuery.first(a).where { a.addressId eq 1; a.version eq 1 }
        }
        assertNotNull(address)
    }
}
