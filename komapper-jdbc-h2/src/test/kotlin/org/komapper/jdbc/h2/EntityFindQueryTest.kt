package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.EntityQuery

@ExtendWith(Env::class)
class EntityFindQueryTest(private val db: Database) {

    @Test
    fun first() {
        val a = Address.metamodel()
        val address = db.execute { EntityQuery.first(a).where { a.addressId eq 1 } }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() {
        val a = Address.metamodel()
        val address = db.execute { EntityQuery.firstOrNull(a).where { a.addressId eq -1 } }
        assertNull(address)
    }

    @Test
    fun first_multipleCondition() {
        val a = Address.metamodel()
        val address = db.execute {
            EntityQuery.first(a).where { a.addressId eq 1; a.version eq 1 }
        }
        assertNotNull(address)
    }
}
