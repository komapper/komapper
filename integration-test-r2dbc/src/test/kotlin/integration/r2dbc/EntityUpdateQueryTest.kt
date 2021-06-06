package integration.r2dbc

import integration.Address
import integration.Department
import integration.NoVersionDepartment
import integration.Person
import integration.meta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.ClockProvider
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityDsl
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(Env::class)
class EntityUpdateQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun test() = inTransaction(db) {
        val a = Address.meta
        val query = EntityDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = "NY street")
        db.runQuery { EntityDsl.update(a).single(newAddress) }
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
    fun updatedAt() = inTransaction(db) {
        val p = Person.meta
        val findQuery = EntityDsl.from(p).where { p.personId eq 1 }.first()
        val person1 = Person(1, "ABC")
        val person2 = db.runQuery {
            EntityDsl.insert(p).single(person1) + findQuery
        }
        val person3 = db.runQuery {
            EntityDsl.update(p).single(person2.copy(name = "DEF")) + findQuery
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
    }

    @Test
    fun updatedAt_customize() = inTransaction(db) {
        val instant = Instant.parse("2021-01-01T00:00:00Z")
        val zoneId = ZoneId.of("UTC")

        val p = Person.meta
        val person1 = Person(1, "ABC")
        db.runQuery { EntityDsl.insert(p).single(person1) }
        val person2 = db.runQuery {
            EntityDsl.from(p).where {
                p.personId eq 1
            }.first()
        }
        val config = object : R2dbcDatabaseConfig by db.config {
            override val clockProvider = ClockProvider {
                Clock.fixed(instant, zoneId)
            }
        }
        val myDb = R2dbcDatabase.create(config)
        myDb.runQuery { EntityDsl.update(p).single(person2.copy(name = "DEF")) }
        val person3 = db.runQuery {
            EntityDsl.from(p).where {
                p.personId eq 1
            }.first()
        }
        assertEquals(LocalDateTime.ofInstant(instant, zoneId), person3.updatedAt)
    }

    @Test
    fun uniqueConstraintException() = inTransaction(db) {
        val a = Address.meta
        val address = Address(1, "STREET 2", 1)
        assertThrows<UniqueConstraintException> {
            db.runQuery { EntityDsl.update(a).single(address) }.let { }
        }
    }

    @Test
    fun optimisticLockException() = inTransaction(db) {
        val a = Address.meta
        val address = db.runQuery { EntityDsl.from(a).where { a.addressId eq 15 }.first() }
        db.runQuery { EntityDsl.update(a).single(address) }
        assertThrows<OptimisticLockException> {
            db.runQuery { EntityDsl.update(a).single(address) }.let {}
        }
    }

    @Test
    fun include() = inTransaction(db) {
        val d = Department.meta
        val findQuery = EntityDsl.from(d).where { d.departmentId eq 1 }.first()
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        db.runQuery { EntityDsl.update(d).include(d.departmentName).single(department2) }
        val department3 = db.runQuery { findQuery }
        assertEquals("ABC", department3.departmentName)
        assertNotEquals("DEF", department3.location)
        assertTrue(department2.version < department3.version)
    }

    @Test
    fun include_emptyTargetProperties() = inTransaction(db) {
        val d = NoVersionDepartment.meta
        val findQuery = EntityDsl.from(d).where { d.departmentId eq 1 }.first()
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        assertThrows<IllegalStateException> {
            db.runQuery { EntityDsl.update(d).include(d.departmentId).single(department2) }.let { }
        }
    }

    @Test
    fun exclude() = inTransaction(db) {
        val d = Department.meta
        val findQuery = EntityDsl.from(d).where { d.departmentId eq 1 }.first()
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        db.runQuery { EntityDsl.update(d).exclude(d.location).single(department2) }
        val department3 = db.runQuery { findQuery }
        assertEquals("ABC", department3.departmentName)
        assertNotEquals("DEF", department3.location)
        assertTrue(department2.version < department3.version)
    }

    @Test
    fun exclude_emptyTargetProperties() = inTransaction(db) {
        val d = NoVersionDepartment.meta
        val findQuery = EntityDsl.from(d).where { d.departmentId eq 1 }.first()
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        assertThrows<IllegalStateException> {
            db.runQuery {
                EntityDsl.update(d)
                    .exclude(d.departmentName, d.location, d.version, d.departmentNo)
                    .single(department2)
            }.let { }
        }
    }
}
