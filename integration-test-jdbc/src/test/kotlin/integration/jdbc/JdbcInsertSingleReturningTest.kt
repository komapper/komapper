package integration.jdbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Department
import integration.core.IdentityStrategy
import integration.core.Run
import integration.core.SequenceStrategy
import integration.core.address
import integration.core.department
import integration.core.identityStrategy
import integration.core.sequenceStrategy
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(JdbcEnv::class)
class JdbcInsertSingleReturningTest(private val db: JdbcDatabase) {

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun test() {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        val address2 = db.runQuery { QueryDsl.insert(a).single(address).returning() }
        assertEquals(address, address2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun uniqueConstraintException() {
        val a = Meta.address
        val address = Address(1, "STREET 1", 0)
        assertFailsWith<UniqueConstraintException> {
            db.runQuery { QueryDsl.insert(a).single(address).returning() }.let { }
        }
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
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

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun identityGenerator() {
        for (i in 1..201) {
            val m = Meta.identityStrategy
            val strategy = IdentityStrategy(0, "test")
            val result = db.runQuery { QueryDsl.insert(m).single(strategy).returning() }
            assertEquals(i, result.id, "i = $i")
        }
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun onDuplicateKeyUpdate_insert() {
        val d = Meta.department
        val department = Department(5, 50, "PLANNING", "TOKYO", 0)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().single(department).returning()
        val department2 = db.runQuery { query }
        assertEquals(department, department2)
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentId eq 5 }.first() }
        assertNotNull(found)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun onDuplicateKeyUpdate_update() {
        val d = Meta.department
        val department = Department(1, 50, "PLANNING", "TOKYO", 10)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().single(department).returning()
        val department2 = db.runQuery { query }
        assertEquals(department, department2)
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentId eq 1 }.first() }
        assertEquals(50, found.departmentNo)
        assertEquals("PLANNING", found.departmentName)
        assertEquals("TOKYO", found.location)
        assertEquals(10, found.version)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
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
            }.single(department).returning()
        val department2 = db.runQuery { query }
        assertNotNull(department2)
        val found = db.runQuery { QueryDsl.from(d).where { d.departmentNo eq 10 }.first() }
        assertEquals(department2, found)
        assertEquals(1, found.departmentId)
        assertEquals("PLANNING2", found.departmentName)
        assertEquals("NEW YORK_TOKYO", found.location)
        assertEquals(1, found.version)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.POSTGRESQL])
    @Test
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
            }.single(department).returning()
        val department2 = db.runQuery { query }
        assertNull(department2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun onDuplicateKeyIgnore_inserted() {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore().single(address).returning()
        val address2 = db.runQuery { query }
        assertEquals(address, address2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun onDuplicateKeyIgnore_ignored() {
        val a = Meta.address
        val address = Address(1, "STREET 100", 0)
        val query = QueryDsl.insert(a).onDuplicateKeyIgnore().single(address).returning()
        val address2 = db.runQuery { query }
        assertNull(address2)
    }

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun unsupportedOperationException_insertReturning() {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.insert(a).single(address).returning() }
            Unit
        }
        println(ex)
    }

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun unsupportedOperationException_onDuplicateKeyUpdate_insertReturning() {
        val a = Meta.address
        val address = Address(16, "STREET 16", 0)
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.insert(a).onDuplicateKeyUpdate().single(address).returning() }
            Unit
        }
        println(ex)
    }
}
