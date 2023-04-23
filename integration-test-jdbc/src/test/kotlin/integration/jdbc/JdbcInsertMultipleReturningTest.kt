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
import org.komapper.core.dryRunQuery
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcInsertMultipleReturningTest(private val db: JdbcDatabase) {

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun test() {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        val addressList2 = db.runQuery { QueryDsl.insert(a).multiple(addressList).returning() }
        assertEquals(addressList, addressList2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningSingleColumn() {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        val streets = db.runQuery { QueryDsl.insert(a).multiple(addressList).returning(a.street) }
        assertEquals(addressList.map { it.street }, streets)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningPairColumns() {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        val pairs = db.runQuery { QueryDsl.insert(a).multiple(addressList).returning(a.street, a.version) }
        assertEquals(addressList.map { it.street to it.version }, pairs)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningTripleColumns() {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        val triples = db.runQuery { QueryDsl.insert(a).multiple(addressList).returning(a.street, a.version, a.addressId) }
        assertEquals(addressList.map { Triple(it.street, it.version, it.addressId) }, triples)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun identity() {
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

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun sequenceGenerator() {
        val generator = Meta.sequenceStrategy.idGenerator() as IdGenerator.Sequence<*, *>
        generator.clear()

        val range = (1..201)
        val m = Meta.sequenceStrategy
        val strategies = range.map { SequenceStrategy(0, "test") }
        val strategies2 = db.runQuery { QueryDsl.insert(m).multiple(strategies).returning() }
        val strategies3 = range.map { SequenceStrategy(it, "test") }

        assertEquals(strategies3, strategies2)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdate() {
        val d = Meta.department
        val departments = listOf(
            Department(5, 50, "PLANNING", "TOKYO", 1),
            Department(1, 60, "DEVELOPMENT", "KYOTO", 1),
        )
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().multiple(departments).returning()
        val departments2 = db.runQuery { query }
        assertEquals(departments.toSet(), departments2.toSet())
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdateReturningSingleColumn() {
        val d = Meta.department
        val departments = listOf(
            Department(5, 50, "PLANNING", "TOKYO", 1),
            Department(1, 60, "DEVELOPMENT", "KYOTO", 1),
        )
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().multiple(departments).returning(d.departmentName)
        val departmentNameList = db.runQuery { query }
        assertEquals(departments.map { it.departmentName }.toSet(), departmentNameList.toSet())
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdateReturningPairColumns() {
        val d = Meta.department
        val departments = listOf(
            Department(5, 50, "PLANNING", "TOKYO", 1),
            Department(1, 60, "DEVELOPMENT", "KYOTO", 1),
        )
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().multiple(departments).returning(d.departmentName, d.location)
        val pairList = db.runQuery { query }
        assertEquals(departments.map { it.departmentName to it.location }.toSet(), pairList.toSet())
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyUpdateReturningTripleColumns() {
        val d = Meta.department
        val departments = listOf(
            Department(5, 50, "PLANNING", "TOKYO", 1),
            Department(1, 60, "DEVELOPMENT", "KYOTO", 1),
        )
        val query = QueryDsl.insert(d).onDuplicateKeyUpdate().multiple(departments).returning(d.departmentName, d.location, d.departmentNo)
        val tripleList = db.runQuery { query }
        assertEquals(departments.map { Triple(it.departmentName, it.location, it.departmentNo) }.toSet(), tripleList.toSet())
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyIgnore() {
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

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyIgnoreReturningSingleColumn() {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyIgnore().multiple(listOf(department1, department2)).returning(d.departmentName)
        val departmentNameList = db.runQuery { query }
        assertEquals(listOf(department1.departmentName), departmentNameList)
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyIgnoreReturningPairColumns() {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyIgnore().multiple(listOf(department1, department2)).returning(d.departmentName, d.location)
        val pairList = db.runQuery { query }
        assertEquals(listOf(department1.departmentName to department1.location), pairList)
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun onDuplicateKeyIgnoreReturningTripleColumns() {
        val d = Meta.department
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = QueryDsl.insert(d).onDuplicateKeyIgnore().multiple(listOf(department1, department2)).returning(d.departmentName, d.location, d.departmentNo)
        val tripleList = db.runQuery { query }
        assertEquals(listOf(Triple(department1.departmentName, department1.location, department1.departmentNo)), tripleList)
        val list = db.runQuery {
            QueryDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location },
        )
    }

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun unsupportedOperationException_insertReturning() {
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

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun unsupportedOperationException_onDuplicateKeyUpdate_insertReturning() {
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

    @Test
    fun dryRun() {
        val a = Meta.address
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0),
        )
        val query = QueryDsl.insert(a).multiple(addressList).returning()
        println(db.dryRunQuery(query))
    }
}
