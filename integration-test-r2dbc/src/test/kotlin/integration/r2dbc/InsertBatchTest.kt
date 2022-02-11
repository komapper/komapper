package integration.r2dbc

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
import org.komapper.r2dbc.R2dbcDatabase
import java.sql.Statement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@ExtendWith(Env::class)
class InsertBatchTest(private val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun test() = inTransaction(db) {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        val ids = db.runQuery { QueryDsl.insert(a).batch(addressList) }.map { it.addressId }
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList ids }
        }
        assertEquals(addressList, list)
    }

    @Test
    fun identity_unsupportedOperationException() = inTransaction(db) {
        val i = Meta.identityStrategy
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC")
        )
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.insert(i).batch(strategies) }
            Unit
        }
        println(ex)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun createdAt_updatedAt() = inTransaction(db) {
        val p = Meta.person
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C")
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
    fun uniqueConstraintException() = inTransaction(db) {
        val a = Meta.address
        assertFailsWith<UniqueConstraintException> {
            db.runQuery {
                QueryDsl.insert(
                    a
                ).batch(
                    listOf(
                        Address(16, "STREET 16", 0),
                        Address(17, "STREET 17", 0),
                        Address(18, "STREET 1", 0)
                    )
                )
            }.let { }
        }
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdate() = inTransaction(db) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().batch(department1, department2)
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mariadb" -> assertEquals(listOf(Statement.SUCCESS_NO_INFO, Statement.SUCCESS_NO_INFO), counts)
            "mysql" -> assertEquals(listOf(1, 2), counts)
            else -> assertEquals(listOf(1, 1), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdateWithKeys() = inTransaction(db) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).batch(department1, department2)
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mariadb" -> assertEquals(listOf(Statement.SUCCESS_NO_INFO, Statement.SUCCESS_NO_INFO), counts)
            "mysql" -> assertEquals(listOf(1, 2), counts)
            else -> assertEquals(listOf(1, 1), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdate_set() = inTransaction(db) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query =
            QueryDsl.insert(d).onDuplicateKeyUpdate().set { excluded ->
                d.departmentName eq excluded.departmentName
            }.batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mysql", "mariadb" -> assertEquals(listOf(1, 2), counts)
            else -> assertEquals(listOf(1, 1), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdateWithKeys_set() = inTransaction(db) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query =
            QueryDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).set { excluded ->
                d.departmentName eq excluded.departmentName
            }.batch(department1, department2)
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mysql", "mariadb" -> assertEquals(listOf(1, 2), counts)
            else -> assertEquals(listOf(1, 1), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyIgnore() = inTransaction(db) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyIgnore().batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mariadb" -> assertEquals(listOf(Statement.SUCCESS_NO_INFO, Statement.SUCCESS_NO_INFO), counts)
            else -> assertEquals(listOf(1, 0), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyIgnoreWithKeys() = inTransaction(db) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyIgnore(d.departmentNo).batch(listOf(department1, department2))
        val counts = db.runQuery { query }
        when (db.config.dialect.driver) {
            "mariadb" -> assertEquals(listOf(Statement.SUCCESS_NO_INFO, Statement.SUCCESS_NO_INFO), counts)
            else -> assertEquals(listOf(1, 0), counts)
        }
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Run(unless = [Dbms.ORACLE, Dbms.SQLSERVER])
    @Test
    fun identity_onDuplicateKeyUpdate_unsupportedOperationException() = inTransaction(db) {
        val i = Meta.identityStrategy
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC")
        )
        assertFailsWith<UnsupportedOperationException> {
            val query = QueryDsl.insert(i).onDuplicateKeyUpdate().batch(strategies)
            db.runQuery(query)
            Unit
        }
    }
}
