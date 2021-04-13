package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.execute

@ExtendWith(Env::class)
class EntityBatchInsertQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.alias
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        val results = db.execute { EntityQuery.batchInsert(a, addressList) }
        val list = db.execute {
            EntityQuery.from(a).where { a.addressId inList listOf(16, 17, 18) }
        }
        assertEquals(list, results)
    }

    @Test
    fun identity() {
        val i = IdentityStrategy.alias
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC")
        )
        val results = db.execute { EntityQuery.batchInsert(i, strategies) }
        val list = db.execute { EntityQuery.from(i).orderBy(i.id) }
        assertEquals(list, results)
        for (result in results) {
            assertNotNull(result.id)
            assertNotNull(result.id)
        }
    }

    @Test
    fun createdAt_updatedAt() {
        val p = Person.alias
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C")
        )
        val results = db.execute { EntityQuery.batchInsert(p, personList) }
        for (result in results) {
            assertNotNull(result.createdAt)
            assertNotNull(result.updatedAt)
        }
    }

    @Test
    fun uniqueConstraintException() {
        val a = Address.alias
        assertThrows<UniqueConstraintException> {
            db.execute {
                EntityQuery.batchInsert(
                    a,
                    listOf(
                        Address(16, "STREET 16", 0),
                        Address(17, "STREET 17", 0),
                        Address(18, "STREET 1", 0)
                    )
                )
            }.let { }
        }
    }
}
