package integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.ClockProvider
import org.komapper.core.Database
import org.komapper.core.DatabaseConfig
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.plus
import org.komapper.core.dsl.runQuery
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(Env::class)
class EntityUpdateQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.alias
        val query = EntityQuery.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = "NY street")
        db.runQuery { EntityQuery.update(a, newAddress) }
        val address2 = db.runQuery { query.firstOrNull() }
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
        val p = Person.alias
        val findQuery = EntityQuery.first(p) { p.personId eq 1 }
        val person1 = Person(1, "ABC")
        val person2 = db.runQuery {
            EntityQuery.insert(p, person1) + findQuery
        }
        val person3 = db.runQuery {
            EntityQuery.update(p, person2.copy(name = "DEF")) + findQuery
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
        assertNotEquals(person2.updatedAt, person3.updatedAt)
    }

    @Test
    fun updatedAt_customize() {
        val instant = Instant.parse("2021-01-01T00:00:00Z")
        val zoneId = ZoneId.of("UTC")

        val p = Person.alias
        val person1 = Person(1, "ABC")
        db.runQuery { EntityQuery.insert(p, person1) }
        val person2 = db.runQuery {
            EntityQuery.first(p) {
                p.personId eq 1
            }
        }
        val config = object : DatabaseConfig by db.config {
            override val clockProvider = ClockProvider {
                Clock.fixed(instant, zoneId)
            }
        }
        val myDb = Database(config)
        myDb.runQuery { EntityQuery.update(p, person2.copy(name = "DEF")) }
        val person3 = db.runQuery {
            EntityQuery.first(p) {
                p.personId eq 1
            }
        }
        assertEquals(LocalDateTime.ofInstant(instant, zoneId), person3.updatedAt)
    }

    @Test
    fun uniqueConstraintException() {
        val a = Address.alias
        val address = Address(1, "STREET 2", 1)
        assertThrows<UniqueConstraintException> {
            db.runQuery { EntityQuery.update(a, address) }.let { }
        }
    }

    @Test
    fun optimisticLockException() {
        val a = Address.alias
        val address = db.runQuery { EntityQuery.from(a).where { a.addressId eq 15 }.first() }
        db.runQuery { EntityQuery.update(a, address) }
        assertThrows<OptimisticLockException> {
            db.runQuery { EntityQuery.update(a, address) }.let {}
        }
    }

    @Test
    fun include() {
        val d = Department.alias
        val findQuery = EntityQuery.first(d) { d.departmentId eq 1 }
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        db.runQuery { EntityQuery.update(d, department2).include(d.departmentName) }
        val department3 = db.runQuery { findQuery }
        assertEquals("ABC", department3.departmentName)
        assertNotEquals("DEF", department3.location)
        assertTrue(department2.version < department3.version)
    }

    @Test
    fun include_emptyTargetProperties() {
        val d = NoVersionDepartment.alias
        val findQuery = EntityQuery.first(d) { d.departmentId eq 1 }
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        assertThrows<IllegalStateException> {
            db.runQuery { EntityQuery.update(d, department2).include(d.departmentId) }
        }
    }

    @Test
    fun exclude() {
        val d = Department.alias
        val findQuery = EntityQuery.first(d) { d.departmentId eq 1 }
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        db.runQuery { EntityQuery.update(d, department2).exclude(d.location) }
        val department3 = db.runQuery { findQuery }
        assertEquals("ABC", department3.departmentName)
        assertNotEquals("DEF", department3.location)
        assertTrue(department2.version < department3.version)
    }

    @Test
    fun exclude_emptyTargetProperties() {
        val d = NoVersionDepartment.alias
        val findQuery = EntityQuery.first(d) { d.departmentId eq 1 }
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        assertThrows<IllegalStateException> {
            db.runQuery {
                EntityQuery.update(d, department2).exclude(d.departmentName, d.location, d.version, d.departmentNo)
            }
        }
    }
}
