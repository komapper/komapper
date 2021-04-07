package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.desc

@ExtendWith(Env::class)
class EntitySelectQueryTest(private val db: Database) {

    @Test
    fun list() {
        val a = Address.metamodel()
        val list: List<Address> = db.execute {
            EntityQuery.from(a).where { a.addressId eq 1 }
        }
        assertNotNull(list)
    }

    @Test
    fun first() {
        val a = Address.metamodel()
        val address: Address = db.execute {
            EntityQuery.from(a).where { a.addressId eq 1 }.first()
        }
        assertNotNull(address)
    }

    @Test
    fun firstOrNull() {
        val a = Address.metamodel()
        val address: Address? = db.execute {
            EntityQuery.from(a).where { a.addressId eq 99 }.firstOrNull()
        }
        assertNull(address)
    }

    @Test
    fun decoupling() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a)
            .where { a.addressId greaterEq 1 }
            .orderBy(a.addressId.desc())
            .limit(2)
            .offset(5)
        val list = db.execute { query }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1)
            ),
            list
        )
    }
}
