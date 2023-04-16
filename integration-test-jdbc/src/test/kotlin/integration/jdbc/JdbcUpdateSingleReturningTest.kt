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

@ExtendWith(JdbcEnv::class)
class JdbcUpdateSingleReturningTest(private val db: JdbcDatabase) {

    @Run(onlyIf = [Dbms.POSTGRESQL])
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

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun uniqueConstraintException() {
        val a = Meta.address
        val address = Address(1, "STREET 2", 1)
        assertFailsWith<UniqueConstraintException> {
            db.runQuery { QueryDsl.update(a).single(address).returning() }.let { }
        }
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun optimisticLockException() {
        val a = Meta.address
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 15 }.first() }
        db.runQuery { QueryDsl.update(a).single(address) }
        assertFailsWith<OptimisticLockException> {
            db.runQuery { QueryDsl.update(a).single(address).returning() }.let {}
        }
    }

    @Run(unless = [Dbms.POSTGRESQL])
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
