package integration.jdbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Department
import integration.core.IdentityStrategy
import integration.core.Person
import integration.core.Run
import integration.core.address
import integration.core.department
import integration.core.identityStrategy
import integration.core.person
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcInsertMultipleTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        val ids = db.runQuery { QueryDsl.insert(a).multiple(addressList) }.map { it.addressId }
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList ids }
        }
        assertEquals(addressList, list)
    }

    @Run(unless = [Dbms.MARIADB, Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun identity() {
        val i = Meta.identityStrategy
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC"),
        )
        val results1 = db.runQuery { QueryDsl.insert(i).multiple(strategies) }
        val results2 = db.runQuery { QueryDsl.from(i).orderBy(i.id) }
        assertEquals(results1, results2)
        assertTrue(results1.all { it.id != null })
        println(results1)
    }

    @Run(onlyIf = [Dbms.MARIADB, Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun identity_unsupportedOperationException() {
        val i = Meta.identityStrategy
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC"),
        )
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.insert(i).multiple(strategies) }
            Unit
        }
        println(ex)
    }

    // @Run(unless = [Dbms.ORACLE])
    @Test
    fun identity_doNotReturnGeneratedKeys() {
        val i = Meta.identityStrategy
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC"),
        )
        val result1 = db.runQuery {
            QueryDsl.insert(i).multiple(strategies).options {
                it.copy(returnGeneratedKeys = true)
            }
        }
        val result2 = db.runQuery {
            QueryDsl.from(i)
                .where { i.value inList listOf("AAA", "BBB", "CCC") }
                .orderBy(i.value)
        }
        assertEquals(listOf("AAA", "BBB", "CCC"), result2.map { it.value })
        // assertTrue(result1.all { it.id == null })
        println(result1)
    }

    @Test
    fun createdAt_updatedAt() {
        val p = Meta.person
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C"),
        )
        val ids = db.runQuery { QueryDsl.insert(p).multiple(personList) }.map { it.personId }
        val list = db.runQuery { QueryDsl.from(p).where { p.personId inList ids } }
        for (person in list) {
            assertNotNull(person.createdAt)
            assertNotNull(person.updatedAt)
        }
    }

    @Test
    fun uniqueConstraintException() {
        val a = Meta.address
        assertFailsWith<UniqueConstraintException> {
            db.runQuery {
                QueryDsl.insert(
                    a,
                ).multiple(
                    listOf(
                        Address(16, "STREET 16", 0),
                        Address(17, "STREET 17", 0),
                        Address(18, "STREET 1", 0),
                    ),
                )
            }.let { }
        }
    }

    @Test
    fun onDuplicateKeyUpdate() {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().multiple(listOf(department1, department2))
        db.runQuery { query }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys() {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).multiple(listOf(department1, department2))
        db.runQuery { query }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Test
    @Run(unless = [Dbms.MARIADB])
    fun onDuplicateKeyUpdate_set() {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 10, "DEVELOPMENT", "KYOTO", 1)
        val query =
            QueryDsl.insert(d).onDuplicateKeyUpdate().set { excluded ->
                d.departmentName eq excluded.departmentName
            }.multiple(listOf(department1, department2))
        db.runQuery { query }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Test
    @Run(unless = [Dbms.MARIADB])
    fun onDuplicateKeyUpdateWithKey_set() {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query =
            QueryDsl.insert(d)
                .onDuplicateKeyUpdate(d.departmentNo)
                .set { excluded ->
                    d.departmentName eq excluded.departmentName
                }.multiple(listOf(department1, department2))
        db.runQuery { query }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Test
    fun onDuplicateKeyIgnore() {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyIgnore().multiple(listOf(department1, department2))
        val count = db.runQuery { query }
        assertEquals(1, count)
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Test
    fun onDuplicateKeyIgnoreWithKeys() {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d)
            .onDuplicateKeyIgnore(d.departmentNo)
            .multiple(listOf(department1, department2))
        val count = db.runQuery { query }
        assertEquals(1, count)
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Test
    fun identity_onDuplicateKeyUpdate() {
        val i = Meta.identityStrategy
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC"),
        )
        val query = QueryDsl.insert(i).onDuplicateKeyUpdate().multiple(strategies)
        val count = db.runQuery { query }
        assertEquals(3, count)
    }
}
