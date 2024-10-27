package integration.jdbc

import integration.core.Address
import integration.core.AddressId
import integration.core.Android
import integration.core.Cyborg
import integration.core.Dbms
import integration.core.Department
import integration.core.EmbeddedIdAddress
import integration.core.GenericEmbeddedIdAddress
import integration.core.Human
import integration.core.IdentityStrategy
import integration.core.Machine
import integration.core.MachineInfo1
import integration.core.Man
import integration.core.Name
import integration.core.Person
import integration.core.Robot
import integration.core.RobotInfo1
import integration.core.Run
import integration.core.SequenceStrategy
import integration.core.address
import integration.core.android
import integration.core.cyborg
import integration.core.department
import integration.core.embeddedIdAddress
import integration.core.genericEmbeddedIdAddress
import integration.core.human
import integration.core.identityStrategy
import integration.core.machine
import integration.core.man
import integration.core.name
import integration.core.person
import integration.core.robot
import integration.core.sequenceStrategy
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.ClockProvider
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.query.first
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

@ExtendWith(JdbcEnv::class)
class JdbcInsertSingleTest(private val db: JdbcDatabase) {
    @Test
    fun test() {
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
    fun embeddedId() {
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
    fun generic_embeddedId() {
        val a = Meta.genericEmbeddedIdAddress
        val address = GenericEmbeddedIdAddress(16 to 1, "STREET 16", 0)
        db.runQuery { QueryDsl.insert(a).single(address) }
        val address2 = db.runQuery {
            QueryDsl.from(a).where {
                a.id.first eq 16
                a.id.second eq 1
            }.first()
        }
        assertEquals(address, address2)
    }

    @Test
    fun embedded() {
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
    fun embedded_generics() {
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
    fun embedded_generics_separation_mapping() {
        val c = Meta.cyborg
        val cyborg = Cyborg(
            employeeId = 99,
            managerId = null,
            departmentId = 1,
            addressId = 1,
            version = 0,
            info1 = 9999 to "a",
            info2 = null,
        )
        db.runQuery { QueryDsl.insert(c).single(cyborg) }
        val cyborg2 = db.runQuery {
            QueryDsl.from(c).where {
                c.employeeId eq 99
            }.first()
        }
        assertEquals(cyborg, cyborg2)
    }

    @Test
    fun embedded_alternate() {
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
    fun createdAt_instant() {
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
    fun createdAt_localDateTime() {
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
    fun createdAt_offsetDateTime() {
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
    fun createdAt_customize() {
        val instant = Instant.parse("2021-01-01T00:00:00Z")
        val zoneId = ZoneId.of("UTC")

        val p = Meta.person
        val config = object : JdbcDatabaseConfig by db.config {
            override val clockProvider = ClockProvider {
                Clock.fixed(instant, zoneId)
            }
        }
        val myDb = JdbcDatabase(config)
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
    fun uniqueConstraintException() {
        val a = Meta.address
        val address = Address(1, "STREET 1", 0)
        assertFailsWith<UniqueConstraintException> {
            db.runQuery { QueryDsl.insert(a).single(address) }.let { }
        }
    }

    @Test
    fun identityGenerator() {
        for (i in 1..201) {
            val m = Meta.identityStrategy
            val strategy = IdentityStrategy(0, "test")
            val result = db.runQuery { QueryDsl.insert(m).single(strategy) }
            assertEquals(i, result.id, "i = $i")
        }
    }

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun identityGenerator_disableAutoIncrement() {
        for (i in 1..201) {
            val m = Meta.identityStrategy.clone(disableAutoIncrement = true)
            val strategy = IdentityStrategy(i + 10, "test")
            val result = db.runQuery { QueryDsl.insert(m).single(strategy) }
            assertEquals(i + 10, result.id, "i = $i")
        }
    }

    @Test
    fun identity_onDuplicateKeyUpdate() {
        val m = Meta.identityStrategy
        db.runQuery {
            val strategy = IdentityStrategy(0, "first")
            QueryDsl.insert(m).onDuplicateKeyUpdate().single(strategy)
        }
        db.runQuery {
            val strategy = IdentityStrategy(1, "second")
            QueryDsl.insert(m).onDuplicateKeyUpdate().single(strategy)
        }
        val list = db.runQuery { QueryDsl.from(m) }
        assertEquals(2, list.size)
    }

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5])
    @Test
    fun sequenceGenerator() {
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
    fun sequenceGenerator_disableSequenceAssignment() {
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
    fun onDuplicateKeyUpdate_insert() {
        val d = Meta.department
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().single(department)
        db.runQuery { query }
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentId eq 5 }.first() }
        assertNotNull(found)
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys_insert() {
        val d = Meta.department
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).single(department)
        db.runQuery { query }
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentId eq 5 }.first() }
        assertNotNull(found)
    }

    @Test
    fun onDuplicateKeyUpdate_update() {
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
    fun onDuplicateKeyUpdateWithKeys_update() {
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
    fun onDuplicateKeyUpdate_nonUpdatableColumn() {
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
    fun onDuplicateKeyUpdate_update_set() {
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
    fun onDuplicateKeyUpdateWithKey_update_set() {
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
    @Run(onlyIf = [Dbms.H2, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    fun onDuplicateKeyUpdateWithKey_update_set_where_success() {
        val d = Meta.department
        val department = Department(5, 10, "PLANNING", "TOKYO", 10)
        val query = QueryDsl.insert(d)
            .onDuplicateKeyUpdate(d.departmentNo)
            .set { excluded ->
                d.departmentName eq "PLANNING2"
                d.location eq concat(d.location, concat("_", excluded.location))
            }.where {
                d.location eq "NEW YORK"
            }.single(department)
        db.runQuery { query }
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentNo eq 10 }.first() }
        assertEquals(1, found.departmentId)
        assertEquals("PLANNING2", found.departmentName)
        assertEquals("NEW YORK_TOKYO", found.location)
        assertEquals(1, found.version)
    }

    @Test
    @Run(onlyIf = [Dbms.H2, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    fun onDuplicateKeyUpdateWithKey_update_set_where_fail() {
        val d = Meta.department
        val department = Department(5, 10, "PLANNING", "TOKYO", 10)
        val query = QueryDsl.insert(d)
            .onDuplicateKeyUpdate(d.departmentNo)
            .set { excluded ->
                d.departmentName eq "PLANNING2"
                d.location eq concat(d.location, concat("_", excluded.location))
            }.where {
                d.location eq "KYOTO"
            }.single(department)
        db.runQuery { query }
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentNo eq 10 }.first() }
        assertEquals(1, found.departmentId)
        assertNotEquals("PLANNING2", found.departmentName)
        assertNotEquals("NEW YORK_TOKYO", found.location)
        assertEquals(1, found.version)
    }

    @Test
    @Run(onlyIf = [Dbms.MARIADB, Dbms.MYSQL, Dbms.MYSQL_5])
    fun onDuplicateKeyUpdateWithKey_update_set_where_unsupported() {
        val d = Meta.department
        val department = Department(5, 10, "PLANNING", "TOKYO", 10)
        val query = QueryDsl.insert(d)
            .onDuplicateKeyUpdate(d.departmentNo)
            .where {
                d.location eq "NEW YORK"
            }.single(department)
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery(query)
            Unit
        }
        println(ex)
    }

    @Test
    @Run(unless = [Dbms.POSTGRESQL])
    fun dangerouslyOnDuplicateKeyUpdate() {
        val d = Meta.department
        val department = Department(5, 10, "PLANNING", "TOKYO", 10)
        val query = QueryDsl.insert(d)
            .dangerouslyOnDuplicateKeyUpdate("test")
            .single(department)
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery(query)
            Unit
        }
        println(ex)
    }

    @Test
    fun onDuplicateKeyIgnore_inserted() {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore().single(address)
        val count = db.runQuery { query }
        assertEquals(1, count)
    }

    @Test
    fun onDuplicateKeyIgnore_ignored_primaryKeyCollision() {
        val a = Meta.address
        val address = Address(1, "STREET 100", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore().single(address)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Test
    @Run(onlyIf = [Dbms.MARIADB, Dbms.MYSQL, Dbms.MYSQL_5, Dbms.POSTGRESQL])
    fun onDuplicateKeyIgnore_ignored_uniqueKeyCollision() {
        val a = Meta.address
        val address = Address(16, "STREET 1", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore().single(address)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Test
    fun onDuplicateKeyIgnoreWithKey_ignored() {
        val a = Meta.address
        val address = Address(100, "STREET 1", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore(a.street).single(address)
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun onDuplicateKeyUpdateWithIndexPredicate() {
        val n = Meta.name
        val name = Name(1, "first", "last", null)
        val query = QueryDsl.insert(n).onDuplicateKeyUpdate(n.lastName) {
            n.deletedAt.isNull()
        }.single(name)
        db.runQuery { query }
        val found = db.runQuery { QueryDsl.from(n).where { n.id eq 1 }.first() }
        assertNotNull(found)
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun onDuplicateKeyUpdateWithIndexPredicate_unsupported() {
        val n = Meta.name
        val name = Name(1, "first", "last", null)
        val query = QueryDsl.insert(n).onDuplicateKeyUpdate(n.lastName) {
            n.deletedAt.isNull()
        }.single(name)
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery(query)
            Unit
        }
        println(ex)
    }
}
