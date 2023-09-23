package integration.r2dbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Run
import integration.core.address
import integration.core.employee
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcDeleteWhereReturningTest(private val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
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

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningSingleColumn(info: TestInfo) = inTransaction(db, info) {
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

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningPairColumns(info: TestInfo) = inTransaction(db, info) {
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

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningTripleColumns(info: TestInfo) = inTransaction(db, info) {
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

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun all(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.delete(e).all().returning()
        }
        assertEquals(14, list.size)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun allowMissingWhereClause_default(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                QueryDsl.delete(e).where { }.returning()
            }
        }
        println(ex)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun allowMissingWhereClause_true(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.delete(e).where { }.returning().options { it.copy(allowMissingWhereClause = true) }
        }
        assertEquals(14, list.size)
    }

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun unsupportedOperationException_deleteReturning(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.delete(a).where { a.addressId eq 15 }.returning() }
            Unit
        }
        println(ex)
    }
}
