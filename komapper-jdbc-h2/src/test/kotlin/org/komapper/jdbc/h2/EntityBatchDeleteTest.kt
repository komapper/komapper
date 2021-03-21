package org.komapper.jdbc.h2

import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database

@ExtendWith(Env::class)
class EntityBatchDeleteTest(private val db: Database) {
/*
    @Test
    fun test() {
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        db.batchInsert(addressList)
        val sql = template<Address>("select * from address where address_id in (16, 17, 18)")
        Assertions.assertEquals(
            addressList,
            db.select(sql)
        )
        db.batchDelete(addressList)
        Assertions.assertTrue(db.select<Address>(sql).isEmpty())
    }

    @Test
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    fun globalEntityListener() {
        val db = Db(object : DbConfig() {
            override val dataSource = db.config.dataSource
            override val dialect = db.config.dialect
            override val entityMetaResolver = db.config.entityMetaResolver
            override val listener = object : GlobalEntityListener {
                override fun <T : Any> preDelete(
                    entity: T,
                    desc: EntityDesc<T>
                ): T {
                    return when (entity) {
                        is Address -> entity.copy(street = "*${entity.street}")
                        else -> entity
                    } as T
                }

                override fun <T : Any> postDelete(
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
        db.batchInsert(addressList)
        val sql = template<Address>("select * from address where address_id in (16, 17, 18)")
        Assertions.assertEquals(
            addressList,
            db.select(sql)
        )
        val list = db.batchDelete(addressList)
        Assertions.assertEquals(
            listOf(
                Address(16, "*STREET 16*", 0),
                Address(17, "*STREET 17*", 0),
                Address(18, "*STREET 18*", 0)
            ), list
        )
        Assertions.assertTrue(
            db.select<Address>(
                sql
            ).isEmpty()
        )
    }

    @Test
    fun entityListener() {
        val db = Db(
            AddressListenerConfig(
                db.config,
                object :
                    EntityListener<Address> {
                    override fun preDelete(
                        entity: Address,
                        desc: EntityDesc<Address>
                    ): Address {
                        return entity.copy(street = "*${entity.street}")
                    }

                    override fun postDelete(
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
        db.batchInsert(addressList)
        val sql = template<Address>("select * from address where address_id in (16, 17, 18)")
        Assertions.assertEquals(
            addressList,
            db.select(sql)
        )
        val list = db.batchDelete(addressList)
        Assertions.assertEquals(
            listOf(
                Address(16, "*STREET 16*", 0),
                Address(17, "*STREET 17*", 0),
                Address(18, "*STREET 18*", 0)
            ), list
        )
        Assertions.assertTrue(
            db.select<Address>(
                sql
            ).isEmpty()
        )
    }

    @Test
    fun optimisticLockException() {
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        db.batchInsert(addressList)
        assertThrows<OptimisticLockException> {
            db.batchDelete(
                listOf(
                    addressList[0],
                    addressList[1],
                    addressList[2].copy(version = +1)
                )
            )
        }
    }
    
 */
}
