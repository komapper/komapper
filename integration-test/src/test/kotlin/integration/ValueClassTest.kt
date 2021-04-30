package integration

import integration.setting.Dbms
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.concat
import org.komapper.core.dsl.count
import org.komapper.core.dsl.max
import org.komapper.core.dsl.plus
import org.komapper.core.dsl.runQuery

@ExtendWith(Env::class)
class ValueClassTest(val db: Database) {

    @Test
    fun list() {
        val a = VAddress.alias
        val list: List<VAddress> = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq IntId(1) }
        }
        assertNotNull(list)
        assertEquals(1, list.size)
    }

    @Test
    fun first() {
        val a = VAddress.alias
        val address: VAddress = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq IntId(1) }.first()
        }
        assertNotNull(address)
        println(address)
    }

    @Test
    fun insert() {
        val a = VAddress.alias
        val address = VAddress(IntId(16), Street("STREET 16"), Version(0))
        db.runQuery { EntityDsl.insert(a).single(address) }
        val address2 = db.runQuery {
            EntityDsl.from(a).first {
                a.addressId eq IntId(16)
            }
        }
        assertEquals(address, address2)
    }

    @Test
    fun insert_timestamp() {
        val p = VPerson.alias
        val person1 = VPerson(IntId(1), "ABC")
        val id = db.runQuery { EntityDsl.insert(p).single(person1) }.personId
        val person2 = db.runQuery { EntityDsl.from(p).first { p.personId eq id } }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.runQuery {
            EntityDsl.from(p).first {
                p.personId to 1
            }
        }
        assertEquals(person2, person3)
    }

    @Test
    fun update() {
        val a = VAddress.alias
        val query = EntityDsl.from(a).where { a.addressId eq IntId(15) }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = Street("NY street"))
        db.runQuery { EntityDsl.update(a).single(newAddress) }
        val address2 = db.runQuery { query.firstOrNull() }
        assertEquals(
            VAddress(
                IntId(15),
                Street("NY street"),
                Version(2)
            ),
            address2
        )
    }

    @Test
    fun updated_timestamp() {
        val p = VPerson.alias
        val findQuery = EntityDsl.from(p).first { p.personId eq IntId(1) }
        val person1 = VPerson(IntId(1), "ABC")
        val person2 = db.runQuery {
            EntityDsl.insert(p).single(person1) + findQuery
        }
        val person3 = db.runQuery {
            EntityDsl.update(p).single(person2.copy(name = "DEF")) + findQuery
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
        Assertions.assertNotEquals(person2.updatedAt, person3.updatedAt)
    }

    @Test
    fun delete() {
        val a = VAddress.alias
        val query = EntityDsl.from(a).where { a.addressId eq IntId(15) }
        val address = db.runQuery { query.first() }
        db.runQuery { EntityDsl.delete(a).single(address) }
        assertEquals(emptyList<VAddress>(), db.runQuery { query })
    }

    @Test
    fun identityGenerator() {
        for (i in 1..201) {
            val m = VIdentityStrategy.alias
            val strategy = VIdentityStrategy(IntId(0), "test")
            val result = db.runQuery { EntityDsl.insert(m).single(strategy) }
            assertEquals(IntId(i), result.id)
        }
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun sequenceGenerator() {
        for (i in 1..201) {
            val m = VSequenceStrategy.alias
            val strategy = VSequenceStrategy(IntId(0), "test")
            val result = db.runQuery { EntityDsl.insert(m).single(strategy) }
            assertEquals(IntId(i), result.id)
        }
    }

    @Test
    fun inList2() {
        val a = VAddress.alias
        val list: List<VAddress> = db.runQuery {
            EntityDsl.from(a).where { (a.addressId to a.street) inList2 listOf(IntId(1) to Street("STREET 1")) }
        }
        assertEquals(1, list.size)
    }

    @Test
    fun endsWith() {
        val a = VAddress.alias
        val list = db.runQuery {
            EntityDsl.from(a).where { a.street endsWith "1" }
        }
        assertEquals(2, list.size)
        assertEquals(listOf(1, 11), list.map { it.addressId.value })
    }

    @Test
    fun select_single() {
        val a = VAddress.alias
        val result = db.runQuery {
            SqlDsl.from(a).orderBy(a.addressId).select(a.street).first()
        }
        assertEquals(Street("STREET 1"), result)
    }

    @Test
    fun select_pair() {
        val a = VAddress.alias
        val result = db.runQuery {
            SqlDsl.from(a).orderBy(a.addressId).select(a.addressId, a.street).first()
        }
        assertEquals(IntId(1) to Street("STREET 1"), result)
    }

    @Test
    fun expression_count() {
        val a = VAddress.alias
        val count = db.runQuery {
            SqlDsl.from(a).select(count()).first()
        }
        assertEquals(15, count)
    }

    @Test
    fun expression_max() {
        val a = VAddress.alias
        val max = db.runQuery {
            SqlDsl.from(a).select(max(a.addressId)).first()
        }
        assertEquals(IntId(15), max)
    }

    @Test
    fun expression_plus() {
        val a = VAddress.alias
        val result = db.runQuery {
            SqlDsl.from(a).orderBy(a.addressId).select(a.addressId + IntId(100)).first()
        }
        assertEquals(IntId(101), result)
    }

    @Test
    fun expression_concat() {
        val a = VAddress.alias
        val result = db.runQuery {
            SqlDsl.from(a).orderBy(a.addressId).select(concat(Street("["), concat(a.street, Street("]")))).first()
        }
        assertEquals(Street("[STREET 1]"), result)
    }
}
