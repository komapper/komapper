package integration.jdbc

import integration.IntId
import integration.Street
import integration.VAddress
import integration.VIdentityStrategy
import integration.VPerson
import integration.VSequenceStrategy
import integration.Version
import integration.meta
import integration.setting.Dbms
import integration.setting.Run
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.expression.When
import org.komapper.core.dsl.operator.case
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.operator.max
import org.komapper.core.dsl.operator.plus
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@ExtendWith(Env::class)
class ValueClassTest(val db: JdbcDatabase) {

    @Test
    fun list() {
        val a = VAddress.meta
        val list: List<VAddress> = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq IntId(1) }
        }
        assertNotNull(list)
        assertEquals(1, list.size)
    }

    @Test
    fun first() {
        val a = VAddress.meta
        val address: VAddress = db.runQuery {
            EntityDsl.from(a).where { a.addressId eq IntId(1) }.first()
        }
        assertNotNull(address)
        println(address)
    }

    @Test
    fun insert() {
        val a = VAddress.meta
        val address = VAddress(IntId(16), Street("STREET 16"), Version(0))
        db.runQuery { EntityDsl.insert(a).single(address) }
        val address2 = db.runQuery {
            EntityDsl.from(a).where {
                a.addressId eq IntId(16)
            }.first()
        }
        assertEquals(address, address2)
    }

    @Test
    fun insert_timestamp() {
        val p = VPerson.meta
        val person1 = VPerson(IntId(1), "ABC")
        val id = db.runQuery { EntityDsl.insert(p).single(person1) }.personId
        val person2 = db.runQuery { EntityDsl.from(p).where { p.personId eq id }.first() }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.runQuery {
            EntityDsl.from(p).where {
                p.personId to 1
            }.first()
        }
        assertEquals(person2, person3)
    }

    @Test
    fun update() {
        val a = VAddress.meta
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
        val p = VPerson.meta
        val findQuery = EntityDsl.from(p).where { p.personId eq IntId(1) }.first()
        val person1 = VPerson(IntId(1), "ABC")
        val person2 = db.runQuery {
            EntityDsl.insert(p).single(person1) + findQuery
        }
        val person3 = db.runQuery {
            EntityDsl.update(p).single(person2.copy(name = "DEF")) + findQuery
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
        assertNotEquals(person2.updatedAt, person3.updatedAt)
    }

    @Test
    fun delete() {
        val a = VAddress.meta
        val query = EntityDsl.from(a).where { a.addressId eq IntId(15) }
        val address = db.runQuery { query.first() }
        db.runQuery { EntityDsl.delete(a).single(address) }
        assertEquals(emptyList<VAddress>(), db.runQuery { query })
    }

    @Test
    fun identityGenerator() {
        for (i in 1..201) {
            val m = VIdentityStrategy.meta
            val strategy = VIdentityStrategy(IntId(0), "test")
            val result = db.runQuery { EntityDsl.insert(m).single(strategy) }
            assertEquals(IntId(i), result.id)
        }
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun sequenceGenerator() {
        for (i in 1..201) {
            val m = VSequenceStrategy.meta
            val strategy = VSequenceStrategy(IntId(0), "test")
            val result = db.runQuery { EntityDsl.insert(m).single(strategy) }
            assertEquals(IntId(i), result.id)
        }
    }

    @Test
    fun inList2() {
        val a = VAddress.meta
        val list: List<VAddress> = db.runQuery {
            EntityDsl.from(a).where { (a.addressId to a.street) inList2 listOf(IntId(1) to Street("STREET 1")) }
        }
        assertEquals(1, list.size)
    }

    @Test
    fun endsWith() {
        val a = VAddress.meta
        val list = db.runQuery {
            EntityDsl.from(a).where { a.street endsWith "1" }
        }
        assertEquals(2, list.size)
        assertEquals(listOf(1, 11), list.map { it.addressId.value })
    }

    @Test
    fun between() {
        val a = VAddress.meta
        val list = db.runQuery {
            EntityDsl.from(a)
                .where { a.addressId between IntId(6)..IntId(10) }
                .orderBy(a.addressId)
        }
        assertEquals(5, list.size)
        assertEquals((6..10).toList(), list.map { it.addressId.value })
    }

    @Test
    fun notBetween() {
        val a = VAddress.meta
        val list = db.runQuery {
            EntityDsl.from(a)
                .where { a.addressId notBetween IntId(6)..IntId(10) }
                .orderBy(a.addressId)
        }
        assertEquals(10, list.size)
        assertEquals(((1..5) + (11..15)).toList(), list.map { it.addressId.value })
    }

    @Test
    fun select_single() {
        val a = VAddress.meta
        val result = db.runQuery {
            SqlDsl.from(a).orderBy(a.addressId).select(a.street).first()
        }
        assertEquals(Street("STREET 1"), result)
    }

    @Test
    fun select_pair() {
        val a = VAddress.meta
        val result = db.runQuery {
            SqlDsl.from(a).orderBy(a.addressId).select(a.addressId, a.street).first()
        }
        assertEquals(IntId(1) to Street("STREET 1"), result)
    }

    @Test
    fun expression_count() {
        val a = VAddress.meta
        val count = db.runQuery {
            SqlDsl.from(a).select(count()).first()
        }
        assertEquals(15, count)
    }

    @Test
    fun expression_max() {
        val a = VAddress.meta
        val max = db.runQuery {
            SqlDsl.from(a).select(max(a.addressId)).first()
        }
        assertEquals(IntId(15), max)
    }

    @Test
    fun expression_plus() {
        val a = VAddress.meta
        val result = db.runQuery {
            SqlDsl.from(a).orderBy(a.addressId).select(a.addressId + IntId(100)).first()
        }
        assertEquals(IntId(101), result)
    }

    @Test
    fun expression_concat() {
        val a = VAddress.meta
        val result = db.runQuery {
            SqlDsl.from(a).orderBy(a.addressId).select(concat(Street("["), concat(a.street, Street("]")))).first()
        }
        assertEquals(Street("[STREET 1]"), result)
    }

    @Test
    fun expression_case() {
        val a = VAddress.meta
        val caseExpression = case(
            When(
                { a.street eq Street("STREET 2"); a.addressId greater IntId(1) },
                concat(a.street, Street("!!!"))
            )
        ) { a.street }
        val list = db.runQuery {
            SqlDsl.from(a).where { a.addressId inList listOf(IntId(1), IntId(2), IntId(3)) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }
        assertEquals(
            listOf(
                Street("STREET 1") to Street("STREET 1"),
                Street("STREET 2") to Street("STREET 2!!!"),
                Street("STREET 3") to Street("STREET 3")
            ),
            list
        )
    }
}
