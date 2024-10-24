package integration.r2dbc

import integration.core.Address
import integration.core.Man
import integration.core.Person
import integration.core.address
import integration.core.department
import integration.core.man
import integration.core.noVersionDepartment
import integration.core.person
import integration.core.robot
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.ClockProvider
import org.komapper.core.EntityNotFoundException
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.andThen
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcUpdateSingleTest(private val db: R2dbcDatabase) {
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = "NY street")
        db.runQuery { QueryDsl.update(a).single(newAddress) }
        val address2 = db.runQuery { query.firstOrNull() }
        assertEquals(
            Address(
                15,
                "NY street",
                2,
            ),
            address2,
        )
    }

    @Test
    fun embedded(info: TestInfo) = inTransaction(db, info) {
        val r = Meta.robot
        val count = db.runQuery {
            QueryDsl.update(r).set {
                r.info2.hiredate eq LocalDate.parse("2001-01-23")
            }.where {
                r.employeeId eq 1
            }
        }
        assertEquals(1, count)
        val robot = db.runQuery {
            QueryDsl.from(r).where {
                r.employeeId eq 1
            }.first()
        }
        assertEquals(LocalDate.parse("2001-01-23"), robot.info2?.hiredate)
    }

    @Test
    fun updatedAt_instant(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.man
        val findQuery = QueryDsl.from(p).where { p.manId eq 1 }.first()
        val person1 = Man(1, "ABC")
        val person2 = db.runQuery {
            QueryDsl.insert(p).single(person1).andThen(findQuery)
        }
        val person3 = db.runQuery {
            QueryDsl.update(p).single(person2.copy(name = "DEF")).andThen(findQuery)
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
    }

    @Test
    fun updatedAt_localDateTime(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        val findQuery = QueryDsl.from(p).where { p.personId eq 1 }.first()
        val person1 = Person(1, "ABC")
        val person2 = db.runQuery {
            QueryDsl.insert(p).single(person1).andThen(findQuery)
        }
        val person3 = db.runQuery {
            QueryDsl.update(p).single(person2.copy(name = "DEF")).andThen(findQuery)
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
    }

    @Test
    fun updatedAt_customize(info: TestInfo) = inTransaction(db, info) {
        val instant = Instant.parse("2021-01-01T00:00:00Z")
        val zoneId = ZoneId.of("UTC")

        val p = Meta.person
        val person1 = Person(1, "ABC")
        db.runQuery { QueryDsl.insert(p).single(person1) }
        val person2 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId eq 1
            }.first()
        }
        val config = object : R2dbcDatabaseConfig by db.config {
            override val clockProvider = ClockProvider {
                Clock.fixed(instant, zoneId)
            }
        }
        val myDb = R2dbcDatabase(config)
        myDb.runQuery { QueryDsl.update(p).single(person2.copy(name = "DEF")) }
        val person3 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId eq 1
            }.first()
        }
        assertEquals(LocalDateTime.ofInstant(instant, zoneId), person3.updatedAt)
    }

    @Test
    fun nonUpdatableColumn_updateSingle(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.man
        val findQuery = QueryDsl.from(p).where { p.manId eq 1 }.first()
        val person1 = Man(manId = 1, name = "Alice", createdBy = "nobody", updatedBy = "nobody")
        val person2 = db.runQuery {
            QueryDsl.insert(p).single(person1).andThen(findQuery)
        }
        val person3 = db.runQuery {
            QueryDsl.update(p).single(person2.copy(createdBy = "somebody", updatedBy = "somebody")).andThen(findQuery)
        }
        assertEquals("nobody", person3.createdBy)
        assertEquals("somebody", person3.updatedBy)
    }

    @Test
    fun uniqueConstraintException(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address = Address(1, "STREET 2", 1)
        assertFailsWith<UniqueConstraintException> {
            db.runQuery { QueryDsl.update(a).single(address) }.let { }
        }
    }

    @Test
    fun optimisticLockException(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 15 }.first() }
        db.runQuery { QueryDsl.update(a).single(address) }
        assertFailsWith<OptimisticLockException> {
            db.runQuery { QueryDsl.update(a).single(address) }.let {}
        }
    }

    @Test
    fun include(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val findQuery = QueryDsl.from(d).where { d.departmentId eq 1 }.first()
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        db.runQuery { QueryDsl.update(d).include(d.departmentName).single(department2) }
        val department3 = db.runQuery { findQuery }
        assertEquals("ABC", department3.departmentName)
        assertNotEquals("DEF", department3.location)
        assertTrue(department2.version < department3.version)
    }

    @Test
    fun include_emptyTargetProperties(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.noVersionDepartment
        val findQuery = QueryDsl.from(d).where { d.departmentId eq 1 }.first()
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        assertFailsWith<IllegalStateException> {
            db.runQuery { QueryDsl.update(d).include(d.departmentId).single(department2) }.let { }
        }
    }

    @Test
    fun exclude(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val findQuery = QueryDsl.from(d).where { d.departmentId eq 1 }.first()
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        db.runQuery { QueryDsl.update(d).exclude(d.location).single(department2) }
        val department3 = db.runQuery { findQuery }
        assertEquals("ABC", department3.departmentName)
        assertNotEquals("DEF", department3.location)
        assertTrue(department2.version < department3.version)
    }

    @Test
    fun exclude_emptyTargetProperties(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.noVersionDepartment
        val findQuery = QueryDsl.from(d).where { d.departmentId eq 1 }.first()
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.update(d)
                    .exclude(d.departmentName, d.location, d.version, d.departmentNo)
                    .single(department2)
            }.let { }
        }
    }

    @Test
    fun throwEntityNotFoundException(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        val person = Person(1, "aaa")
        val ex = assertFailsWith<EntityNotFoundException> {
            db.runQuery { QueryDsl.update(p).single(person) }
            Unit
        }
        println(ex)
    }

    @Test
    fun suppressEntityNotFoundException(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        val person = Person(1, "aaa")
        val result = db.runQuery {
            QueryDsl.update(p).single(person).options {
                it.copy(suppressEntityNotFoundException = true)
            }
        }
        assertNotNull(result)
    }
}
