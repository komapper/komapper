package integration.r2dbc

import integration.r2dbc.setting.Dbms
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.case
import org.komapper.core.dsl.concat
import org.komapper.core.dsl.count
import org.komapper.core.dsl.expression.When
import org.komapper.core.dsl.max
import org.komapper.core.dsl.plus
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcEntityDsl
import org.komapper.r2dbc.dsl.R2dbcSqlDsl

@ExtendWith(Env::class)
class ValueClassTest(val db: R2dbcDatabase) {

    @Test
    fun list() = inTransaction(db) {
        val a = VAddress.meta
        val list: List<VAddress> = db.runQuery {
            R2dbcEntityDsl.from(a).where { a.addressId eq IntId(1) }
        }.toList()
        assertNotNull(list)
        assertEquals(1, list.size)
    }

    @Test
    fun first() = inTransaction(db) {
        val a = VAddress.meta
        val address: VAddress = db.runQuery {
            R2dbcEntityDsl.from(a).where { a.addressId eq IntId(1) }.first()
        }
        assertNotNull(address)
        println(address)
    }

    @Test
    fun insert() = inTransaction(db) {
        val a = VAddress.meta
        val address = VAddress(IntId(16), Street("STREET 16"), Version(0))
        db.runQuery { R2dbcEntityDsl.insert(a).single(address) }
        val address2 = db.runQuery {
            R2dbcEntityDsl.from(a).first {
                a.addressId eq IntId(16)
            }
        }
        assertEquals(address, address2)
    }

    @Test
    fun insert_timestamp() = inTransaction(db) {
        val p = VPerson.meta
        val person1 = VPerson(IntId(1), "ABC")
        val id = db.runQuery { R2dbcEntityDsl.insert(p).single(person1) }.personId
        val person2 = db.runQuery { R2dbcEntityDsl.from(p).first { p.personId eq id } }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.runQuery {
            R2dbcEntityDsl.from(p).first {
                p.personId to 1
            }
        }
        assertEquals(person2, person3)
    }

    @Test
    fun update() = inTransaction(db) {
        val a = VAddress.meta
        val query = R2dbcEntityDsl.from(a).where { a.addressId eq IntId(15) }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = Street("NY street"))
        db.runQuery { R2dbcEntityDsl.update(a).single(newAddress) }
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
    fun updated_timestamp() = inTransaction(db) {
        val p = VPerson.meta
        val findQuery = R2dbcEntityDsl.from(p).first { p.personId eq IntId(1) }
        val person1 = VPerson(IntId(1), "ABC")
        val person2 = db.runQuery {
            R2dbcEntityDsl.insert(p).single(person1) + findQuery
        }
        val person3 = db.runQuery {
            R2dbcEntityDsl.update(p).single(person2.copy(name = "DEF")) + findQuery
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
        Assertions.assertNotEquals(person2.updatedAt, person3.updatedAt)
    }

    @Test
    fun delete() = inTransaction(db) {
        val a = VAddress.meta
        val query = R2dbcEntityDsl.from(a).where { a.addressId eq IntId(15) }
        val address = db.runQuery { query.first() }
        db.runQuery { R2dbcEntityDsl.delete(a).single(address) }
        assertEquals(emptyList<VAddress>(), db.runQuery { query }.toList())
    }

    @Test
    fun identityGenerator() = inTransaction(db) {
        for (i in 1..201) {
            val m = VIdentityStrategy.meta
            val strategy = VIdentityStrategy(IntId(0), "test")
            val result = db.runQuery { R2dbcEntityDsl.insert(m).single(strategy) }
            assertEquals(IntId(i), result.id)
        }
    }

    @Run(unless = [Dbms.MYSQL])
    @Test
    fun sequenceGenerator() = inTransaction(db) {
        for (i in 1..201) {
            val m = VSequenceStrategy.meta
            val strategy = VSequenceStrategy(IntId(0), "test")
            val result = db.runQuery { R2dbcEntityDsl.insert(m).single(strategy) }
            assertEquals(IntId(i), result.id)
        }
    }

    @Test
    fun inList2() = inTransaction(db) {
        val a = VAddress.meta
        val list: List<VAddress> = db.runQuery {
            R2dbcEntityDsl.from(a).where { (a.addressId to a.street) inList2 listOf(IntId(1) to Street("STREET 1")) }
        }.toList()
        assertEquals(1, list.size)
    }

    @Test
    fun endsWith() = inTransaction(db) {
        val a = VAddress.meta
        val list = db.runQuery {
            R2dbcEntityDsl.from(a).where { a.street endsWith "1" }
        }.toList()
        assertEquals(2, list.size)
        assertEquals(listOf(1, 11), list.map { it.addressId.value })
    }

    @Test
    fun between() = inTransaction(db) {
        val a = VAddress.meta
        val list = db.runQuery {
            R2dbcEntityDsl.from(a)
                .where { a.addressId between IntId(6)..IntId(10) }
                .orderBy(a.addressId)
        }.toList()
        assertEquals(5, list.size)
        assertEquals((6..10).toList(), list.map { it.addressId.value })
    }

    @Test
    fun notBetween() = inTransaction(db) {
        val a = VAddress.meta
        val list = db.runQuery {
            R2dbcEntityDsl.from(a)
                .where { a.addressId notBetween IntId(6)..IntId(10) }
                .orderBy(a.addressId)
        }.toList()
        assertEquals(10, list.size)
        assertEquals(((1..5) + (11..15)).toList(), list.map { it.addressId.value })
    }

    @Test
    fun select_single() = inTransaction(db) {
        val a = VAddress.meta
        val result = db.runQuery {
            R2dbcSqlDsl.from(a).orderBy(a.addressId).select(a.street).first()
        }
        assertEquals(Street("STREET 1"), result)
    }

    @Test
    fun select_pair() = inTransaction(db) {
        val a = VAddress.meta
        val result = db.runQuery {
            R2dbcSqlDsl.from(a).orderBy(a.addressId).select(a.addressId, a.street).first()
        }
        assertEquals(IntId(1) to Street("STREET 1"), result)
    }

    @Test
    fun expression_count() = inTransaction(db) {
        val a = VAddress.meta
        val count = db.runQuery {
            R2dbcSqlDsl.from(a).select(count()).first()
        }
        assertEquals(15, count)
    }

    @Test
    fun expression_max() = inTransaction(db) {
        val a = VAddress.meta
        val max = db.runQuery {
            R2dbcSqlDsl.from(a).select(max(a.addressId)).first()
        }
        assertEquals(IntId(15), max)
    }

    @Test
    fun expression_plus() = inTransaction(db) {
        val a = VAddress.meta
        val result = db.runQuery {
            R2dbcSqlDsl.from(a).orderBy(a.addressId).select(a.addressId + IntId(100)).first()
        }
        assertEquals(IntId(101), result)
    }

    @Test
    fun expression_concat() = inTransaction(db) {
        val a = VAddress.meta
        val result = db.runQuery {
            R2dbcSqlDsl.from(a).orderBy(a.addressId).select(concat(Street("["), concat(a.street, Street("]")))).first()
        }
        assertEquals(Street("[STREET 1]"), result)
    }

    @Test
    fun expression_case() = inTransaction(db) {
        val a = VAddress.meta
        val caseExpression = case(
            When(
                { a.street eq Street("STREET 2"); a.addressId greater IntId(1) },
                concat(a.street, Street("!!!"))
            )
        ) { a.street }
        val list = db.runQuery {
            R2dbcSqlDsl.from(a).where { a.addressId inList listOf(IntId(1), IntId(2), IntId(3)) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }.toList()
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
