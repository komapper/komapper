package integration.r2dbc

import integration.core.Dbms
import integration.core.IntId
import integration.core.Run
import integration.core.Street
import integration.core.VAddress
import integration.core.VIdentityStrategy
import integration.core.VPerson
import integration.core.VSequenceStrategy
import integration.core.Version
import integration.core.vAddress
import integration.core.vIdentityStrategy
import integration.core.vPerson
import integration.core.vSequenceStrategy
import kotlinx.coroutines.delay
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.When
import org.komapper.core.dsl.operator.case
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.operator.max
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.query.andThen
import org.komapper.core.dsl.query.bind
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.core.dsl.query.getNotNull
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@ExtendWith(R2dbcEnv::class)
class R2dbcValueClassTest(val db: R2dbcDatabase) {

    @Test
    fun list(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val list: List<VAddress> = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq IntId(1) }
        }
        assertNotNull(list)
        assertEquals(1, list.size)
    }

    @Test
    fun first(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val address: VAddress = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq IntId(1) }.first()
        }
        assertNotNull(address)
        println(address)
    }

    @Test
    fun insert(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val address = VAddress(IntId(16), Street("STREET 16"), Version(0))
        db.runQuery { QueryDsl.insert(a).single(address) }
        val address2 = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq IntId(16)
            }.first()
        }
        assertEquals(address, address2)
    }

    @Test
    fun insert_timestamp(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.vPerson
        val person1 = VPerson(IntId(1), "ABC")
        val id = db.runQuery { QueryDsl.insert(p).single(person1) }.personId
        val person2 = db.runQuery { QueryDsl.from(p).where { p.personId eq id }.first() }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId to 1
            }.first()
        }
        assertEquals(person2, person3)
    }

    @Test
    fun update(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val query = QueryDsl.from(a).where { a.addressId eq IntId(15) }
        val address = db.runQuery { query.first() }
        val newAddress = address.copy(street = Street("NY street"))
        db.runQuery { QueryDsl.update(a).single(newAddress) }
        val address2 = db.runQuery { query.firstOrNull() }
        assertEquals(
            VAddress(
                IntId(15),
                Street("NY street"),
                Version(2),
            ),
            address2,
        )
    }

    @Test
    fun updated_timestamp(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.vPerson
        val findQuery = QueryDsl.from(p).where { p.personId eq IntId(1) }.first()
        val person1 = VPerson(IntId(1), "ABC")
        val person2 = db.runQuery {
            QueryDsl.insert(p).single(person1).andThen(findQuery)
        }
        delay(10)
        val person3 = db.runQuery {
            QueryDsl.update(p).single(person2.copy(name = "DEF")).andThen(findQuery)
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
        assertNotEquals(person2.updatedAt, person3.updatedAt)
    }

    @Test
    fun delete(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val query = QueryDsl.from(a).where { a.addressId eq IntId(15) }
        val address = db.runQuery { query.first() }
        db.runQuery { QueryDsl.delete(a).single(address) }
        assertEquals(emptyList<VAddress>(), db.runQuery { query })
    }

    @Test
    fun identityGenerator(info: TestInfo) = inTransaction(db, info) {
        for (i in 1..201) {
            val m = Meta.vIdentityStrategy
            val strategy = VIdentityStrategy(IntId(0), "test")
            val result = db.runQuery { QueryDsl.insert(m).single(strategy) }
            assertEquals(IntId(i), result.id)
        }
    }

    @Run(unless = [Dbms.MYSQL, Dbms.MYSQL_5])
    @Test
    fun sequenceGenerator(info: TestInfo) = inTransaction(db, info) {
        for (i in 1..201) {
            val m = Meta.vSequenceStrategy
            val strategy = VSequenceStrategy(IntId(0), "test")
            val result = db.runQuery { QueryDsl.insert(m).single(strategy) }
            assertEquals(IntId(i), result.id)
        }
    }

    @Test
    fun inList2(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val list: List<VAddress> = db.runQuery {
            QueryDsl.from(a).where { (a.addressId to a.street) inList2 listOf(IntId(1) to Street("STREET 1")) }
        }
        assertEquals(1, list.size)
    }

    @Test
    fun endsWith(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val list = db.runQuery {
            QueryDsl.from(a).where { a.street endsWith "1" }
        }
        assertEquals(2, list.size)
        assertEquals(listOf(1, 11), list.map { it.addressId.value })
    }

    @Test
    fun between(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val list = db.runQuery {
            QueryDsl.from(a)
                .where { a.addressId between IntId(6)..IntId(10) }
                .orderBy(a.addressId)
        }
        assertEquals(5, list.size)
        assertEquals((6..10).toList(), list.map { it.addressId.value })
    }

    @Test
    fun notBetween(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val list = db.runQuery {
            QueryDsl.from(a)
                .where { a.addressId notBetween IntId(6)..IntId(10) }
                .orderBy(a.addressId)
        }
        assertEquals(10, list.size)
        assertEquals(((1..5) + (11..15)).toList(), list.map { it.addressId.value })
    }

    @Test
    fun select_single(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val result = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq IntId(1) }.orderBy(a.addressId).select(a.street).first()
        }
        assertEquals(Street("STREET 1"), result)
    }

    @Test
    fun select_pair(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val result = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq IntId(1) }.orderBy(a.addressId).select(a.addressId, a.street)
                .first()
        }
        assertEquals(IntId(1) to Street("STREET 1"), result)
    }

    @Test
    fun expression_count(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val count = db.runQuery {
            QueryDsl.from(a).select(count())
        }
        assertEquals(15, count)
    }

    @Test
    fun expression_max(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val max = db.runQuery {
            QueryDsl.from(a).select(max(a.addressId))
        }
        assertEquals(IntId(15), max)
    }

    @Test
    fun expression_plus(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val result = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq IntId(1) }.orderBy(a.addressId).select(a.addressId + IntId(100))
                .first()
        }
        assertEquals(IntId(101), result)
    }

    @Test
    fun expression_concat(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val result = db.runQuery {
            QueryDsl.from(a).where { a.addressId eq IntId(1) }.orderBy(a.addressId)
                .select(concat(Street("["), concat(a.street, Street("]")))).first()
        }
        assertEquals(Street("[STREET 1]"), result)
    }

    @Test
    fun expression_case(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.vAddress
        val caseExpression = case(
            When(
                {
                    a.street eq Street("STREET 2")
                    a.addressId greater IntId(1)
                },
                concat(a.street, Street("!!!")),
            ),
        ) { a.street }
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList listOf(IntId(1), IntId(2), IntId(3)) }
                .orderBy(a.addressId)
                .select(a.street, caseExpression)
        }
        assertEquals(
            listOf(
                Street("STREET 1") to Street("STREET 1"),
                Street("STREET 2") to Street("STREET 2!!!"),
                Street("STREET 3") to Street("STREET 3"),
            ),
            list,
        )
    }

    @Test
    fun list_using_template(info: TestInfo) = inTransaction(db, info) {
        val list: List<VAddress> = db.runQuery {
            QueryDsl.fromTemplate("select * from address where address_id = /*id*/0")
                .bind("id", IntId(1))
                .select { row ->
                    VAddress(
                        IntId(row.getNotNull("address_id")),
                        Street(row.getNotNull("street")),
                        Version(row.getNotNull("version")),
                    )
                }
        }
        assertNotNull(list)
        assertEquals(1, list.size)
    }

    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun inList2_using_template(info: TestInfo) = inTransaction(db, info) {
        val list: List<VAddress> = db.runQuery {
            QueryDsl.fromTemplate("select * from address where (address_id, street) in /*pairs*/(0, '')")
                .bind("pairs", listOf(IntId(1) to Street("STREET 1")))
                .select { row ->
                    VAddress(
                        IntId(row.getNotNull("address_id")),
                        Street(row.getNotNull("street")),
                        Version(row.getNotNull("version")),
                    )
                }
        }
        assertNotNull(list)
        assertEquals(1, list.size)
    }
}
