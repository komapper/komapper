package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.UniqueConstraintException
import org.komapper.core.dsl.EntityQuery

@ExtendWith(Env::class)
class EntityBatchInsertQueryTest(private val db: Database) {

    @Test
    fun test() {
        val a = Address.metamodel()
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        val results = db.batchInsert(a, addressList)
        val list = db.execute(EntityQuery.from(a).where { a.addressId inList listOf(16, 17, 18) })
        assertEquals(list, results)
    }

    @Test
    fun identity() {
        val i = IdentityStrategy.metamodel()
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC")
        )
        val results = db.batchInsert(i, strategies)
        val list = db.execute(EntityQuery.from(i).orderBy(i.id))
        assertEquals(list, results)
        for (result in results) {
            assertNotNull(result.id)
            assertNotNull(result.id)
        }
    }

    @Test
    fun createdAt_updatedAt() {
        val p = Person.metamodel()
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C")
        )
        val results = db.batchInsert(p, personList)
        for (result in results) {
            assertNotNull(result.createdAt)
            assertNotNull(result.updatedAt)
        }
    }

    @Test
    fun uniqueConstraintException() {
        val a = Address.metamodel()
        assertThrows<UniqueConstraintException> {
            db.batchInsert(
                a,
                listOf(
                    Address(16, "STREET 16", 0),
                    Address(17, "STREET 17", 0),
                    Address(18, "STREET 1", 0)
                )
            )
        }
    }
}
