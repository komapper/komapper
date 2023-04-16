package integration.r2dbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Run
import integration.core.address
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.OptimisticLockException
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.core.dsl.query.single
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(R2dbcEnv::class)
class R2dbcUpdateSingleReturningTest(private val db: R2dbcDatabase) {

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
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
    fun uniqueConstraintException(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address = Address(1, "STREET 2", 1)
        assertFailsWith<UniqueConstraintException> {
            db.runQuery { QueryDsl.update(a).single(address).returning() }.let { }
        }
    }

    @Run(onlyIf = [Dbms.POSTGRESQL])
    @Test
    fun optimisticLockException(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 15 }.first() }
        db.runQuery { QueryDsl.update(a).single(address) }
        assertFailsWith<OptimisticLockException> {
            db.runQuery { QueryDsl.update(a).single(address).returning() }.let {}
        }
    }

    @Run(unless = [Dbms.POSTGRESQL])
    @Test
    fun unsupportedOperationException_updateReturning(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address = db.runQuery { QueryDsl.from(a).where { a.addressId eq 15 }.single() }
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "STREET 123")).returning() }
            Unit
        }
        println(ex)
    }
}
