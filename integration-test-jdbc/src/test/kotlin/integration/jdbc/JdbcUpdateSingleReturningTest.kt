package integration.jdbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Run
import integration.core.address
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dryRunQuery
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.core.dsl.query.single
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@ExtendWith(JdbcEnv::class)
class JdbcUpdateSingleReturningTest(private val db: JdbcDatabase) {

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun test() {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = "NY street")
        val returningAddress = db.runQuery { QueryDsl.update(a).single(newAddress).returning() }
        val address2 = db.runQuery { query.firstOrNull() }
        assertEquals(
            Address(
                15,
                "NY street",
                2,
            ),
            address2,
        )
        assertEquals(address2, returningAddress)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningSingleColumn() {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = "NY street")
        val street = db.runQuery { QueryDsl.update(a).single(newAddress).returning(a.street) }
        val address2 = db.runQuery { query.firstOrNull() }
        assertEquals("NY street", street)
        assertEquals(
            Address(
                15,
                "NY street",
                2,
            ),
            address2,
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningPairColumns() {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = "NY street")
        val pair = db.runQuery { QueryDsl.update(a).single(newAddress).returning(a.street, a.version) }
        val address2 = db.runQuery { query.firstOrNull() }
        assertEquals("NY street" to 2, pair)
        assertEquals(
            Address(
                15,
                "NY street",
                2,
            ),
            address2,
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningTripleColumns() {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = "NY street")
        val triple = db.runQuery { QueryDsl.update(a).single(newAddress).returning(a.street, a.version, a.addressId) }
        val address2 = db.runQuery { query.firstOrNull() }
        assertEquals(Triple("NY street", 2, 15), triple)
        assertEquals(
            Address(
                15,
                "NY street",
                2,
            ),
            address2,
        )
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun suppressOptimisticLockException() {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = "NY street", version = address.version + 1)
        val returningAddress = db.runQuery { QueryDsl.update(a).single(newAddress).returning().options { it.copy(suppressOptimisticLockException = true) } }
        assertNull(returningAddress)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun uniqueConstraintException() {
        val a = Meta.address
        val address = Address(1, "STREET 2", 1)
        assertFailsWith<UniqueConstraintException> {
            db.runQuery { QueryDsl.update(a).single(address).returning() }.let { }
        }
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun optimisticLockException() {
        val a = Meta.address
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 15 }.first() }
        db.runQuery { QueryDsl.update(a).single(address) }
        assertFailsWith<OptimisticLockException> {
            db.runQuery { QueryDsl.update(a).single(address).returning() }.let {}
        }
    }

    @Run(unless = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun unsupportedOperationException_updateReturning() {
        val a = Meta.address
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 15 }.single() }
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "STREET 123")).returning() }
            Unit
        }
        println(ex)
    }

    @Test
    fun dryRun() {
        val a = Meta.address
        val address = Address(addressId = 1, street = "STREET 123", version = 0)
        val query = QueryDsl.update(a).single(address).returning()
        println(db.dryRunQuery(query))
    }
}
