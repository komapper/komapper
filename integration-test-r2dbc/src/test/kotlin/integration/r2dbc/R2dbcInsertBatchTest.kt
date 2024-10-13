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
import java.sql.Statement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@ExtendWith(R2dbcEnv::class)
class R2dbcInsertBatchTest(private val db: R2dbcDatabase) {
    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        val ids = db.runQuery { QueryDsl.insert(a).batch(addressList) }.map { it.addressId }
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList ids }
        }
        assertEquals(addressList, list)
    }

    @Test
    fun identity_unsupportedOperationException(info: TestInfo) = inTransaction(db, info) {
        val i = Meta.identityStrategy
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC"),
        )
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.insert(i).batch(strategies) }
            Unit
        }
        println(ex)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun createdAt_updatedAt(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C"),
        )
        val ids = db.runQuery { QueryDsl.insert(p).batch(personList) }.map { it.personId }
        val list = db.runQuery { QueryDsl.from(p).where { p.personId inList ids } }
        for (person in list) {
            assertNotNull(person.createdAt)
            assertNotNull(person.updatedAt)
        }
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun uniqueConstraintException(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        assertFailsWith<UniqueConstraintException> {
            db.runQuery {
                QueryDsl.insert(
                    a,
                ).batch(
                    listOf(
                        Address(16, "STREET 16", 0),
                        Address(17, "STREET 17", 0),
                        Address(18, "STREET 1", 0),
                    ),
                )
            }.let { }
        }
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdate(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().batch(department1, department2)
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mariadb" -> assertEquals(listOf(Statement.SUCCESS_NO_INFO.toLong(), Statement.SUCCESS_NO_INFO.toLong()), counts)
            "mysql" -> assertEquals(listOf(1L, 2L), counts)
            else -> assertEquals(listOf(1L, 1L), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdate_nonUpdatableColumn(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.man
        val initialList = listOf(
            Man(manId = 1, name = "Alice", createdBy = "nobody", updatedBy = "nobody"),
            Man(manId = 2, name = "Bob", createdBy = "nobody", updatedBy = "nobody"),
            Man(manId = 3, name = "Clair", createdBy = "nobody", updatedBy = "nobody"),
        )
        val findQuery = QueryDsl.from(p).where { p.manId inList listOf(1, 2, 3) }
        db.runQuery { QueryDsl.insert(p).onDuplicateKeyUpdate().batch(initialList) }
        val beforeUpdate = db.runQuery { findQuery }
        db.runQuery {
            val updateList = beforeUpdate.map { it.copy(createdBy = "somebody", updatedBy = "somebody") }
            QueryDsl.insert(p).onDuplicateKeyUpdate().batch(updateList)
        }
        val afterUpdate = db.runQuery { findQuery }
        for (person in afterUpdate) {
            assertEquals("nobody", person.createdBy)
            assertEquals("somebody", person.updatedBy)
        }
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdateWithKeys(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).batch(department1, department2)
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mariadb" -> assertEquals(listOf(Statement.SUCCESS_NO_INFO.toLong(), Statement.SUCCESS_NO_INFO.toLong()), counts)
            "mysql" -> assertEquals(listOf(1L, 2L), counts)
            else -> assertEquals(listOf(1L, 1L), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdate_set(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query =
            QueryDsl.insert(d).onDuplicateKeyUpdate().set { excluded ->
                d.departmentName eq excluded.departmentName
            }.batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mysql", "mariadb" -> assertEquals(listOf(1L, 2L), counts)
            else -> assertEquals(listOf(1L, 1L), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdateWithKeys_set(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query =
            QueryDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).set { excluded ->
                d.departmentName eq excluded.departmentName
            }.batch(department1, department2)
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mysql", "mariadb" -> assertEquals(listOf(1L, 2L), counts)
            else -> assertEquals(listOf(1L, 1L), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyIgnore(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyIgnore().batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mariadb" -> assertEquals(listOf(Statement.SUCCESS_NO_INFO.toLong(), Statement.SUCCESS_NO_INFO.toLong()), counts)
            else -> assertEquals(listOf(1L, 0L), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyIgnoreWithKeys(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyIgnore(d.departmentNo).batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mariadb" -> assertEquals(listOf(Statement.SUCCESS_NO_INFO.toLong(), Statement.SUCCESS_NO_INFO.toLong()), counts)
            else -> assertEquals(listOf(1L, 0L), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(unless = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun identity_onDuplicateKeyUpdate_unsupportedOperationException(info: TestInfo) = inTransaction(db, info) {
        val i = Meta.identityStrategy
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC"),
        )
        assertFailsWith<UnsupportedOperationException> {
            val query = QueryDsl.insert(i).onDuplicateKeyUpdate().batch(strategies)
            db.runQuery(query)
            Unit
        }
    }
}
