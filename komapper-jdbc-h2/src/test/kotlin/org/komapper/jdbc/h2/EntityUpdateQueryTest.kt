package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.ClockProvider
import org.komapper.core.Database
import org.komapper.core.DatabaseConfig
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityQuery
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(Env::class)
class EntityUpdateQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where { a.addressId eq 15 }
        val address = db.execute { query.first() }
        val newAddress = address.copy(street = "NY street")
        db.execute { EntityQuery.update(a, newAddress) }
        val address2 = db.execute { query.firstOrNull() }
        assertEquals(
            Address(
                15,
                "NY street",
                2
            ),
            address2
        )
    }

    @Test
    fun updatedAt() {
        val p = Person.metamodel()
        val person1 = Person(1, "ABC")
        db.execute { EntityQuery.insert(p, person1) }
        val person2 = db.execute {
            EntityQuery.first(p).where {
                p.personId eq 1
            }
        }
        val person3 = db.execute { EntityQuery.update(p, person2.copy(name = "DEF")) }
        val person4 = db.execute {
            EntityQuery.first(p).where {
                p.personId eq 1
            }
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
        assertNotNull(person4.updatedAt)
        assertNotEquals(person2.updatedAt, person4.updatedAt)
        assertEquals(person3.updatedAt, person4.updatedAt)
    }

    @Test
    fun updatedAt_customize() {
        val instant = Instant.parse("2021-01-01T00:00:00Z")
        val zoneId = ZoneId.of("UTC")

        val p = Person.metamodel()
        val person1 = Person(1, "ABC")
        db.execute { EntityQuery.insert(p, person1) }
        val person2 = db.execute {
            EntityQuery.first(p).where {
                p.personId eq 1
            }
        }
        val config = object : DatabaseConfig by db.config {
            override val clockProvider = ClockProvider {
                Clock.fixed(instant, zoneId)
            }
        }
        val myDb = Database(config)
        val person3 = myDb.execute { EntityQuery.update(p, person2.copy(name = "DEF")) }
        val person4 = db.execute {
            EntityQuery.first(p).where {
                p.personId eq 1
            }
        }
        assertEquals(person3.updatedAt, person4.updatedAt)
        assertEquals(LocalDateTime.ofInstant(instant, zoneId), person4.updatedAt)
    }

    @Test
    fun uniqueConstraintException() {
        val a = Address.metamodel()
        val address = Address(1, "STREET 2", 1)
        assertThrows<UniqueConstraintException> {
            db.execute { EntityQuery.update(a, address) }.let { }
        }
    }

    @Test
    fun optimisticLockException() {
        val a = Address.metamodel()
        val address = db.execute { EntityQuery.from(a).where { a.addressId eq 15 }.first() }
        db.execute { EntityQuery.update(a, address) }
        assertThrows<OptimisticLockException> {
            db.execute { EntityQuery.update(a, address) }.let {}
        }
    }

    @Test
    fun criteria() {
        val a = Address.metamodel()
        val selectQuery = EntityQuery.from(a).where { a.addressId eq 15 }.first()
        val address1 = db.execute { selectQuery }.copy(street = "new street")
        val address2 = db.execute { EntityQuery.update(a, address1) }
        val address3 = db.execute { selectQuery }
        assertEquals(Address(15, "new street", 2), address2)
        assertEquals(address2, address3)
    }
}
