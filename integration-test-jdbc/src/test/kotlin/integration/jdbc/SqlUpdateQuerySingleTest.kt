package integration.jdbc

import integration.Address
import integration.Department
import integration.NoVersionDepartment
import integration.Person
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.ClockProvider
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.plus
import org.komapper.jdbc.JdbcDatabase
import org.komapper.jdbc.JdbcDatabaseConfig
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(Env::class)
class SqlUpdateQuerySingleTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val a = Address.meta
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = "NY street")
        db.runQuery { QueryDsl.update(a).single(newAddress) }
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
        val p = Person.meta
        val findQuery = QueryDsl.from(p).where { p.personId eq 1 }.first()
        val person1 = Person(1, "ABC")
        val person2 = db.runQuery {
            QueryDsl.insert(p).single(person1) + findQuery
        }
        val person3 = db.runQuery {
            QueryDsl.update(p).single(person2.copy(name = "DEF")) + findQuery
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
    }

    @Test
    fun updatedAt_customize() {
        val instant = Instant.parse("2021-01-01T00:00:00Z")
        val zoneId = ZoneId.of("UTC")

        val p = Person.meta
        val person1 = Person(1, "ABC")
        db.runQuery { QueryDsl.insert(p).single(person1) }
        val person2 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId eq 1
            }.first()
        }
        val config = object : JdbcDatabaseConfig by db.config {
            override val clockProvider = ClockProvider {
                Clock.fixed(instant, zoneId)
            }
        }
        val myDb = JdbcDatabase.create(config)
        myDb.runQuery { QueryDsl.update(p).single(person2.copy(name = "DEF")) }
        val person3 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId eq 1
            }.first()
        }
        assertEquals(LocalDateTime.ofInstant(instant, zoneId), person3.updatedAt)
    }

    @Test
    fun uniqueConstraintException() {
        val a = Address.meta
        val address = Address(1, "STREET 2", 1)
        assertFailsWith<UniqueConstraintException> {
            db.runQuery { QueryDsl.update(a).single(address) }.let { }
        }
    }

    @Test
    fun optimisticLockException() {
        val a = Address.meta
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 15 }.first() }
        db.runQuery { QueryDsl.update(a).single(address) }
        assertFailsWith<OptimisticLockException> {
            db.runQuery { QueryDsl.update(a).single(address) }.let {}
        }
    }

    @Test
    fun include() {
        val d = Department.meta
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
    fun include_emptyTargetProperties() {
        val d = NoVersionDepartment.meta
        val findQuery = QueryDsl.from(d).where { d.departmentId eq 1 }.first()
        val department = db.runQuery { findQuery }
        val department2 = department.copy(departmentName = "ABC", location = "DEF")
        assertFailsWith<IllegalStateException> {
            db.runQuery { QueryDsl.update(d).include(d.departmentId).single(department2) }.let { }
        }
    }

    @Test
    fun exclude() {
        val d = Department.meta
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
    fun exclude_emptyTargetProperties() {
        val d = NoVersionDepartment.meta
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
}
