package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class EntityDeleteQueryTest(private val db: Database) {

    @Test
    fun optimisticLockException() {
        val a = Address.metamodel()
        val address = db.execute {
            EntityQuery.first(a).where {
                a.addressId eq 15
            }
        }
        db.execute { EntityQuery.delete(a, address) }
        assertThrows<OptimisticLockException> {
            db.execute { EntityQuery.delete(a, address) }
        }
    }

    @Test
    fun testEntity() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where { a.addressId eq 15 }
        val address = db.execute { query.first() }
        db.execute { EntityQuery.delete(a, address) }
        assertEquals(emptyList<Address>(), db.execute { query })
    }
}
