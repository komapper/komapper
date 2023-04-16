package integration.r2dbc

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
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcInsertMultipleReturningTest(private val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        val addressList2 = db.runQuery { QueryDsl.insert(a).multiple(addressList).returning() }
        assertEquals(addressList, addressList2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun identity(info: TestInfo) = inTransaction(db, info) {
        val i = Meta.identityStrategy
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC"),
        )
        val results1 = db.runQuery { QueryDsl.insert(i).multiple(strategies).returning() }
        val results2 = db.runQuery { QueryDsl.from(i).orderBy(i.id) }
        assertEquals(results1, results2)
        assertTrue(results1.all { it.id != null })
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun sequenceGenerator(info: TestInfo) = inTransaction(db, info) {
        val generator = Meta.sequenceStrategy.idGenerator() as IdGenerator.Sequence<*, *>
        generator.clear()

        val range = (1..201)
        val m = Meta.sequenceStrategy
        val strategies = range.map { SequenceStrategy(0, "test") }
        val strategies2 = db.runQuery { QueryDsl.insert(m).multiple(strategies).returning() }
        val strategies3 = range.map { SequenceStrategy(it, "test") }

        assertEquals(strategies3, strategies2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun onDuplicateKeyUpdate(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val departments = listOf(
            Department(5, 50, "PLANNING", "TOKYO", 1),
            Department(1, 60, "DEVELOPMENT", "KYOTO", 1),
        )
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().multiple(departments).returning()
        val departments2 = db.runQuery { query }
        assertEquals(departments, departments2)
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun onDuplicateKeyIgnore(info: TestInfo) = inTransaction(db, info) {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyIgnore().multiple(listOf(department1, department2)).returning()
        val departments = db.runQuery { query }
        assertEquals(listOf(department1), departments)
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun unsupportedOperationException_insertReturning(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.insert(a).multiple(addressList).returning() }
            Unit
        }
        println(ex)
    }

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun unsupportedOperationException_onDuplicateKeyUpdate_insertReturning(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.insert(a).onDuplicateKeyUpdate().multiple(addressList).returning() }
            Unit
        }
        println(ex)
    }
}
