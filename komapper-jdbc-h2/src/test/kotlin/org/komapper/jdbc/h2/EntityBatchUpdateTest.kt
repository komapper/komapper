package org.komapper.jdbc.h2

import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database

@ExtendWith(Env::class)
class EntityBatchUpdateTest(private val db: Database) {
/*
    @Test
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    fun globalEntityListener() {
        val db = Db(object : DbConfig() {
            override val dataSource = db.config.dataSource
            override val dialect = db.config.dialect
            override val entityMetaResolver = db.config.entityMetaResolver
            override val listener = object : GlobalEntityListener {
                override fun <T : Any> preUpdate(
                    entity: T,
                    desc: EntityDesc<T>
                ): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T : Any> postUpdate(
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

        val t = template<Address>("select * from address where address_id in (1,2,3)")
        val addressList = db.select(t)
        val list = db.batchUpdate(addressList)
        Assertions.assertEquals(
            listOf(
                Address(1, "*STREET 1*", 2),
                Address(2, "*STREET 2*", 2),
                Address(3, "*STREET 3*", 2)
            ), list
        )
        val list2 = db.select(t)
        Assertions.assertEquals(
            listOf(
                Address(1, "*STREET 1", 2),
                Address(2, "*STREET 2", 2),
                Address(3, "*STREET 3", 2)
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
                    override fun preUpdate(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "*${entity.street}")
                    }

                    override fun postUpdate(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "${entity.street}*")
                    }
                })
        )

        val sql = template<Address>("select * from address where address_id in (1,2,3)")
        val addressList = db.select(sql)
        val list = db.batchUpdate(addressList)
        Assertions.assertEquals(
            listOf(
                Address(1, "*STREET 1*", 2),
                Address(2, "*STREET 2*", 2),
                Address(3, "*STREET 3*", 2)
            ), list
        )
        val list2 = db.select<Address>(sql)
        Assertions.assertEquals(
            listOf(
                Address(1, "*STREET 1", 2),
                Address(2, "*STREET 2", 2),
                Address(3, "*STREET 3", 2)
            ), list2
        )
    }

    @Test
    fun updatedAt() {
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C")
        )
        db.batchInsert(personList)
        db.select<Person>(template("select /*%expand*/* from person")).let {
            db.batchUpdate(it)
        }
        val list = db.select<Person>(template("select /*%expand*/* from person"))
        Assertions.assertTrue(list.all { it.updatedAt > LocalDateTime.MIN })
    }

    @Test
    fun uniqueConstraintException() {
        assertThrows<UniqueConstraintException> {
            db.batchUpdate(
                listOf(
                    Address(1, "A", 1),
                    Address(2, "B", 1),
                    Address(3, "B", 1)
                )
            )
        }
    }

    @Test
    fun optimisticLockException() {
        assertThrows<OptimisticLockException> {
            db.batchUpdate(
                listOf(
                    Address(1, "A", 1),
                    Address(2, "B", 1),
                    Address(3, "C", 2)
                )
            )
        }
    }
    
 */
}
