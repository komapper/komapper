package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.UniqueConstraintException

@ExtendWith(Env::class)
class EntityInsertQueryableTest(private val db: Database) {
    @Test
    fun test() {
        val a = Address.metamodel()
        val address = Address(16, "STREET 16", 0)
        db.insert(a, address)
        val address2 = db.find(a) { a.addressId eq 16 }
        assertEquals(address, address2)
    }

    @Test
    fun createdAt() {
        val p = Person.metamodel()
        val person1 = Person(1, "ABC")
        val person2 = db.insert(p, person1)
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        val person3 = db.find(p) { p.personId to 1 }
        assertEquals(person2, person3)
    }

    /*
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    @Test
    fun globalEntityListener() {
        val db = Db(object : DbConfig() {
            override val dataSource = db.config.dataSource
            override val dialect = db.config.dialect
            override val entityMetaResolver = db.config.entityMetaResolver
            override val listener = object : GlobalEntityListener {
                override fun <T : Any> preInsert(
                    entity: T,
                    desc: EntityDesc<T>
                ): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T : Any> postInsert(
                    entity: T,
                    desc: EntityDesc<T>
                ): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "${entity.street}*")
                        else -> entity
                    } as T
                }
            }
        })

        val address = Address(16, "STREET 16", 0)
        val address2 = db.insert(address)
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16*",
                0
            ), address2
        )
        val t = template<Address>("select * from address where address_id = 16")
        val address3 = db.select(t).firstOrNull()
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16",
                0
            ), address3
        )
    }

    @Test
    fun entityListener() {
        val db = Db(
            AddressListenerConfig(
                db.config,
                object :
                    EntityListener<Address> {
                    override fun preInsert(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "*${entity.street}")
                    }

                    override fun postInsert(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "${entity.street}*")
                    }
                })
        )

        val address = Address(16, "STREET 16", 0)
        val address2 = db.insert(address)
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16*",
                0
            ), address2
        )
        val t = template<Address>("select * from address where address_id = 16")
        val address3 = db.select(t).firstOrNull()
        Assertions.assertEquals(
            Address(
                16,
                "*STREET 16",
                0
            ), address3
        )
    }

    */

    @Test
    fun uniqueConstraintException() {
        val a = Address.metamodel()
        val address = Address(1, "STREET 1", 0)
        assertThrows<UniqueConstraintException> {
            db.insert(a, address)
        }
    }

    @Test
    fun identityGenerator() {
        for (i in 1..201) {
            val m = IdentityStrategy.metamodel()
            val strategy = IdentityStrategy(0, "test")
            val newStrategy = db.insert(m, strategy)
            assertEquals(i, newStrategy.id)
        }
    }

    @Test
    fun sequenceGenerator() {
        for (i in 1..201) {
            val m = SequenceStrategy.metamodel()
            val strategy = SequenceStrategy(0, "test")
            val newStrategy = db.insert(m, strategy)
            assertEquals(i, newStrategy.id)
        }
    }
}
