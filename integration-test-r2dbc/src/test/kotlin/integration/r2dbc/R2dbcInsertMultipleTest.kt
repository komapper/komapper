package integration.r2dbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Department
import integration.core.IdentityStrategy
import integration.core.Man
import integration.core.Person
import integration.core.Run
import integration.core.address
import integration.core.department
import integration.core.identityStrategy
import integration.core.man
import integration.core.person
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcInsertMultipleTest(private val db: R2dbcDatabase) {
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
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

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5, Dbms.MARIADB, Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun identity(info: TestInfo) = inTransaction(db, info) {
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
    }

    @Run(onlyIf = [Dbms.MYSQL, Dbms.MYSQL_5, Dbms.MARIADB, Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun identity_unsupportedOperationException(info: TestInfo) = inTransaction(db, info) {
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

    @Run(unless = [Dbms.ORACLE])
    @Test
    fun identity_doNotReturnGeneratedKeys(info: TestInfo) = inTransaction(db, info) {
        val i = Meta.identityStrategy
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC"),
        )
        val result1 = db.runQuery {
            QueryDsl.insert(i).multiple(strategies).options {
                it.copy(returnGeneratedKeys = false)
            }
        }
        val result2 = db.runQuery {
            QueryDsl.from(i)
                .where { i.value inList listOf("AAA", "BBB", "CCC") }
                .orderBy(i.value)
        }
        assertEquals(listOf("AAA", "BBB", "CCC"), result2.map { it.value })
        assertTrue(result1.all { it.id == null })
    }

    @Test
    fun createdAt_updatedAt(info: TestInfo) = inTransaction(db, info) {
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
    fun uniqueConstraintException(info: TestInfo) = inTransaction(db, info) {
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
    fun onDuplicateKeyUpdate(info: TestInfo) = inTransaction(db, info) {
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
    fun onDuplicateKeyUpdate_nonUpdatableColumn(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.man
        val initialList = listOf(
            Man(manId = 1, name = "Alice", createdBy = "nobody", updatedBy = "nobody"),
            Man(manId = 2, name = "Bob", createdBy = "nobody", updatedBy = "nobody"),
            Man(manId = 3, name = "Clair", createdBy = "nobody", updatedBy = "nobody"),
        )
        val findQuery = QueryDsl.from(p).where { p.manId inList listOf(1, 2, 3) }
        db.runQuery { QueryDsl.insert(p).onDuplicateKeyUpdate().multiple(initialList) }
        val beforeUpdate = db.runQuery { findQuery }
        db.runQuery {
            val updateList = beforeUpdate.map { it.copy(createdBy = "somebody", updatedBy = "somebody") }
            QueryDsl.insert(p).onDuplicateKeyUpdate().multiple(updateList)
        }
        val afterUpdate = db.runQuery { findQuery }
        for (person in afterUpdate) {
            assertEquals("nobody", person.createdBy)
            assertEquals("somebody", person.updatedBy)
        }
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query =
            QueryDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).multiple(listOf(department1, department2))
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
    fun onDuplicateKeyUpdate_set(info: TestInfo) = inTransaction(db, info) {
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
    fun onDuplicateKeyUpdateWithKey_set(info: TestInfo) = inTransaction(db, info) {
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
    fun onDuplicateKeyIgnore(info: TestInfo) = inTransaction(db, info) {
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
    fun onDuplicateKeyIgnoreWithKeys(info: TestInfo) = inTransaction(db, info) {
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
    @Run(unless = [Dbms.ORACLE])
    fun identity_onDuplicateKeyUpdate(info: TestInfo) = inTransaction(db, info) {
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

    @Test
    @Run(onlyIf = [Dbms.ORACLE])
    fun identity_onDuplicateKeyUpdate_unsupported(info: TestInfo) = inTransaction(db, info) {
        val i = Meta.identityStrategy
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC"),
        )
        val query = QueryDsl.insert(i).onDuplicateKeyUpdate().multiple(strategies)
        assertFailsWith<UnsupportedOperationException> {
            db.runQuery { query }
            Unit
        }
    }
}
