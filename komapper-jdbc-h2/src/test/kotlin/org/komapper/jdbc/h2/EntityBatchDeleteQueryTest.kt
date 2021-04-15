package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class EntityBatchDeleteQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.alias
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        for (address in addressList) {
            db.execute { EntityQuery.insert(a, address) }
        }
        val query = EntityQuery.from(a).where { a.addressId inList listOf(16, 17, 18) }
        assertEquals(3, db.execute { query }.size)
        db.execute { EntityQuery.deleteBatch(a, addressList) }
        assertTrue(db.execute { query }.isEmpty())
    }

    @Test
    fun optimisticLockException() {
        val a = Address.alias
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        for (address in addressList) {
            db.execute { EntityQuery.insert(a, address) }
        }
        val query = EntityQuery.from(a).where { a.addressId inList listOf(16, 17, 18) }
        assertEquals(3, db.execute { query }.size)
        val ex = assertThrows<OptimisticLockException> {
            db.execute {
                EntityQuery.deleteBatch(
                    a,
                    listOf(
                        addressList[0],
                        addressList[1],
                        addressList[2].copy(version = 1)
                    )
                )
            }
        }
        assertEquals("index=2, count=0", ex.message)
    }

    @Test
    fun suppressOptimisticLockException() {
        val a = Address.alias
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        for (address in addressList) {
            db.execute { EntityQuery.insert(a, address) }
        }
        val query = EntityQuery.from(a).where { a.addressId inList listOf(16, 17, 18) }
        assertEquals(3, db.execute { query }.size)
        db.execute {
            EntityQuery.deleteBatch(
                a,
                listOf(
                    addressList[0],
                    addressList[1],
                    addressList[2].copy(version = 1)
                )
            ).option {
                it.copy(suppressOptimisticLockException = true)
            }
        }
    }
}
