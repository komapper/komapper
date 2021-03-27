package org.komapper.jdbc.h2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.OptimisticLockException
import org.komapper.core.query.EntityQuery

@ExtendWith(Env::class)
class EntityDeleteQueryTest(private val db: Database) {

    /*
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    @Test
    fun globalEntityListener() {
        val db = Db(
            object : DbConfig() {
                override val dataSource = db.config.dataSource
                override val dialect = db.config.dialect
                override val entityMetaResolver = db.config.entityMetaResolver
                override val listener: GlobalEntityListener =
                    object : GlobalEntityListener {
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
            }
        )

        val sql = template<Address>("select * from address where address_id = 15")
        val address = db.select(sql).first()
        val address2 = db.delete(address)
        Assertions.assertEquals(
            Address(
                15,
                "*STREET 15*",
                1
            ), address2
        )
        val address3 = db.select(sql).firstOrNull()
        Assertions.assertNull(address3)
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

        val sql = template<Address>("select * from address where address_id = 15")
        val address = db.select(sql).first()
        val address2 = db.delete(address)
        Assertions.assertEquals(
            Address(
                15,
                "*STREET 15*",
                1
            ), address2
        )
        val address3 = db.select(sql).firstOrNull()
        Assertions.assertNull(address3)
    }
*/
    @Test
    fun optimisticLockException() {
        val a = Address.metamodel()
        val address = db.find(a) { a.addressId eq 15 }
        db.delete(a, address)
        assertThrows<OptimisticLockException> {
            db.delete(a, address)
        }
    }

    @Test
    fun testEntity() {
        val a = Address.metamodel()
        val query = EntityQuery.from(a).where { a.addressId eq 15 }
        val address = db.execute(query.first())
        db.delete(a, address)
        assertEquals(emptyList<Address>(), db.execute(query))
    }
}
