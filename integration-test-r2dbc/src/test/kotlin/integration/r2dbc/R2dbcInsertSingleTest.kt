package integration.r2dbc

import integration.core.Address
import integration.core.AddressId
import integration.core.Android
import integration.core.Dbms
import integration.core.Department
import integration.core.EmbeddedIdAddress
import integration.core.Human
import integration.core.IdentityStrategy
import integration.core.Machine
import integration.core.MachineInfo1
import integration.core.Man
import integration.core.MultiGenerated
import integration.core.Person
import integration.core.Robot
import integration.core.RobotInfo1
import integration.core.Run
import integration.core.SequenceStrategy
import integration.core.address
import integration.core.android
import integration.core.department
import integration.core.embeddedIdAddress
import integration.core.human
import integration.core.identityStrategy
import integration.core.machine
import integration.core.man
import integration.core.multiGenerated
import integration.core.person
import integration.core.robot
import integration.core.sequenceStrategy
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.ClockProvider
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@ExtendWith(R2dbcEnv::class)
class R2dbcInsertSingleTest(private val db: R2dbcDatabase) {
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        db.runQuery { QueryDsl.insert(a).single(address) }
        val address2 = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 16
            }.first()
        }
        assertEquals(address, address2)
    }

    @Test
    fun embeddedId(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.embeddedIdAddress
        val address = EmbeddedIdAddress(AddressId(16, 1), "STREET 16", 0)
        db.runQuery { QueryDsl.insert(a).single(address) }
        val address2 = db.runQuery {
            QueryDsl.from(a).where {
                a.id.addressId1 eq 16
                a.id.addressId2 eq 1
            }.first()
        }
        assertEquals(address, address2)
    }

    @Test
    fun embedded(info: TestInfo) = inTransaction(db, info) {
        val r = Meta.robot
        val robot = Robot(
            employeeId = 99,
            managerId = null,
            departmentId = 1,
            addressId = 1,
            version = 0,
            info1 = RobotInfo1(
                employeeNo = 9999,
                employeeName = "a",
            ),
            info2 = null,
        )
        db.runQuery { QueryDsl.insert(r).single(robot) }
        val robot2 = db.runQuery {
            QueryDsl.from(r).where {
                r.employeeId eq 99
            }.first()
        }
        assertEquals(robot, robot2)
    }

    @Test
    fun embedded_generics(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.android
        val android = Android(
            employeeId = 99,
            managerId = null,
            departmentId = 1,
            addressId = 1,
            version = 0,
            info1 = 9999 to "a",
            info2 = null,
        )
        db.runQuery { QueryDsl.insert(a).single(android) }
        val android2 = db.runQuery {
            QueryDsl.from(a).where {
                a.employeeId eq 99
            }.first()
        }
        assertEquals(android, android2)
    }

    @Test
    fun embedded_alternate(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.machine
        val robot = Machine(
            employeeId = 99,
            managerId = null,
            departmentId = 1,
            addressId = 1,
            version = 0,
            info1 = MachineInfo1(
                employeeNo = 9999,
                employeeName = "a",
            ),
            info2 = null,
        )
        db.runQuery { QueryDsl.insert(m).single(robot) }
        val robot2 = db.runQuery {
            QueryDsl.from(m).where {
                m.employeeId eq 99
            }.first()
        }
        assertEquals(robot, robot2)
    }

    @Test
    fun createdAt_instant(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.man
        val person1 = Man(1, "ABC")
        val id = db.runQuery { QueryDsl.insert(p).single(person1) }.manId
        val person2 = db.runQuery { QueryDsl.from(p).where { p.manId eq id }.first() }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.runQuery {
            QueryDsl.from(p).where {
                p.manId to 1
            }.first()
        }
        assertEquals(person2, person3)
    }

    @Test
    fun createdAt_localDateTime(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        val person1 = Person(1, "ABC")
        val id = db.runQuery { QueryDsl.insert(p).single(person1) }.personId
        val person2 = db.runQuery { QueryDsl.from(p).where { p.personId eq id }.first() }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId to 1
            }.first()
        }
        assertEquals(person2, person3)
    }

    @Run(unless = [Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun createdAt_offsetDateTime(info: TestInfo) = inTransaction(db, info) {
        val h = Meta.human
        val human1 = Human(1, "ABC")
        val id = db.runQuery { QueryDsl.insert(h).single(human1) }.humanId
        val human2 = db.runQuery { QueryDsl.from(h).where { h.humanId eq id }.first() }
        assertNotNull(human2.createdAt)
        assertNotNull(human2.updatedAt)
        assertEquals(human2.createdAt, human2.updatedAt)
        val human3 = db.runQuery {
            QueryDsl.from(h).where {
                h.humanId to 1
            }.first()
        }
        assertEquals(human2, human3)
    }

    @Test
    fun createdAt_customize(info: TestInfo) = inTransaction(db, info) {
        val instant = Instant.parse("2021-01-01T00:00:00Z")
        val zoneId = ZoneId.of("UTC")

        val p = Meta.person
        val config = object : R2dbcDatabaseConfig by db.config {
            override val clockProvider = ClockProvider {
                Clock.fixed(instant, zoneId)
            }
        }
        val myDb = R2dbcDatabase(config)
        val person1 = Person(1, "ABC")
        val id = myDb.runQuery { QueryDsl.insert(p).single(person1) }
        val person2 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId to id
            }.first()
        }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        assertEquals(LocalDateTime.ofInstant(instant, zoneId), person2.createdAt)
    }

    @Test
    fun uniqueConstraintException(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address = Address(1, "STREET 1", 0)
        assertFailsWith<UniqueConstraintException> {
            db.runQuery { QueryDsl.insert(a).single(address) }.let { }
        }
    }

    @Test
    fun identityGenerator(info: TestInfo) = inTransaction(db, info) {
        for (i in 1..201) {
            val m = Meta.identityStrategy
            val strategy = IdentityStrategy(0, "test")
            val result = db.runQuery { QueryDsl.insert(m).single(strategy) }
            assertEquals(i, result.id)
        }
    }

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5])
    @Test
    fun sequenceGenerator(info: TestInfo) = inTransaction(db, info) {
        val generator = Meta.sequenceStrategy.idGenerator() as IdGenerator.Sequence<*, *>
        generator.clear()

        for (i in 1..201) {
            val m = Meta.sequenceStrategy
            val strategy = SequenceStrategy(0, "test")
            val result = db.runQuery { QueryDsl.insert(m).single(strategy) }
            assertEquals(i, result.id)
        }
    }

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5])
    @Test
    fun sequenceGenerator_disableSequenceAssignment(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.sequenceStrategy
        val strategy = SequenceStrategy(50, "test")
        val result = db.runQuery {
            QueryDsl.insert(m).single(strategy).options {
                it.copy(disableSequenceAssignment = true)
            }
        }
        assertEquals(50, result.id)
    }

    @Test
    fun onDuplicateKeyUpdate_insert(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().single(department)
        db.runQuery { query }
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentId eq 5 }.first() }
        assertNotNull(found)
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys_insert(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).single(department)
        db.runQuery { query }
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentId eq 5 }.first() }
        assertNotNull(found)
    }

    @Test
    fun onDuplicateKeyUpdate_update(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department = Department(1, 50, "PLANNING", "TOKYO", 10)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().single(department)
        db.runQuery { query }
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentId eq 1 }.first() }
        assertEquals(50, found.departmentNo)
        assertEquals("PLANNING", found.departmentName)
        assertEquals("TOKYO", found.location)
        assertEquals(10, found.version)
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys_update(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department = Department(6, 10, "PLANNING", "TOKYO", 10)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).single(department)
        db.runQuery { query }
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentNo eq 10 }.first() }
        assertEquals(1, found.departmentId)
        assertEquals(10, found.departmentNo)
        assertEquals("PLANNING", found.departmentName)
        assertEquals("TOKYO", found.location)
        assertEquals(10, found.version)
    }

    @Test
    fun onDuplicateKeyUpdate_nonUpdatableColumn(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.man
        val person1 = Man(manId = 1, name = "Alice", createdBy = "nobody", updatedBy = "nobody")
        db.runQuery {
            QueryDsl.insert(p).onDuplicateKeyUpdate().single(person1)
        }
        val person2 = person1.copy(createdBy = "somebody", updatedBy = "somebody")
        db.runQuery {
            QueryDsl.insert(p).onDuplicateKeyUpdate().single(person2)
        }
        val person3 = db.runQuery { QueryDsl.from(p).where { p.manId eq 1 }.first() }

        assertEquals("nobody", person3.createdBy)
        assertEquals("somebody", person3.updatedBy)
    }

    @Test
    fun onDuplicateKeyUpdate_update_set(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department = Department(1, 50, "PLANNING", "TOKYO", 10)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().set { excluded ->
            d.departmentName eq "PLANNING2"
            d.location eq concat(d.location, concat("_", excluded.location))
        }.single(department)
        db.runQuery { query }
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentId eq 1 }.first() }
        assertEquals(10, found.departmentNo)
        assertEquals("PLANNING2", found.departmentName)
        assertEquals("NEW YORK_TOKYO", found.location)
        assertEquals(1, found.version)
    }

    @Test
    fun onDuplicateKeyUpdateWithKey_update_set(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department = Department(5, 10, "PLANNING", "TOKYO", 10)
        val query = QueryDsl.insert(d)
            .onDuplicateKeyUpdate(d.departmentNo)
            .set { excluded ->
                d.departmentName eq "PLANNING2"
                d.location eq concat(d.location, concat("_", excluded.location))
            }.single(department)
        db.runQuery { query }
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentNo eq 10 }.first() }
        assertEquals(1, found.departmentId)
        assertEquals("PLANNING2", found.departmentName)
        assertEquals("NEW YORK_TOKYO", found.location)
        assertEquals(1, found.version)
    }

    @Test
    fun onDuplicateKeyIgnore_inserted(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore().single(address)
        val count = db.runQuery { query }
        assertEquals(1, count)
    }

    @Test
    fun onDuplicateKeyIgnore_ignored(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address = Address(1, "STREET 100", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore().single(address)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Test
    fun onDuplicateKeyIgnoreWithKey_ignored(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address = Address(100, "STREET 1", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore(a.street).single(address)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Test
    fun multipleGeneratedColumns(info: TestInfo) = inTransaction(db, info) {
        db.runQuery { QueryDsl.insert(Meta.multiGenerated).single(MultiGenerated()) }
    }
}
