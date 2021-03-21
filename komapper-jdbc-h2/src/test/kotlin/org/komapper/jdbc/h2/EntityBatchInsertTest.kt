package org.komapper.jdbc.h2

import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database

@ExtendWith(Env::class)
class EntityBatchInsertTest(private val db: Database) {
/*
    @Test
    fun test() {
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        db.batchInsert(addressList)
        val t = template<Address>("select * from address where address_id in (16, 17, 18)")
        val list = db.select(t)
        Assertions.assertEquals(addressList, list)
    }

    @Test
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
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

        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        val list = db.batchInsert(addressList)
        Assertions.assertEquals(
            listOf(
                Address(16, "*STREET 16*", 0),
                Address(17, "*STREET 17*", 0),
                Address(18, "*STREET 18*", 0)
            ), list
        )
        val list2 = db.select<Address>(template("select * from address where address_id in (16, 17, 18)"))
        Assertions.assertEquals(
            listOf(
                Address(16, "*STREET 16", 0),
                Address(17, "*STREET 17", 0),
                Address(18, "*STREET 18", 0)
            ), list2
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

        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        val list = db.batchInsert(addressList)
        Assertions.assertEquals(
            listOf(
                Address(16, "*STREET 16*", 0),
                Address(17, "*STREET 17*", 0),
                Address(18, "*STREET 18*", 0)
            ), list
        )
        val list2 = db.select<Address>(template("select * from address where address_id in (16, 17, 18)"))
        Assertions.assertEquals(
            listOf(
                Address(16, "*STREET 16", 0),
                Address(17, "*STREET 17", 0),
                Address(18, "*STREET 18", 0)
            ), list2
        )
    }

    @Test
    fun createdAt() {
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C")
        )
        db.batchInsert(personList)
        val list = db.select<Person>(template("select /*%expand*/* from person"))
        Assertions.assertTrue(list.all { it.createdAt > LocalDateTime.MIN })
    }

    @Test
    fun uniqueConstraintException() {
        assertThrows<UniqueConstraintException> {
            db.batchInsert(
                listOf(
                    Address(16, "STREET 16", 0),
                    Address(17, "STREET 17", 0),
                    Address(18, "STREET 1", 0)
                )
            )
        }
    }
 */
}
