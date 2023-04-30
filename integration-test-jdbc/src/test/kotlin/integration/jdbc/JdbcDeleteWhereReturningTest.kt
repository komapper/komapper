package integration.jdbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Run
import integration.core.address
import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcDeleteWhereReturningTest(private val db: JdbcDatabase) {

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun test() {
        val a = Meta.address
        db.runQuery {
            QueryDsl.insert(a).multiple(Address(16, "STREET 16", 0))
        }
        val query = QueryDsl.from(a).where { a.addressId inList listOf(15, 16) }
        val addressList = db.runQuery { query }
        assertEquals(2, addressList.size)
        val addressList2 = db.runQuery {
            QueryDsl.delete(a).where { a.addressId inList listOf(15, 16) }.returning()
        }
        assertEquals(addressList.toSet(), addressList2.toSet())
        val addressList3 = db.runQuery { query }
        assertTrue(addressList3.isEmpty())
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningSingleColumn() {
        val a = Meta.address
        db.runQuery {
            QueryDsl.insert(a).multiple(Address(16, "STREET 16", 0))
        }
        val query = QueryDsl.from(a).where { a.addressId inList listOf(15, 16) }
        val addressList = db.runQuery { query }
        assertEquals(2, addressList.size)
        val streets = db.runQuery {
            QueryDsl.delete(a).where { a.addressId inList listOf(15, 16) }.returning(a.street)
        }
        assertEquals(addressList.map { it.street }.toSet(), streets.toSet())
        val addressList2 = db.runQuery { query }
        assertTrue(addressList2.isEmpty())
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningPairColumns() {
        val a = Meta.address
        db.runQuery {
            QueryDsl.insert(a).multiple(Address(16, "STREET 16", 0))
        }
        val query = QueryDsl.from(a).where { a.addressId inList listOf(15, 16) }
        val addressList = db.runQuery { query }
        assertEquals(2, addressList.size)
        val pairs = db.runQuery {
            QueryDsl.delete(a).where { a.addressId inList listOf(15, 16) }.returning(a.street, a.version)
        }
        assertEquals(addressList.map { it.street to it.version }.toSet(), pairs.toSet())
        val addressList2 = db.runQuery { query }
        assertTrue(addressList2.isEmpty())
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningTripleColumns() {
        val a = Meta.address
        db.runQuery {
            QueryDsl.insert(a).multiple(Address(16, "STREET 16", 0))
        }
        val query = QueryDsl.from(a).where { a.addressId inList listOf(15, 16) }
        val addressList = db.runQuery { query }
        assertEquals(2, addressList.size)
        val triples = db.runQuery {
            QueryDsl.delete(a).where { a.addressId inList listOf(15, 16) }.returning(a.street, a.version, a.addressId)
        }
        assertEquals(addressList.map { Triple(it.street, it.version, it.addressId) }.toSet(), triples.toSet())
        val addressList2 = db.runQuery { query }
        assertTrue(addressList2.isEmpty())
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun allowMissingWhereClause_default() {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                QueryDsl.delete(e).all().returning()
            }
        }
        println(ex)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun allowMissingWhereClause_default_empty() {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                QueryDsl.delete(e).where { }.returning()
            }
        }
        println(ex)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun allowMissingWhereClause_true() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.delete(e).all().returning().options { it.copy(allowMissingWhereClause = true) }
        }
        assertEquals(14, list.size)
    }

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun unsupportedOperationException_deleteReturning() {
        val a = Meta.address
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.delete(a).where { a.addressId eq 15 }.returning() }
            Unit
        }
        println(ex)
    }
}
