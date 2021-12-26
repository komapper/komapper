package integration.r2dbc

import integration.address
import integration.employee
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.TemplateDsl
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.ScalarQuery
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class FlowTest(val db: R2dbcDatabase) {

    @Test
    fun singleEntity() = inTransaction(db) {
        val flow = db.flow {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2) }.orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList().map { it.addressId })
    }

    @Test
    fun singleEntity_union() = inTransaction(db) {
        val flow = db.flow {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId eq 1 }.union(
                QueryDsl.from(a).where { a.addressId eq 2 }
            ).orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList().map { it.addressId })
    }

    @Test
    fun singleColumn() = inTransaction(db) {
        val flow = db.flow {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList())
    }

    @Test
    fun singleNotNullColumn() = inTransaction(db) {
        val flow: Flow<Int> = db.flow {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .selectNotNull(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList())
    }

    @Test
    fun singleColumn_union() = inTransaction(db) {
        val flow = db.flow {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId).union(
                    QueryDsl.from(Meta.address)
                        .where { a.addressId eq 2 }
                        .select(a.addressId)
                ).orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList())
    }

    @Test
    fun pairColumns() = inTransaction(db) {
        val flow = db.flow {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street)
        }
        assertEquals(
            listOf(
                1 to "STREET 1",
                2 to "STREET 2"
            ),
            flow.toList()
        )
    }

    @Test
    fun pairColumns_union() = inTransaction(db) {
        val flow = db.flow {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId, a.street).union(
                    QueryDsl.from(Meta.address)
                        .where { a.addressId eq 2 }
                        .select(a.addressId, a.street)
                ).orderBy(a.addressId)
        }
        assertEquals(
            listOf(
                1 to "STREET 1",
                2 to "STREET 2"
            ),
            flow.toList()
        )
    }

    @Test
    fun tripleColumns() = inTransaction(db) {
        val flow = db.flow {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1)
            ),
            flow.toList()
        )
    }

    @Test
    fun tripleColumns_union() = inTransaction(db) {
        val flow = db.flow {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId, a.street, a.version).union(
                    QueryDsl.from(a)
                        .where { a.addressId eq 2 }
                        .select(a.addressId, a.street, a.version)
                ).orderBy(a.addressId)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1)
            ),
            flow.toList()
        )
    }

    @Test
    fun multipleColumns() = inTransaction(db) {
        val a = Meta.address
        val flow = db.flow {
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version, a.addressId)
        }
        val list = flow.toList()
        assertEquals(2, list.size)
        assertEquals(1, list[0][a.addressId])
        assertEquals(2, list[1][a.addressId])
    }

    @Test
    fun multipleColumns_union() = inTransaction(db) {
        val e = Meta.employee
        val flow = db.flow {
            QueryDsl.from(e)
                .where { e.employeeId eq 1 }
                .select(e.employeeId, e.employeeNo, e.employeeName, e.salary).union(
                    QueryDsl.from(e)
                        .where { e.employeeId eq 2 }
                        .select(e.employeeId, e.employeeNo, e.employeeName, e.salary)
                ).orderBy(e.employeeId)
        }
        val list = flow.toList()
        assertEquals(2, list.size)
        assertEquals(1, list[0][e.employeeId])
        assertEquals(2, list[1][e.employeeId])
    }

    @Test
    fun template() = inTransaction(db) {
        val flow = db.flow {
            TemplateDsl.from("select address_id from ADDRESS order by address_id").select { it.asInt("address_id") }
        }
        assertEquals((1..15).toList(), flow.toList())
    }

    @Test
    fun scalar() = inTransaction(db) {
        val a = Meta.address
        val scalarQuery: ScalarQuery<Long?, Long, Long> = QueryDsl.from(a).select(count(a.street))
        val flow = db.flow { scalarQuery }
        assertEquals(listOf(15L), flow.toList())
    }
}
